package it.units.youweather.utils;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.utils.functionals.Consumer;

/**
 * This class supports geo-location features, like getting
 * user's current location.
 * An instance of this class refers to a single {@link Activity}.
 *
 * @author Matteo Ferfoglia
 */
public class LocationHelper {

    /**
     * TAG for logger.
     */
    private static final String TAG = LocationHelper.class.getSimpleName();

    /**
     * Mutex for concurrency: used for asynchronous thread notifications.
     */
    private final Object lock = new Object();

    /**
     * Current user's location.
     */
    private volatile LocationContainer userCurrentLocation = new LocationContainer();

    /**
     * Refresh time (seconds) for current user's location.
     */
    private final static int LOCATION_REFRESH_TIME = 15000; // 15 seconds to update
    /**
     * Refresh distance (meters) for current user's location.
     */
    final static int LOCATION_REFRESH_DISTANCE = 5000;       // 500 meters to update

    /**
     * {@link LocationListener} if user's location change.
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.i(TAG, "Location changed: lat="
                    + location.getLatitude() + ", long=" + location.getLongitude());
            synchronized (lock) {
                userCurrentLocation.setLocation(Objects.requireNonNull(location));
                lock.notifyAll();
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.i(TAG, "Location provider (" + provider + ") enabled");
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.i(TAG, "Location provider (" + provider + ") disabled");
            synchronized (lock) {
                userCurrentLocation.setLocation(null);
                lock.notifyAll();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * Constructor.
     *
     * @param activity The activity for which this instance will work.
     */
    @SuppressLint("MissingPermission")  // checked in if-statement
    public LocationHelper(@NonNull Activity activity) throws Permissions.MissingPermissionsException {

        String[] neededPermissions = new String[]{   // array of needed permissions
                Manifest.permission.ACCESS_FINE_LOCATION,   // for getting user's current position
                Manifest.permission.WRITE_EXTERNAL_STORAGE  // for showing the map
        };
        Permissions.requestPermissionsForActivityIfNecessary(
                neededPermissions, Objects.requireNonNull(activity));

        // Set the LocationManager to updates (asynchronously) the user location
        if (Permissions.arePermissionsGrantedForActivity(neededPermissions, activity)) {
            ((LocationManager) activity.getSystemService(LOCATION_SERVICE))
                    .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        } else {
            throw new Permissions.MissingPermissionsException();
        }

    }

    /**
     * @return the user's current location.
     * @throws UnavailablePositionException If the position is unavailable.
     */
    public Coordinates getUserCurrentLocation() throws UnavailablePositionException {
        return new Coordinates(
                userCurrentLocation.getLatitude(),
                userCurrentLocation.getLongitude());
    }

    /**
     * Permissions thrown when {@link #userCurrentLocation} is null
     */
    private static class UnavailablePositionException extends Throwable {
        UnavailablePositionException() {
            super();
        }
    }

    /**
     * Add a listener for the position change.
     *
     * @param onChange The {@link Runnable} to be executed each time the position changes.
     * @return a {@link Stoppable} object: if its {@link Stoppable#stop()}
     * method is invoked, the thread invoking this "wait" procedure will
     * stop to listen to position change.
     */
    public Stoppable addPositionChangeListener(@NonNull Consumer<Location> onChange) {   // TODO: test

        final LocationContainer lastUpdateOfCurrentLocationContainer =
                new LocationContainer(userCurrentLocation.getCopyOfLocation());

        final AtomicBoolean isStillListening = new AtomicBoolean(true);

        Thread listener = new Thread(() -> {
            while (isStillListening.get()) { // loop until the listener is still listening for changes   // TODO: test and refactor
                synchronized (lock) {
                    while (Objects.equals(lastUpdateOfCurrentLocationContainer, userCurrentLocation)) {
                        try {
                            lock.wait(); // notification should be provided by the entities that updates the location
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Interrupted thread.");
                        }
                    }
                    Log.d(TAG, "Location changed, thread "
                            + Thread.currentThread() + " notified.");
                }

                // location changed
                Location newLocation =  userCurrentLocation.getCopyOfLocation();
                lastUpdateOfCurrentLocationContainer.setLocation(newLocation);
                Objects.requireNonNull(onChange).accept(newLocation);
            }
        });

        listener.start();

        return () -> {
            listener.interrupt();
            isStillListening.set(false);
            return true;
        };
    }

    /**
     * Class containing the {@link Location}.
     * The container is needed for the field for multi-thread
     * synchronization issues: the container can be final, the
     * inner {@link Location} cannot be final.
     */
    private static class LocationContainer {
        private volatile Location location = null;

        public LocationContainer() {
        }

        public LocationContainer(@Nullable Location location) {
            this();
            setLocation(location);
        }

        @Nullable
        public Location getCopyOfLocation() {
            return location == null ? null : new Location(location);
        }

        public double getLatitude() throws UnavailablePositionException {
            if (location == null) {
                throw new UnavailablePositionException();
            } else {
                return location.getLatitude();
            }
        }

        public double getLongitude() throws UnavailablePositionException {
            if (location == null) {
                throw new UnavailablePositionException();
            } else {
                return location.getLongitude();
            }
        }

        public void setLocation(@Nullable Location location) {
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LocationContainer that = (LocationContainer) o;

            if(location==null && that.location==null) return true;

            if(location!=null && that.location!=null) {
                double EPSILON = 1e-15;
                        return Math.abs(location.getLongitude() - that.location.getLongitude())<EPSILON
                                && Math.abs(location.getLatitude() - that.location.getLatitude())<EPSILON;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }

}
