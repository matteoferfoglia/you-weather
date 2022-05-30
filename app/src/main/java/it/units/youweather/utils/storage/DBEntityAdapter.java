package it.units.youweather.utils.storage;

import java.util.Objects;

import it.units.youweather.utils.functionals.Consumer;

/**
 * Adapter to an entity of the database.
 * This class provides methods to manipulate the data in the database.
 */
public abstract class DBEntityAdapter<T extends DBEntity> implements DBEntityHelper<T> {

    /**
     * Saves the {@link Class} for the entity stored in the database.
     */
    private final Class<T> dbEntityClass;

    /**
     * Constructor.
     *
     * @param onCreated The action to be performed if a new tuple of the entity is created in the database.
     * @param onRemoved The action to be performed if a tuple of the entity is removed from the database.
     * @param onUpdated The action to be performed if a tuple of the entity is updated in the database.
     */
    protected DBEntityAdapter(Class<T> entityClass,
                              Consumer<T> onCreated, Consumer<T> onRemoved, Consumer<T> onUpdated) {
        this.dbEntityClass = Objects.requireNonNull(entityClass);
        observeDBChanges(onCreated, onRemoved, onUpdated);
    }

    /**
     * Getter for {@link #dbEntityClass}.
     */
    protected Class<T> getDbEntityClass() {
        return dbEntityClass;
    }
}
