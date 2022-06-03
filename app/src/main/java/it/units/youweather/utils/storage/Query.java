package it.units.youweather.utils.storage;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class to create a query for the database.
 * Let <em>T</em> be the (generic for the) type of a field of
 * an entity to query in the database.
 * Supported queries are <em>range queries</em>.
 * If you can force results to have a given field value included
 * between a given minimum and a given maximum (both minimum and
 * maximum values included) or to be exactly a given value: use the
 * appropriate constructor.
 *
 * @param <T> generic for the field of an entity to query in the database.
 * @author Matteo Ferfoglia
 */
public class Query<T> {

    private final T minValueInclusive;
    private final T maxValueInclusive;
    private final Field field;

    /**
     * Array of valid classes for the field on which to perform the query.
     */
    private static final Class<?>[] validClassesForField =
            {String.class, Double.class, Boolean.class};

    /**
     * @param field             The {@link Field} on which the range query must be evaluated.
     * @param minValueInclusive The minimum value in results for the specified field.
     * @param maxValueInclusive The minimum value in results for the specified field.
     */
    public Query(@NonNull Field field, @NonNull T minValueInclusive, @NonNull T maxValueInclusive) {
        this.field = Objects.requireNonNull(field);
        boolean validFieldClassForQuery = false;
        for (Class<?> clazz : validClassesForField) {
            validFieldClassForQuery = validFieldClassForQuery || clazz.isInstance(minValueInclusive); // minValueInclusive instanceof clazz
        }


        if (!validFieldClassForQuery) {
            throw new UnsupportedOperationException("Valid classes for field to query are "
                    + Arrays.toString(validClassesForField) + " but found " + minValueInclusive.getClass());
        } else {
            if (((Comparable<T>) Objects.requireNonNull(minValueInclusive)) // all valid classes for queries implement Comparable
                    .compareTo(Objects.requireNonNull(maxValueInclusive)) <= 0) {
                this.minValueInclusive = Objects.requireNonNull(minValueInclusive);
                this.maxValueInclusive = Objects.requireNonNull(maxValueInclusive);
            } else {
                throw new IllegalArgumentException("Given minimum value must be lower or equal than the maximum value");
            }
        }
    }

    /**
     * Like {@link #Query(Field, Object, Object)}, but minimum and
     * maximum values are equal and are specified in the parameter.
     */
    public Query(@NonNull Field field, @NonNull T exactValue) {
        this(field, exactValue, exactValue);
    }

    public T getMinValueInclusive() {
        return minValueInclusive;
    }

    public T getMaxValueInclusive() {
        return maxValueInclusive;
    }

    public Field getField() {
        return field;
    }

    @NonNull
    @Override
    public String toString() {
        return "Query{" +
                "minValueInclusive=" + minValueInclusive +
                ", maxValueInclusive=" + maxValueInclusive +
                '}';
    }

}
