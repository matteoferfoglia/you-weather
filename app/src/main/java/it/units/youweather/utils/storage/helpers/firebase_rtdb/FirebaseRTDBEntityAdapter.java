package it.units.youweather.utils.storage.helpers.firebase_rtdb;

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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import it.units.youweather.EnvironmentVariables;
import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.storage.entities.DBEntity;
import it.units.youweather.utils.storage.helpers.DBEntityAdapter;

public class FirebaseRTDBEntityAdapter<T extends DBEntity> extends DBEntityAdapter<T> {


    /**
     * TAG for the logger for this class.
     */
    private final static String FIREBASE_RT_DB_TAG = "FirebaseRealTimeDB";

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
        Log.d(FIREBASE_RT_DB_TAG, "push method execution started");

        String newTupleId = Objects.requireNonNull(dbRef.push().getKey());
        newTuple.setId(newTupleId);
        firebaseRTDBSynchronizer.observeNewEntity(newTuple);
        dbRef.child(newTupleId)
                .setValue(newTuple)     // create a new tuple with the content
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(FIREBASE_RT_DB_TAG, "push successfully completed");
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        Log.e(FIREBASE_RT_DB_TAG, "error with push", task.getException());
                        if (onError != null) {
                            onError.run();
                        }
                    }
                });
        Log.d(FIREBASE_RT_DB_TAG, "push method execution terminated");
    }

    @Override
    public void pull(@NonNull Consumer<Collection<T>> onSuccess, @Nullable Runnable onError) {
        Log.d(FIREBASE_RT_DB_TAG, "pull method execution started");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> content = (Map<String, Object>) snapshot.getValue();
                if (content == null) {
                    content = new ConcurrentHashMap<>(0);
                }

                // De-serialization of the object
                Map<String, T> deserializedContent = new ConcurrentHashMap<>(); // data form DB
                Gson gson = new Gson();
                for (Map.Entry<String, Object> aTuple : content.entrySet()) {
                    String json = gson.toJson(aTuple.getValue());
                    deserializedContent.put(aTuple.getKey(), gson.fromJson(json, (Type) getDbEntityClass()));
                }

                Objects.requireNonNull(onSuccess).accept(deserializedContent.values());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onError != null) {
                    onError.run();
                }
            }
        });

        Log.d(FIREBASE_RT_DB_TAG, "push method execution terminated");
    }


    @Override
    public void observeDBChanges(
            Consumer<T> onCreated, Consumer<T> onRemoved, Consumer<T> onUpdated) {
        // The listener to this table, needed to listen for changes and get the data.
        // The system is thought for asynchronous operations.

        tableListener = new ChildEventListener() {
            private void logDataChangedInfo() {
                Log.i(FIREBASE_RT_DB_TAG, "Data changed.");
            }

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(FIREBASE_RT_DB_TAG, "New tuple: " + changedContent);
                    onCreated.accept(changedContent);
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(FIREBASE_RT_DB_TAG, "Updated tuple: " + changedContent);
                    onUpdated.accept(changedContent);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(FIREBASE_RT_DB_TAG, "Removed tuple: " + changedContent);
                    onRemoved.accept(changedContent);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    T changedContent = snapshot.getValue(getDbEntityClass());
                    Log.i(FIREBASE_RT_DB_TAG, "Moved tuple: " + changedContent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(FIREBASE_RT_DB_TAG, "EventListener CANCELLED", error.toException());
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
                        Log.d(FIREBASE_RT_DB_TAG, "remove successfully completed");
                    } else {
                        Log.e(FIREBASE_RT_DB_TAG, "error with remove", task.getException());
                    }
                });
    }

}
