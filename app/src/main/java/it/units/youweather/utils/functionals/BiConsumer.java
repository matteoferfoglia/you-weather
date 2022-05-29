package it.units.youweather.utils.functionals;

/**
 * This interface emulates the interface Consumer available from Java 8.
 *
 * @param <S> The generic for the type of the firs argument.
 * @param <T> The generic for the type of the second argument.
 */
public interface BiConsumer<S, T> {
    void accept(S a, T b);
}
