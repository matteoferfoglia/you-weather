package it.units.youweather.utils.functionals;

/**
 * This interface emulates the interface Function available from Java 8.
 *
 * @param <S> The type of the input parameter.
 * @param <T> The type of the output parameter.
 */
public interface Function<S, T> {
    T apply(S s);
}
