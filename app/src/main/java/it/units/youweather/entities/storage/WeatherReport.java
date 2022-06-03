package it.units.youweather.entities.storage;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Objects;

import it.units.youweather.entities.City;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.utils.ImagesHelper;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.storage.DBEntity;
import it.units.youweather.utils.storage.DBHelper;
import it.units.youweather.utils.storage.Query;

/**
 * This class is a weather report that can be created by the user.
 *
 * @author Matteo Ferfoglia
 */
public class WeatherReport extends DBEntity implements Serializable {

    /**
     * The {@link City} to which this instance refers to.
     */
    private volatile City city;

    /**
     * The {@link Coordinates} to which this instance refers to.
     */
    private volatile Coordinates coordinates;

    /**
     * The {@link WeatherCondition} reported in this instance.
     */
    private volatile WeatherCondition weatherCondition;

    /**
     * The milliseconds elapsed since Epoch to the instant
     * in which the instance has been created.
     */
    private volatile long millisecondsSinceEpoch;

    /**
     * The {@link ImagesHelper.SerializableBitmap photo} taken by the user for this report.
     */
    private volatile ImagesHelper.SerializableBitmap picture;

    /**
     * The property returned by {@link LoggedInUser#getUserId()} of the {@link LoggedInUser}
     * that made this report.
     */
    private volatile String reporterUserId;

    /**
     * Tis property merges the city and the time and is created to support
     * queries involving the time and then the city too, in the case that your
     * DBMS supports query on single field only.
     */
    private volatile String city_time;

    /**
     * TAG for Logger.
     */
    private static final String TAG = WeatherReport.class.getSimpleName();

    /**
     * Register this class to be used with the database.
     */
    public static void registerThisClassForDB() {
        if (!registeredClasses.contains(WeatherReport.class)) {
            DBHelper.registerEntityClass(WeatherReport.class,
                    createdEntity -> Log.d(TAG, "CREATED " + createdEntity),
                    removedEntity -> Log.d(TAG, "REMOVED " + removedEntity),
                    updatedEntity -> Log.d(TAG, "UPDATED " + updatedEntity));
            registeredClasses.add(WeatherReport.class);
        }
    }

    /**
     * Creates a new instance of this class.
     *
     * @param reporterUserId   The property returned by {@link LoggedInUser#getUserId()} of the
     *                         {@link LoggedInUser} that made this report
     * @param city             The {@link City} to which this instance refers to.
     * @param coordinates      The {@link Coordinates} (more precise than the
     *                         {@link City}) to which this instance refers to.
     * @param weatherCondition The {@link WeatherCondition} for this instance.
     * @param picture          The {@link ImagesHelper.SerializableBitmap photo} for this instance.
     */
    public WeatherReport(@NonNull String reporterUserId,
                         @NonNull City city, @NonNull Coordinates coordinates,
                         @NonNull WeatherCondition weatherCondition, @Nullable ImagesHelper.SerializableBitmap picture) {
        this();
        this.reporterUserId = Objects.requireNonNull(reporterUserId);
        this.city = Objects.requireNonNull(city);
        this.coordinates = Objects.requireNonNull(coordinates);
        this.weatherCondition = Objects.requireNonNull(weatherCondition);
        this.picture = picture;
        this.millisecondsSinceEpoch = Timing.getMillisSinceEpoch();
        this.city_time = mergeCityAndTimeIntoSingleValue(city, millisecondsSinceEpoch);
    }

    /**
     * This method should be used to populate the value of {@link #city_time}.
     */
    @NonNull
    private static String mergeCityAndTimeIntoSingleValue(@NonNull City city, long millisecondsSinceEpoch) {
        return city.toString() + millisecondsSinceEpoch;
    }

    /**
     * See parameter descriptions in {@link #WeatherReport(String, City, Coordinates, WeatherCondition, ImagesHelper.SerializableBitmap)}.
     */
    public WeatherReport(@NonNull String reporterUserId,
                         @NonNull City city, @NonNull Coordinates coordinates,
                         @NonNull WeatherCondition weatherCondition) {
        this(reporterUserId, city, coordinates, weatherCondition, null);
    }

    private WeatherReport() {
        super();
        registerThisClassForDB();
    }

    public City getCity() {
        return city;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public ImagesHelper.SerializableBitmap getPicture() {
        return picture;
    }

    public long getMillisecondsSinceEpoch() {
        return millisecondsSinceEpoch;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public String getCity_time() {
        return city_time;
    }

    /**
     * Create a {@link Query} whose expected result (after evaluation) are all
     * the instances of this class, reported in the specified time interval,
     * for the specified {@link City}.
     *
     * @param city A city
     * @return a {@link Query} for {@link WeatherReport}s, for all entities with
     * the given city in the given time interval.
     */
    public static Query<String> createQueryOnCityAndTime(
            @NonNull City city, @NonNull Date startDateTime, @NonNull Date endDateTime) {
        registerThisClassForDB();   // if it is not registered yet
        try {
            Field city_time_field = WeatherReport.class.getDeclaredField("city_time");
            long startDateTimeSinceEpoch = Timing.getMillisSinceEpoch(startDateTime);
            long endDateTimeSinceEpoch = Timing.getMillisSinceEpoch(endDateTime);
            String startingValue = mergeCityAndTimeIntoSingleValue(
                    Objects.requireNonNull(city), startDateTimeSinceEpoch);
            String endingValue = mergeCityAndTimeIntoSingleValue(
                    Objects.requireNonNull(city), endDateTimeSinceEpoch);
            return new Query<>(city_time_field, startingValue, endingValue);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Error in query generation.", e);
            throw new IllegalStateException(e);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "WeatherReport{" +
                "city=" + city +
                ", coordinates=" + coordinates +
                ", weatherCondition=" + weatherCondition +
                ", millisecondsSinceEpoch=" + millisecondsSinceEpoch +
                ", picture=" + picture +
                '}';
    }

    /**
     * @return a short description of the {@link WeatherCondition}
     * for this instance.
     */
    public String getWeatherConditionToString() {
        return weatherCondition.getDescription();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherReport that = (WeatherReport) o;
        return Objects.equals(city, that.city)
                && Objects.equals(coordinates, that.coordinates)
                && Objects.equals(weatherCondition, that.weatherCondition)
                && Objects.equals(picture, that.picture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, coordinates, weatherCondition, picture);
    }
}
