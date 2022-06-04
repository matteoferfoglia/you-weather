package it.units.youweather.utils;

import android.app.Activity;

public abstract class Utility {
    public static void runOnUiThread(Activity activity, Runnable runnable) {
        try {
            activity.runOnUiThread(runnable);
        } catch (Exception e) { // if user has already changed activity, an exception might raise
            System.err.println("runOnUiThread failed, but this is not a problem. The error is: " + e);
        }
    }
}
