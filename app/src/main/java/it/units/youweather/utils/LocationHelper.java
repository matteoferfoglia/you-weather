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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import it.units.youweather.R;
import it.units.youweather.entities.City;
import it.units.youweather.entities.Forecast;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.http.HTTPRequest;
import it.units.youweather.utils.http.HTTPResponse;

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
    private final LocationContainer userCurrentLocation = new LocationContainer();

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
        @SuppressWarnings({"deprecation", "RedundantSuppression"})
        // "deprecation": this abstract method must be overridden; "RedundantSuppression": IDE thinks the suppression is redundant but Xlint warns for it
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * Constructor.
     *
     * @param activity The activity for which this instance will work.
     */
    @SuppressLint("MissingPermission")  // checked in if-statement
    public LocationHelper(@NonNull Activity activity) throws PermissionsHelper.MissingPermissionsException {

        String[] neededPermissions = new String[]{   // array of needed permissions
                Manifest.permission.ACCESS_FINE_LOCATION,   // for getting user's current position
                Manifest.permission.WRITE_EXTERNAL_STORAGE  // for showing the map
        };
        PermissionsHelper.requestPermissionsForActivityIfNecessary(
                neededPermissions, Objects.requireNonNull(activity));

        // Set the LocationManager to updates (asynchronously) the user location
        if (PermissionsHelper.arePermissionsGrantedForActivity(neededPermissions, activity)) {
            ((LocationManager) activity.getSystemService(LOCATION_SERVICE))
                    .requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        } else {
            throw new PermissionsHelper.MissingPermissionsException();
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
    @SuppressWarnings({"serial", "RedundantSuppression"})
    // "serial": helper class, not to be serialized; "RedundantSuppression": IDE says redundant suppression for "serial", Xlint instead warns
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
                Location newLocation = userCurrentLocation.getCopyOfLocation();
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

            if (location == null && that.location == null) return true;

            if (location != null && that.location != null) {
                double EPSILON = 1e-15;
                return Math.abs(location.getLongitude() - that.location.getLongitude()) < EPSILON
                        && Math.abs(location.getLatitude() - that.location.getLatitude()) < EPSILON;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(location);
        }
    }

// -------------------------------------------------------------------------------------------------

    private static final String OPEN_WEATHER_MAP_API_KEY;

    static {
        String openWeatherMapApiKeyTmp;
        try {
            openWeatherMapApiKeyTmp = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(
                                    ResourceHelper.getAppContext().getResources()
                                            .openRawResource(R.raw.openweathermap_apikey))))
                    .readLine();
        } catch (IOException e) {
            openWeatherMapApiKeyTmp = "";
            Log.e(TAG, "Error in file opening", e);
        }
        OPEN_WEATHER_MAP_API_KEY = openWeatherMapApiKeyTmp;
    }

    /**
     * Given the name of a city (also a draft only) the system tries to retrieve
     * all cities matching the given name.
     * The execution of this method is non-blocking: operations are performed by
     * a separate thread.
     *
     * @param cityName          The name of a {@link City}.
     * @param cityArrayConsumer The consumer for the array of {@link City cities}
     *                          matching the inserted string.
     */
    public static void getCitiesFromNameAndConsume(@NonNull String cityName,
                                                   @NonNull Consumer<City[]> cityArrayConsumer) {
        new Thread(() -> {
            String requestString = "http://api.openweathermap.org/geo/1.0/direct?"
                    + "q=" + Objects.requireNonNull(cityName)
                    //+ "&limit=10"
                    + "&appid=" + OPEN_WEATHER_MAP_API_KEY;

            City[] cities;
            try {
                HTTPRequest req = new HTTPRequest(requestString);
                String responseString = new HTTPResponse(req).getResponse();
                cities = JsonHelper.fromJson(responseString, City[].class);
            } catch (IOException e) {
                cities = new City[0];
            }
            cityArrayConsumer.accept(cities);
        }).start();
    }


    /**
     * This method performs a HTTP request to the server in charge of resolving
     * coordinates into cities and returns the array of cities satisfying the
     * request.
     * <strong>Important</strong>: this method performs networking operation,
     * so, it cannot be invoked from the main thread. Use method
     * {@link } instead.
     *
     * @param coordinates Coordinates.
     * @return the array of know cities complying with the request.
     */
    public static City[] getCitiesFromCoordinates(@NonNull Coordinates coordinates) {

        City[] resolvedCities;

        // Reverse Geocoding API to resolve coordinates into cities

        final String requestString = "http://api.openweathermap.org/geo/1.0/reverse?"
                + "lat=" + Objects.requireNonNull(coordinates).getLat()
                + "&lon=" + coordinates.getLon()
                + "&appid=" + OPEN_WEATHER_MAP_API_KEY;

        try {
            HTTPRequest req = new HTTPRequest(requestString);
            final String responseString = new HTTPResponse(req).getResponse();
            resolvedCities = JsonHelper.fromJson(responseString, City[].class);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open HTTP connection. Error is: " + e.getMessage(), e);
            resolvedCities = new City[0];
        }

        return resolvedCities;
    }

    /**
     * This method is similar to {@link #getCitiesFromCoordinates(Coordinates)},
     * but this one performs all the operation on a separate thread (this means that
     * the invoker thread returns immediately), and then, instead of returning the
     * {@link City} array, consumes it according to the given {@link Consumer}.
     * This method performs asynchronous operations.
     *
     * @param coordinates       Coordinates.
     * @param cityArrayConsumer The {@link Consumer} for the array of cities matching
     *                          the request and returned by the server.
     * @return The {@link Thread} responsible for the asynchronous operations, after
     * having <strong>already</strong> started it.
     */
    public static Thread getCitiesFromCoordinatesAndConsume(
            @NonNull Coordinates coordinates, @NonNull Consumer<City[]> cityArrayConsumer) {
        Thread t = new Thread(() -> Objects.requireNonNull(cityArrayConsumer)
                .accept(getCitiesFromCoordinates(coordinates)));
        t.start();
        return t;
    }

    /**
     * Get the {@link Forecast weather forecast} for the given
     * {@link Coordinates location}.
     * <br/>
     * <strong>The operation is asynchronous</strong>: when completed,
     * the given {@link Consumer} will consumes the result.
     * <br/>
     * This method does not throw any exceptions, but, if they happened
     * while retrieving the data, the given handler will be executed.
     *
     * @param coordinates      {@link Coordinates} for the place for which you want to know
     *                         to have the {@link Forecast weather forecast}.
     * @param consumer         The {@link Consumer} for the {@link Forecast}.
     * @param exceptionHandler The {@link Consumer exception handler} for the
     *                         eventually thrown exception.
     */
    public static void getForecastForCoordinates(
            @NonNull Coordinates coordinates,
            @NonNull Consumer<Forecast> consumer,
            @NonNull Consumer<IOException> exceptionHandler) {
        new Thread(() -> {
            String requestString = "http://api.openweathermap.org/data/2.5/weather?"
                    + "lat=" + Objects.requireNonNull(coordinates).getLat()
                    + "&lon=" + coordinates.getLon()
                    + "&appid=" + OPEN_WEATHER_MAP_API_KEY;
            try {
                HTTPRequest req = new HTTPRequest(requestString);
                String responseString = new HTTPResponse(req).getResponse();
                Objects.requireNonNull(consumer)
                        .accept(JsonHelper.fromJson(responseString, Forecast.class));
            } catch (IOException e) {
                Objects.requireNonNull(exceptionHandler).accept(e);
            }
        }).start();
    }

}
