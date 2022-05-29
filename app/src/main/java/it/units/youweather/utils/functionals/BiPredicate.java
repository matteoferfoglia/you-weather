package it.units.youweather.utils.functionals;

/**
 * This interface emulates the interface BiPredicate available from Java 8.
 *
 * @param <S> The generic for the type of the first argument on which the predicate applies.
 * @param <T> The generic for the type of the first argument on which the predicate applies.
 */
public interface BiPredicate<S, T> {
    boolean test(S s, T t);
}
