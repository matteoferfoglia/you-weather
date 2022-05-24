package it.units.youweather.utils.storage.helpers.firebase_rtdb;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import it.units.youweather.utils.Timing;
import it.units.youweather.utils.storage.entities.DBEntity;

/**
 * Registers the instances to be synchronized with the real time database.
 * This class defines a {@link Thread} that periodically check for changes
 * in any of the registered instances, and if any change happened, they are
 * reported into the real time database.
 * Changes are detected according to the hash of the object obtained from
 * {@link DBEntity#hashCode()}, this means that, if the method is not
 * correctly implemented, changes might be not detected.
 *
 * @author Matteo Ferfoglia
 */
class FirebaseRTDBSynchronizer {

    /**
     * The period at which the task synchronize local changes with the database.
     */
    final int SCHEDULING_PERIOD_IN_MILLIS = 2000;

    /**
     * The TAG for the logger.
     */
    private final String TAG = FirebaseRTDBSynchronizer.class.getSimpleName();

    /**
     * Saves all the registered entities in a {@link Map}, having the entity
     * as key and its has as value.
     */
    private final Map<DBEntity, Integer> registeredEntities =
            new LinkedHashMap<>();

    /**
     * The {@link ScheduledFuture} to use for cancel the periodic task of this class.
     */
    private ScheduledFuture<?> completion = null;

    /**
     * The {@link DatabaseReference} with wich the synchronization must be done.
     */
    private volatile DatabaseReference dbRef = null;

    /**
     * Constructor.
     *
     * @param dbRef The {@link DatabaseReference} with wich the synchronization must be done.
     */
    FirebaseRTDBSynchronizer(DatabaseReference dbRef) {

        this.dbRef = Objects.requireNonNull(dbRef);

        // Create the scheduler that will periodically check for changes
        final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);
        completion = scheduler.scheduleAtFixedRate(
                this::synchronizeWithRealTimeDB,
                SCHEDULING_PERIOD_IN_MILLIS, SCHEDULING_PERIOD_IN_MILLIS, TimeUnit.MILLISECONDS);

        Log.i(TAG, "Created");
    }

    /**
     * Starts observing the entity passed as parameter.
     *
     * @param newEntity The entity to start observing.
     */
    void observeNewEntity(DBEntity newEntity) {
        synchronized (registeredEntities) {
            registeredEntities.put(Objects.requireNonNull(newEntity), newEntity.hashCode());
        }
        Log.v(TAG, "Observing new entity: " + newEntity);
    }

    /**
     * Stops observing the entity passed as parameter.
     *
     * @param entity The entity to stop observing.
     */
    void stopObserve(DBEntity entity) {
        synchronized (registeredEntities) {
            registeredEntities.remove(Objects.requireNonNull(entity));
        }
        Log.v(TAG, "Stop observing new entity: " + entity);
    }

    /**
     * Check for changes and report them to the online database,
     * as described in the {@link FirebaseRTDBSynchronizer class}
     * documentation.
     */
    void synchronizeWithRealTimeDB() {
        Log.v(TAG, "Starting synchronization at " + Timing.getCurrentLocaleDateTime());
        for (Map.Entry<DBEntity, Integer> entityAndItsHash : registeredEntities.entrySet()) {
            synchronized (registeredEntities) {
                DBEntity entity = entityAndItsHash.getKey();
                int currentHash = entity.hashCode();
                if (entityAndItsHash.getValue() != currentHash) {
                    dbRef.child(entity.getId()).setValue(entity)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Update DB with entity " + entity);
                                } else {
                                    Log.e(TAG, "Error while trying to update DB with entity "
                                            + entity, task.getException());
                                }
                            });
                    entityAndItsHash.setValue(currentHash);
                }
            }
        }
    }

    /**
     * Stops the periodic task and invokes {@link #synchronizeWithRealTimeDB()}
     * one last time.
     */
    public synchronized void finalize() {
        Log.i(TAG, "Finalize " + this);
        completion.cancel(false);
        synchronizeWithRealTimeDB();
    }

    @NonNull
    @Override
    public String toString() {
        return "FirebaseRTDBSynchronizer{dbRef=" + dbRef + '}';
    }
}
