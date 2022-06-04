package it.units.youweather.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import it.units.youweather.R;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.ResourceHelper;
import it.units.youweather.utils.Timing;

/**
 * Class representing a city, with its information, used for
 * de/serialization of information.
 *
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("unused") // used for de/serialization
public class City implements Serializable {
    private static final String TAG = City.class.getSimpleName();

    private String name;
    private Map<String, String> local_names;
    private double lat;
    private double lon;
    private String country;
    private String state;

    /**
     * Saves the sunrise time for today in this{@link City}.
     * Initialized to {@link Timing#epochTimeInvalidInitialization}
     * to recognize if it has not been computed yet.
     * Time is in seconds.
     */
    private final transient AtomicLong todaySunrise =
            new AtomicLong(Timing.epochTimeInvalidInitialization);

    /**
     * Like {@link #todaySunrise}, but for the sunset.
     */
    private final transient AtomicLong todaySunset =
            new AtomicLong(Timing.epochTimeInvalidInitialization);

    /**
     * Scheduler to reset {@link #todaySunrise} and {@link #todaySunrise}
     * when the midnight is reached.
     */
    private transient ScheduledExecutorService sunriseAndSunsetResetScheduler = null;

    /**
     * Mutex used to wait for the sunrise and the sunset times to be set.
     */
    private final transient AtomicBoolean sunriseAndSunsetTimesForCityAreSet =
            new AtomicBoolean(false);

    private City() {

        new Thread(this::setSunriseAndSunsetTimesForToday).start();

        sunriseAndSunsetResetScheduler = Executors.newScheduledThreadPool(1);
        {
            // Schedules a thread to reset sunrise and sunset time at midnight

            Calendar lastMidnight = Calendar.getInstance();
            lastMidnight.set(Calendar.HOUR, 0);
            lastMidnight.set(Calendar.MINUTE, 0);
            lastMidnight.set(Calendar.SECOND, 0);
            lastMidnight.set(Calendar.MILLISECOND, 0);

            Calendar nextMidnight = Calendar.getInstance();
            nextMidnight.setTime(lastMidnight.getTime());
            nextMidnight.add(Calendar.DATE, 1);

            final int SECONDS_TO_MILLISECONDS_CONVERSION_FACTOR = 1_000;
            final long secondsSinceMidnight = (Timing.getMillisSinceEpoch() - lastMidnight.getTimeInMillis()) / SECONDS_TO_MILLISECONDS_CONVERSION_FACTOR;
            final long secondsToMidnight = (nextMidnight.getTimeInMillis() - Timing.getMillisSinceEpoch()) / SECONDS_TO_MILLISECONDS_CONVERSION_FACTOR;
            final long SECONDS_IN_A_DAY = 3600 * 24;

            Log.d(TAG, "Since midnight: " + secondsSinceMidnight
                    + " s, to nex midnight: " + secondsToMidnight); // TODO: check correctness with Locale?

            sunriseAndSunsetResetScheduler.scheduleAtFixedRate(
                    this::setSunriseAndSunsetTimesForToday,
                    secondsToMidnight, SECONDS_IN_A_DAY, TimeUnit.SECONDS);
        }

    }

    /**
     * @return the sunrise time for today, in seconds, since UNIX epoch, UTC,
     * or {@link Timing#epochTimeInvalidInitialization} in case of invalid initialization.
     */
    public long getSunriseUTCTimeInSecondsSinceEpochOrInvalidInitialization() {
        waitForSunriseAndSunsetToBeSet();
        return todaySunrise.get();
    }

    private void waitForSunriseAndSunsetToBeSet() {
        final int RETRY_PERIOD_MILLIS = 10;
        while (!sunriseAndSunsetTimesForCityAreSet.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted", e);
            }
        }
    }

    private void setSunriseAndSunsetTimesForToday() {
        final long START_TIME_MILLIS = Timing.getMillisSinceEpoch();
        final long DELAY_BETWEEN_ATTEMPTS_MILLIS = 10;
        final long MAX_TOTAL_DELAY_FOR_ATTEMPTS_MILLIS = 15_000;
        final long END_TIME_ATTEMPTS_MILLIS = START_TIME_MILLIS + MAX_TOTAL_DELAY_FOR_ATTEMPTS_MILLIS;

        boolean coordinatesAreSet = false;

        while (Timing.getMillisSinceEpoch() < END_TIME_ATTEMPTS_MILLIS && !coordinatesAreSet) {
            if (lat >= 0 && lon >= 0) {
                coordinatesAreSet = true;
            } else {
                try {
                    Thread.sleep(DELAY_BETWEEN_ATTEMPTS_MILLIS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted thread", e);
                    break;
                }
            }
        }

        if (!coordinatesAreSet) {
            Log.e(TAG, ResourceHelper.getResString(R.string.unable_to_set_sunrise_and_sunset_coordinates_for_city));
        }

        LocationHelper.getForecastForCoordinates(
                new Coordinates(lat, lon),
                forecast -> {
                    todaySunrise.set(forecast.getSunriseUTCTimeInSecondsSinceEpoch());
                    todaySunset.set(forecast.getSunsetUTCTimeInSecondsSinceEpoch());

                    sunriseAndSunsetTimesForCityAreSet.set(true);
                },
                e -> {
                    Log.e(TAG, "Unable to get sunset and sunrise times, set to "
                            + Timing.epochTimeInvalidInitialization, e);

                    todaySunrise.set(Timing.epochTimeInvalidInitialization);
                    todaySunset.set(Timing.epochTimeInvalidInitialization);

                    sunriseAndSunsetTimesForCityAreSet.set(true);
                });

        while (!sunriseAndSunsetTimesForCityAreSet.get()) {
            try {
                Thread.sleep(DELAY_BETWEEN_ATTEMPTS_MILLIS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted", e);
            }
        }
    }

    /**
     * @return the sunset time for today, in seconds, since UNIX epoch, UTC,
     * or {@link Timing#epochTimeInvalidInitialization} in case of invalid initialization.
     */
    public long getSunsetUTCTimeInSecondsSinceEpochOrInvalidInitialization() {
        waitForSunriseAndSunsetToBeSet();
        return todaySunset.get();
    }

    protected void finalize() {
        sunriseAndSunsetResetScheduler.shutdownNow();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLocal_names() {
        return local_names;
    }

    public void setLocal_names(Map<String, String> local_names) {
        this.local_names = local_names;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return name
                + (country != null ? (", " + country) : "")
                + (state != null ? (", " + state) : "");
    }
}
