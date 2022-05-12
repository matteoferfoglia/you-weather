package it.units.youweather.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

/**
 * This class is used as helper to allow static method or variables to access
 * Android application resources.
 * The idea is to use the singleton pattern to allocate an {@link AppCompatActivity}
 * to be used for accessing Android application resources.
 * If you want to use this class in an activity, this class must be initialized
 * (by invoking method {@link #initialize(Context)}) fro that class.
 */
public abstract class ActivityStaticResourceHandler extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak") // Saves the Application Context
    private volatile static Context _context;

    /**
     * Register this class for the activity whose {@link Context} is passed
     * as argument.
     *
     * @param context The context of the activity to be registered for this class.
     */
    public static void initialize(@NonNull Context context) {
        _context = Objects.requireNonNull(context).getApplicationContext();
    }

    /**
     * @return the {@link Context} for the activity for which this class was registered,
     * i.e., the activity that invoked {@link #initialize(Context)}.
     */
    public static Context getAppContext() {
        try {
            return Objects.requireNonNull(_context, "Method "
                    + ActivityStaticResourceHandler.class.getMethod("initialize", Context.class)
                    + " was not invoked by the current activity.");
        } catch (NoSuchMethodException e) {
            throw new NullPointerException();
        }
    }

    /**
     * @param resId The id of the string resource to retrieve.
     * @return a string resource known from this {@link Context}.
     */
    public static String getResString(@StringRes int resId) {
        return getAppContext().getString(resId);
    }

}
