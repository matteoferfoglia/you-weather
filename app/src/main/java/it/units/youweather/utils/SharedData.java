package it.units.youweather.utils;

import android.annotation.SuppressLint;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This class is an helper for saving shared data for the application.
 * Data are saved in a static {@link Map} and are lost when the application ends.
 */
@SuppressWarnings("unchecked")  // de/serialization of saved data can require unchecked cast
public abstract class SharedData {

    private static final ConcurrentMap<SharedDataName, Pair<Class<?>, ?>> sharedDate
            = new ConcurrentHashMap<>();

    /**
     * Enumeration of valid shared data names.
     * Shared data are saved in a map, whose valid keys are only ones
     * belonging to this enum.
     * Using an enumeration allows to know all data that can be saved.
     */
    public enum SharedDataName {
        /**
         * Saves the currently signed-in user or null if no user is currently signed in.
         */
        LOGGED_IN_USER,

        /**
         * Saves true if the checkbox for date filtering in fragment
         * {@link it.units.youweather.ui.logged_in_area.UserPageWithHistoryFragment}
         * is currently checked, false otherwise.
         */
        USER_PAGE_WITH_HISTORY_FRAGMENT_FILTER_DATE_CHECKED,

        /**
         * Saves the minimum date (the "from-date") set for filtering in fragment
         * {@link it.units.youweather.ui.logged_in_area.UserPageWithHistoryFragment}.
         */
        USER_PAGE_WITH_HISTORY_FRAGMENT_FILTER_DATE_FROM,

        /**
         * Saves the minimum date (the "to-date") set for filtering in fragment
         * {@link it.units.youweather.ui.logged_in_area.UserPageWithHistoryFragment}.
         */
        USER_PAGE_WITH_HISTORY_FRAGMENT_FILTER_DATE_TO,

        /**
         * Saves the user's last known position.
         */
        USER_LAST_KNOWN_POSITION,
    }

    /**
     * Retrieves a shared data.
     *
     * @param sharedDataName The name for the shared data to retrieve.
     * @return The desired property value, or null if absent.
     */
    public static <T> T getValue(@NonNull SharedDataName sharedDataName) {

        synchronized (sharedDate) {

            Pair<Class<?>, ?> preferenceValue_class_value = sharedDate.get(sharedDataName);
            if (preferenceValue_class_value != null) {
                Class<?> clazz = preferenceValue_class_value.first;
                return (T) clazz.cast(preferenceValue_class_value.second);
            } else {
                return null;
            }

        }
    }

    /**
     * Saves a shared data.
     *
     * @param sharedDataName  The name for the shared preference to save.
     * @param preferenceValue The value for the shared preference to save (or update).
     * @param <T>             The generic for the type of the value to set.
     */
    @SuppressLint("ApplySharedPref")    // synchronous (blocking operation) update
    public static <T> void setValue(@NonNull SharedDataName sharedDataName,
                                    @NonNull T preferenceValue) {

        Class<T> clazz = (Class<T>) Objects.requireNonNull(preferenceValue).getClass();
        synchronized (sharedDate) {
            sharedDate.put(
                    sharedDataName,
                    new Pair<>(clazz, preferenceValue));
        }
    }

    /**
     * Like {@link #setValue(SharedDataName, Object)}, but using this
     * method, the given value is set <strong>only if</strong> no value is
     * saved for the given name.
     */
    public static <T> void setValueIfAbsent(@NonNull SharedDataName sharedDataName,
                                            @NonNull T preferenceValue) {
        synchronized (sharedDate) {
            if (!sharedDate.containsKey(sharedDataName)) {
                setValue(sharedDataName, preferenceValue);
            }
        }
    }

    /**
     * Removes a shared preference.
     *
     * @param sharedDataName The name for the shared preference to remove.
     */
    @SuppressLint("ApplySharedPref")    // synchronous (blocking operation) update
    public static void removeValue(@NonNull SharedDataName sharedDataName) {
        synchronized (sharedDate) {
            sharedDate.remove(sharedDataName);
        }
    }

}
