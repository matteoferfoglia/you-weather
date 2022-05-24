package it.units.youweather.utils.storage.entities;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.units.youweather.utils.storage.helpers.DBHelper;

/**
 * Base class for entities that can be saved into the DB.
 * Derived class must be invoke the "super" constructor if
 * derived objects have to be saved into te DB.
 *
 * @author Matteo Ferfoglia
 */
public abstract class DBEntity {

    /**
     * TAG for logger.
     */
    private static final String TAG = DBEntity.class.getSimpleName();

    /**
     * Saves the classes that are registered for the DB.
     */
    private static final List<Class<?>> registeredClasses = new ArrayList<>();

    /**
     * The unique identifier required for the database.
     */
    private volatile String id;

    /**
     * No-args constructor.
     */
    protected DBEntity() {
        if (!registeredClasses.contains(getClass())) {
            DBHelper.registerEntityClass(getClass(),
                    arg -> Log.d(TAG, "CREATED " + this),
                    arg -> Log.d(TAG, "REMOVED " + this),
                    arg -> Log.d(TAG, "UPDATED " + this));
            registeredClasses.add(getClass());
        }
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
}
