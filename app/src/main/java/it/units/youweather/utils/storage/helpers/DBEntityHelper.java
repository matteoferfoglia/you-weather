package it.units.youweather.utils.storage.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.storage.entities.DBEntity;

/**
 * Interface for an entity of a database (i.e., a table in the relational
 * database model).
 *
 * @param <T> The generic for the class representing an entity in the database.
 * @author Matteo Ferfoglia
 */
interface DBEntityHelper<T extends DBEntity> {

    /**
     * Add new instance for the entity associated with the {@link DBEntity} to be added
     * to the database.
     * The method assigns an unique identifier to the entity after it has been added.
     * The method also starts to observe the object if it is updated by the application
     * and, if changes are observed, they are propagated to the database.
     * If changes are observed before the unique identifier is assigned, those changes
     * will be propagated to the database after the unique identifier assignment.
     *
     * @param newTuple  The new instance to be added.
     * @param onSuccess The {@link Runnable} to be run if the new instance is
     *                  successfully added to the database.
     * @param onError   The {@link Runnable} to be run in case of error.
     */
    void push(DBEntity newTuple, Runnable onSuccess, Runnable onError);

    /**
     * Like {@link #push(DBEntity, Runnable, Runnable)}, but no
     * custom {@link Runnable}s to be run in case of success or
     * error are provided.
     *
     * @param newTuple The new object to be added to the database.
     */
    default void push(DBEntity newTuple) {
        push(newTuple, null, null);
    }


    /**
     * Retrieve all {@link DBEntity tuples} for this entity.
     * The method assigns an unique identifier to each entity after it has been retrieved.
     * The method also starts to observe each object if it is updated by the application
     * and, if changes are observed, they are propagated to the database.
     *
     * @param onSuccess The {@link Consumer} that will accept all the retrieved entities
     *                  in case of success.
     * @param onError   The {@link Runnable} to be run in case of error.
     */
    void pull(@NonNull Consumer<Collection<T>> onSuccess, @Nullable Runnable onError);

    /**
     * This method makes the instance to start to observe the entity associated
     * with the generic of the instance in the database and take actions if any
     * tuple of the entity in the database changes.
     *
     * @param onCreated The action to be performed if a new tuple of the entity is created.
     * @param onRemoved The action to be performed if a tuple of the entity is removed.
     * @param onUpdated The action to be performed if a tuple of the entity is updated.
     */
    void observeDBChanges(Consumer<T> onCreated, Consumer<T> onRemoved, Consumer<T> onUpdated);

    /**
     * This method makes the database forget of the given tuple: any operation
     * on the given tuple will not be tracked into the database after this method
     * is executed.
     *
     * @param tuple The tuple to be forgotten.
     */
    void forget(DBEntity tuple);

    /**
     * This method invokes {@link #forget(DBEntity)} on the given parameter
     * and then remove it from the database, too.
     *
     * @param tuple The tuple to be removed from the database.
     */
    void remove(DBEntity tuple);

}
