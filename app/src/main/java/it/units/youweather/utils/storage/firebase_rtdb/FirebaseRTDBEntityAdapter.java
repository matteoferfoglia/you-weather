package it.units.youweather.utils.storage.firebase_rtdb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.youweather.EnvironmentVariables;
import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.storage.DBEntity;
import it.units.youweather.utils.storage.DBEntityAdapter;
import it.units.youweather.utils.storage.Query;

public class FirebaseRTDBEntityAdapter<T extends DBEntity> extends DBEntityAdapter<T> {


    /**
     * TAG for the logger for this class.
     */
    private final static String TAG = "FirebaseRealTimeDB";

    /**
     * Reference to the table in the database.
     */
    private final DatabaseReference dbRef;

    /**
     * Listener for changes (add/update/remove) to tuples of this table in the database.
     */
    private ChildEventListener tableListener;

    /**
     * Synchronizes local changes with Firebase Realtime Database.
     */
    private final FirebaseRTDBSynchronizer firebaseRTDBSynchronizer;

    /**
     * Constructor.
     * See {@link DBEntityAdapter} for parameters description.
     */
    public FirebaseRTDBEntityAdapter(Class<T> entityClass, Consumer<T> onCreated, Consumer<T> onRemoved, Consumer<T> onUpdated) {
        super(entityClass, onCreated, onRemoved, onUpdated);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        if (EnvironmentVariables.USE_FIREBASE_EMULATORS) {
            db.useEmulator(
                    EnvironmentVariables.FIREBASE_EMULATOR_HOST,
                    EnvironmentVariables.FIREBASE_EMULATOR_REALTIME_DB_PORT);
        }

        dbRef = db.getReference(
                Objects.requireNonNull(
                        Objects.requireNonNull(entityClass)
                                .getCanonicalName())
                        .replaceAll("\\.",
                                "_"));

        assert tableListener != null;
        dbRef.addChildEventListener(tableListener);

        this.firebaseRTDBSynchronizer = new FirebaseRTDBSynchronizer(dbRef);
    }

