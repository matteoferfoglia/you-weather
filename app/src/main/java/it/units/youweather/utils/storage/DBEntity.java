package it.units.youweather.utils.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for entities that can be saved into the DB.
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
    protected static final List<Class<?>> registeredClasses = new ArrayList<>();
    /**
     * The unique identifier required for the database.
     */
    private volatile String id;

    /**
     * No-args constructor.
     */
    protected DBEntity() {
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
