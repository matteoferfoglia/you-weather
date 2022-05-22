package it.units.youweather.utils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * This class provides utility methods for JSON de/serialization.
 *
 * @author Matteo Ferfoglia
 */
public abstract class JsonHelper {

    private static final Gson gsonSingleInstance = new Gson();

    /**
     * Converts a JSON serialized object (provided as {@link String}
     * into the Java object.
     *
     * @param <T>               The generic type for the Java object.
     * @param jsonEncoded       The {@link String} encoding the object.
     * @param deserializedClass The {@link Class} for the deserialized object.
     * @return The deserialized object.
     */
    public static <T> T fromJson(@NonNull String jsonEncoded, @NonNull Class<T> deserializedClass) {
        return gsonSingleInstance.fromJson(
                Objects.requireNonNull(jsonEncoded), Objects.requireNonNull(deserializedClass));
    }

    /**
     * Converts a JSON serialized object (provided as {@link String}
     * into the Java object.
     *
     * @param <T>              The generic type for the Java object.
     * @param jsonEncoded      The {@link String} encoding the object.
     * @param deserializedType The {@link Class} for the deserialized object.
     * @return The deserialized object.
     */
    public static <T> T fromJson(@NonNull String jsonEncoded, @NonNull Type deserializedType) {
        return gsonSingleInstance.fromJson(
                Objects.requireNonNull(jsonEncoded), Objects.requireNonNull(deserializedType));
    }

    /**
     * Converts a Java object into a {@link String} corresponding to its JSON
     * representation.
     *
     * @param <T>        The generic type for the Java object.
     * @param javaObject The Java object to be serialized into JSON format.
     * @return The {@link String} which is the JSON encoding for the input Java object.
     */
    public static <T> String toJson(@NonNull T javaObject) {
        return gsonSingleInstance.toJson(Objects.requireNonNull(javaObject));
    }

}
