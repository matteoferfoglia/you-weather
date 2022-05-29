package it.units.youweather.utils.functionals;

/**
 * This interface emulates the interface Supplier available from Java 8.
 *
 * @param <T> The generic for the type that the supplier will return.
 */
public interface Supplier<T> {
    T get();
}