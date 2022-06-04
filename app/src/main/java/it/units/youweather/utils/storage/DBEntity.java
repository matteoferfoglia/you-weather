package it.units.youweather.utils.storage;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for entities that can be saved into the DB.
 * Derived class must call the constructor of this class
 * (super) in order to be registered and eligible to be
 * used as database entities.
 *
 * @author Matteo Ferfoglia
 */
public abstract class DBEntity implements Serializable {

    /**
     * TAG for logger.
     */
    private static final String TAG = DBEntity.class.getSimpleName();

    /**
     * Saves the classes that are registered for the DB.
     */
    protected static final List<Class<?>> registeredClasses = new ArrayList<>();
    /**
     * The unique identifier required for the database.
     */
    private volatile String id;

    /**
     * No-args constructor.
     */
    protected DBEntity() {
        registerThisClassForDB(getClass());
    }

    /**
     * Getter for {@link #id}.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for {@link #id}.
     */
    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();


    /**
     * Register this class to be used with the database.
     */
    public static void registerThisClassForDB(@NonNull Class<? extends DBEntity> clazz) {
        if (!registeredClasses.contains(clazz)) {   // TODO: try to remove this method
            DBHelper.registerEntityClass(clazz,
                    createdEntity -> Log.d(TAG, "CREATED " + createdEntity),
                    removedEntity -> Log.d(TAG, "REMOVED " + removedEntity),
                    updatedEntity -> Log.d(TAG, "UPDATED " + updatedEntity));
            registeredClasses.add(clazz);
        }
    }
}