    @Override
    public void push(DBEntity newTuple, Runnable onSuccess, Runnable onError) {
        Log.d(TAG, "push method execution started");

        String newTupleId = Objects.requireNonNull(dbRef.push().getKey());
        newTuple.setId(newTupleId);
        firebaseRTDBSynchronizer.observeNewEntity(newTuple);
        dbRef.child(newTupleId)
                .setValue(newTuple)     // create a new tuple with the content
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "push successfully completed");
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        Log.e(TAG, "error with push", task.getException());
                        if (onError != null) {
                            onError.run();
                        }
                    }
                });
        Log.d(TAG, "push method execution terminated");
    }

    @Override
    public void pull(@NonNull Consumer<List<T>> onSuccess, @Nullable Runnable onError) {
        Log.d(TAG, "pull method execution started");

        ValueEventListener querySingleValueEventListener = getSingleValueEventListenerForQuery(onSuccess, onError);
        dbRef.addListenerForSingleValueEvent(querySingleValueEventListener);

        Log.d(TAG, "pull method execution terminated");
    }

    /**
     * @return The {@link ValueEventListener} to use for queries
     * (See {@link #pull(Consumer, Runnable)} and {@link #pull(Query, Consumer, Runnable)}).
     */
    @NonNull
    private ValueEventListener getSingleValueEventListenerForQuery(@NonNull Consumer<List<T>> onSuccess, @Nullable Runnable onError) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> content = (Map<String, Object>) snapshot.getValue();
                if (content == null) {
                    content = new ConcurrentHashMap<>(0);
                }

                // TODO : maybe a loader (graphic) is needed until data are downloaded

                // De-serialization of the object
                Map<String, T> deserializedContent = new ConcurrentHashMap<>(); // data form DB
                Gson gson = new Gson();
                for (Map.Entry<String, Object> aTuple : content.entrySet()) {
                    String json = gson.toJson(aTuple.getValue());
                    deserializedContent.put(aTuple.getKey(), gson.fromJson(json, (Type) getDbEntityClass()));
                }

                Log.d(TAG, "DB event listener - onDataChanged : "
                        + content.size() + " elements retrieved");

                List<T> results = new ArrayList<>(deserializedContent.values());
                Collections.sort(results, (a, b) -> a.toString().compareTo(b.toString()));
                Objects.requireNonNull(onSuccess).accept(results);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onError != null) {
                    onError.run();
                }
            }
        };
    }

    @Override
    public <S> void pull(@NonNull Query<S> query, @NonNull Consumer<List<T>> onSuccess, @Nullable Runnable onError) {
        Log.d(TAG, "pull query method execution started");

        S queryMinValue = Objects.requireNonNull(query).getMinValueInclusive();
        Class<S> fieldForQueryClass = (Class<S>) queryMinValue.getClass();

        com.google.firebase.database.Query q = dbRef.orderByChild(query.getField().getName());
        if (queryMinValue instanceof String) {
            q = q.startAt((String) query.getMinValueInclusive())
                    .endAt((String) query.getMaxValueInclusive());
        } else if (queryMinValue instanceof Double) {
            q = q.startAt((Double) query.getMinValueInclusive())
                    .endAt((Double) query.getMaxValueInclusive());
        } else if (queryMinValue instanceof Boolean) {
            q = q.startAt((Boolean) query.getMinValueInclusive())
                    .endAt((Boolean) query.getMaxValueInclusive());
        } else {
            throw new IllegalArgumentException("Invalid class " + fieldForQueryClass
                    + ". You can use only: "
                    + String.class + ", " + Double.class + ", " + Boolean.class);
        }


        // Try to execute the query and, if after a given amount of time the query execution
        // is not completed yet, abort the execution and run the onError callback

        final int AMOUNT_OF_SECONDS_FOR_FAILURE_DETECTION = 5;

        AtomicBoolean queryExecutionCompleted = new AtomicBoolean(false);
        Executors.newScheduledThreadPool(1).schedule(() -> {
            synchronized (queryExecutionCompleted) {
                if (!queryExecutionCompleted.get() && onError != null) {
                    onError.run();
                    queryExecutionCompleted.set(true);
                }
            }
        }, AMOUNT_OF_SECONDS_FOR_FAILURE_DETECTION, TimeUnit.SECONDS);
        q.addListenerForSingleValueEvent(getSingleValueEventListenerForQuery(results -> {
            synchronized (queryExecutionCompleted) {
                if (!queryExecutionCompleted.get()) {
                    queryExecutionCompleted.set(true);
                    onSuccess.accept(results);
                }
            }
        }, onError));

        Log.d(TAG, "pull query method execution terminated:" +
                " it will asynchronously download the data for the query");
    }

    @Override
    public void observeDBChanges(
            Consumer<T> onCreated, Consumer<T> onRemoved, Consumer<T> onUpdated) {
        // The listener to this table, needed to listen for changes and get the data.
        // The system is thought for asynchronous operations.

        tableListener = new ChildEventListener() {
            private void logDataChangedInfo() {
                Log.i(TAG, "Data changed.");
            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(TAG, "New tuple: " + changedContent);
                    onCreated.accept(changedContent);
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(TAG, "Updated tuple: " + changedContent);
                    onUpdated.accept(changedContent);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(TAG, "Removed tuple: " + changedContent);
                    onRemoved.accept(changedContent);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(TAG, "Moved tuple: " + changedContent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "EventListener CANCELLED", error.toException());
            }
        };
    }

    @Override
    public void forget(DBEntity tuple) {
        firebaseRTDBSynchronizer.stopObserve(tuple);
    }


    @Override
    public void remove(DBEntity tuple) {
        forget(tuple);
        dbRef.child(tuple.getId()).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "remove successfully completed");
                    } else {
                        Log.e(TAG, "error with remove", task.getException());
                    }
                });
    }

}
