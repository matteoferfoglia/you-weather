package it.units.youweather.utils.functionals;

/**
 * This interface emulates the interface Predicate available from Java 8.
 *
 * @param <T> The generic for the type on which the predicate applies.
 */
public interface Predicate<T> {
    boolean test(T t);
}
