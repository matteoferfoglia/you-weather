package it.units.youweather.utils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A {@link android.content.SharedPreferences} object points to a file
 * containing key-value pairs and provides simple methods to read and write them.
 * This utility class provides an uniform interface to use
 * {@link android.content.SharedPreferences}
 */
public class SharedPreferences {

    private static final String TAG = SharedPreferences.class.getSimpleName();

    private static final Gson gson = new Gson();

    private static final String SHARED_PREFERENCES_NAME =
            SharedPreferences.class.getCanonicalName(); // name that's uniquely identifiable to this app

    /**
     * Enumeration of valid shared preferences.
     * Using an enumeration allows to know all preferences that can be saved.
     */
    public enum SharedPreferenceName {
        /**
         * Saves the currently signed-in user or null if no user is currently signed in.
         */
        LOGGED_IN_USER,
    }

    /**
     * Retrieves a shared preference.
     *
     * @param applicationContext   The {@link Context application context}.
     * @param sharedPreferenceName The name for the shared preference to retrieve.
     * @return The desired property value, or null if absent.
     */
    public static <T> T getValue(@NonNull Context applicationContext,
                                 @NonNull SharedPreferenceName sharedPreferenceName) {
        String preferenceValueAsJson = Objects.requireNonNull(applicationContext)
                .getSharedPreferences(
                        Objects.requireNonNull(SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE)
                .getString(sharedPreferenceName.name(), null/*default value if absent*/);
        if(preferenceValueAsJson!=null) {

            @SuppressWarnings("unchecked")
            Pair<String, String> preferenceValue_class_value = (Pair<String, String>)
                    gson.fromJson(preferenceValueAsJson, Pair.class);
            try {
                Class<?> clazz = Class.forName(preferenceValue_class_value.first);
                return gson.fromJson(preferenceValue_class_value.second, (Type) clazz);
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Error in class detection", e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Saves a shared preference.
     *
     * @param applicationContext   The {@link Context application context}.
     * @param sharedPreferenceName The name for the shared preference to save.
     * @param preferenceValue      The value for the shared preference to save (or update).
     * @param <T>                  The generic for the type of the value to set.
     */
    public static <T> void setValue(@NonNull Context applicationContext,
                                    @NonNull SharedPreferenceName sharedPreferenceName,
                                    @NonNull T preferenceValue) {
        String classCanonicalName = Objects.requireNonNull(preferenceValue).getClass().getCanonicalName();
        Objects.requireNonNull(applicationContext)
                .getSharedPreferences(
                        Objects.requireNonNull(SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE)
                .edit()
                .putString(
                        sharedPreferenceName.name(),
                        gson.toJson(new Pair<>(classCanonicalName, gson.toJson(preferenceValue))))
                .apply(); // asynchronous save, use commit() for blocking operation
    }

    /**
     * Removes a shared preference.
     *
     * @param applicationContext   The {@link Context application context}.
     * @param sharedPreferenceName The name for the shared preference to remove.
     */
    public static void removeValue(@NonNull Context applicationContext,
                                   @NonNull SharedPreferenceName sharedPreferenceName) {
        Objects.requireNonNull(applicationContext)
                .getSharedPreferences(
                        Objects.requireNonNull(SHARED_PREFERENCES_NAME), Context.MODE_PRIVATE)
                .edit()
                .remove(sharedPreferenceName.name())
                .apply(); // apply() for asynchronous save, use commit() for blocking operation
    }

}
