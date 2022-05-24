package it.units.youweather.utils.storage.helpers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.storage.entities.DBEntity;
import it.units.youweather.utils.storage.helpers.firebase_rtdb.FirebaseRTDBEntityAdapter;

/**
 * Helper to use to interface the app with the database.
 * The {@link DBEntityAdapter} to use is specified in the method
 * {@link #registerEntityClass(Class, Consumer, Consumer, Consumer)}.
 *
 * @author Matteo Ferfoglia
 */
public class DBHelper {

    /**
     * {@link ConcurrentMap} containing all the tables of the database.
     */
    private static final ConcurrentMap<Class<? extends DBEntity>, DBEntityHelper<? extends DBEntity>> entities =
            new ConcurrentHashMap<>();

    /**
     * @param entityClass The {@link Class} corresponding to the entity
     *                    in the database for which this adapter instance
     *                    is created.
     * @return the {@link DBEntityHelper} for the {@link DBEntity} passed as parameter.
     */
    private static <T extends DBEntity> DBEntityHelper<T> getInstance(Class<T> entityClass) {
        DBEntityHelper<T> instance = (DBEntityHelper<T>) entities.get(entityClass);
        if (instance == null) {
            try {
                throw new UnregisteredEntity(entityClass);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /**
     * Register the given class to be used as an entity of the database.
     *
     * @param entityClass The class to be registered.
     * @param onCreated   The action to be performed if a new tuple of the entity is created in the database.
     * @param onRemoved   The action to be performed if a tuple of the entity is removed from the database.
     * @param onUpdated   The action to be performed if a tuple of the entity is updated in the database.
     */
    public static <T extends DBEntity> void registerEntityClass(Class<T> entityClass,
                                                                Consumer<T> onCreated,
                                                                Consumer<T> onRemoved,
                                                                Consumer<T> onUpdated) {
        DBEntityHelper<T> instance =
                new FirebaseRTDBEntityAdapter<>(entityClass, onCreated, onRemoved, onUpdated);
        entities.put(entityClass, instance);
    }

    /**
     * See {@link DBEntityHelper#push(DBEntity, Runnable, Runnable)}.
     */
    public static void push(DBEntity newTuple, Runnable onSuccess, Runnable onError) {
        getInstance(Objects.requireNonNull(newTuple).getClass())
                .push(newTuple, onSuccess, onError);
    }

    /**
     * See {@link DBEntityHelper#push(DBEntity)}.
     */
    public static void push(DBEntity newTuple) {
        getInstance(Objects.requireNonNull(newTuple).getClass()).push(newTuple);
    }

    /**
     * See {@link DBEntityHelper#forget(DBEntity)} (DBEntity)}.
     */
    public static void forget(DBEntity newTuple) throws UnregisteredEntity {
        getInstance(Objects.requireNonNull(newTuple).getClass()).forget(newTuple);
    }

    /**
     * See {@link DBEntityHelper#remove(DBEntity)} (DBEntity)} (DBEntity)}.
     */
    public static void remove(DBEntity newTuple) throws UnregisteredEntity {
        getInstance(Objects.requireNonNull(newTuple).getClass()).remove(newTuple);
    }

    /**
     * Exception to be thrown if a {@link DBEntity} class has been used
     * as entity class without initializing it before.
     */
    private static class UnregisteredEntity extends RuntimeException {
        UnregisteredEntity(Class<? extends DBEntity> entityClass) throws NoSuchMethodException {
            super("The class " + entityClass.getCanonicalName() + " is not registered as DB entity. " +
                    " Make sure to have invoked the method "
                    + DBHelper.class.getMethod("registerEntityClass",
                    Class.class, Consumer.class, Consumer.class, Consumer.class).getName()
                    + " to register the entity class before using it.");
        }
    }

}
