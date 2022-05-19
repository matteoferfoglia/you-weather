package it.units.youweather.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import it.units.youweather.utils.functionals.Function;

/**
 * Utility class.
 *
 * @author Matteo Ferfoglia
 */
public class Utility {

    /**
     * Given an array, this methods returns an array of {@link String}s
     * composed by the {@link String} obtained by invoking {@link Object#toString()}
     * on each element of the input array, keeping the same order.
     *
     * @param array The input array.
     * @param <T>   The generic for each element of the input array.
     * @return The {@link String} array obtained from {@link Object#toString()}
     * invoked on each element of the input array.
     */
    public static <T> String[] toStringArray(@NonNull T[] array) {
        String[] strings = new String[Objects.requireNonNull(array).length];
        for (int i = 0; i < array.length; i++) {
            strings[i] = Objects.requireNonNull(array[i]).toString();
        }
        return strings;
    }

    /**
     * Maps each element of the input array into elements of the output (returned) array.
     *
     * @param inputArray The input array.
     * @param mapper     The mapper to map each element of the input array into an element
     *                   of the output array.
     * @param <T>        The generic for each element of the output array
     * @param <S>        The generic for each element of the input array.
     * @return the mapped array or null if the array is empty.
     */
    @Nullable
    public static <S, T> T[] map(@NonNull S[] inputArray, @NonNull Function<S, T> mapper) {
        Objects.requireNonNull(mapper);
        ArrayList<T> outputArrayList = new ArrayList<>();
        for (S s : inputArray) {
            outputArrayList.add(mapper.apply(s));
        }
        Object[] tmp = outputArrayList.toArray();
        T[] outputArray;
        if (tmp.length > 0) {
            outputArray = (T[]) Array.newInstance(tmp[0].getClass(), tmp.length);
            for (int i = 0; i < tmp.length; i++) {
                outputArray[i] = (T) tmp[i];
            }
        } else {
            outputArray = null;
        }
        return outputArray;
    }
}
