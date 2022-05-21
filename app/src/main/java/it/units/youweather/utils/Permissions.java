package it.units.youweather.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Utility class for permissions handling (security).
 *
 * @author Matteo Ferfoglia
 */
public abstract class Permissions {
    /**
     * If the input array is non-empty, this method requests the
     * necessary permissions.
     *
     * @param permissions The necessary permissions.
     */
    public static void requestPermissionsForActivityIfNecessary(
            @NonNull String[] permissions, @NonNull Activity activity) {
        Objects.requireNonNull(permissions);
        Objects.requireNonNull(activity);

        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) { // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Check if permissions are granted for the given {@link Activity}.
     *
     * @param neededPermissions array of the needed permissions.
     * @param activity          The {@link Activity}.
     * @return true if all permissions in the given array are granted for the given activity.
     */
    public static boolean arePermissionsGrantedForActivity(
            @NonNull String[] neededPermissions, @NonNull Activity activity) {
        Objects.requireNonNull(neededPermissions);
        Objects.requireNonNull(activity);

        boolean permissionsGranted = true;
        for (String permission : neededPermissions) {
            permissionsGranted = permissionsGranted
                    && ActivityCompat.checkSelfPermission(Objects.requireNonNull(activity), permission)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return permissionsGranted;
    }

    /**
     * Exception thrown if any permission is missing.
     */
    @SuppressWarnings({"serial", "RedundantSuppression"})
    // IDE says "serial" warning does not need to be suppressed but Lint disagrees
    public static class MissingPermissionsException extends Throwable {
        MissingPermissionsException() {
            super("Missing required permissions");
        }
    }
}
