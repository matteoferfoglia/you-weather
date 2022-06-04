package it.units.youweather.entities.storage;

import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Objects;

import it.units.youweather.entities.City;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.storage.DBEntity;
import it.units.youweather.utils.storage.Query;

/**
 * An instance of this class is built starting from an instance of
 * {@link WeatherReport}. This class contains just some of the fields
 * from {@link WeatherReport} and is intended to provide a preview
 * of {@link WeatherReport}. This is done to exchange and load less
 * data from the database, which is a time and computational expensive
 * operation.
 *
 * @author Matteo Ferfoglia
 */
public class WeatherReportPreview extends DBEntity {

    /**
     * TAG for the logger.
     */
    private static final String TAG = WeatherReportPreview.class.getSimpleName();
    /**
     * The key on the database for the tuple containing the details
     * (i.e., the actual {@link WeatherReport}) associated with this
     * instance.
     */
    private volatile String weatherReportDetailsKey;

    /**
     * The name of the location to which this instance refers to.
     */
    private volatile String locationName;

    /**
     * The property returned by {@link LoggedInUser#getUserId()} of the {@link LoggedInUser}
     * that made this report.
     */
    private volatile String reporterUserId;

    /**
     * The date time for the report, expressed as the number of
     * milliseconds elapsed since Epoch.
     */
    private volatile long reportedTimeMillisSinceEpoch;

    /**
     * This property merges the location and the time to which it refers to
     * and is created to support queries involving the time and then the
     * city too, in the case that your DBMS supports query on single field only.
     */
    private volatile String location_time;

    /**
     * The coordinates for this report.
     */
    private volatile Coordinates coordinates;

    /**
     * The reported weather condition.
     */
    private volatile WeatherCondition weatherCondition;

    private WeatherReportPreview() {
        super();
    }

    public WeatherReportPreview(@NonNull String weatherReportDetailsKey, @NonNull WeatherReport weatherReport) {
        this();
        this.weatherReportDetailsKey = Objects.requireNonNull(weatherReportDetailsKey);
        Objects.requireNonNull(weatherReport);
        this.locationName = weatherReport.getCity().toString();
        this.reporterUserId = weatherReport.getReporterUserId();
        this.reportedTimeMillisSinceEpoch = weatherReport.getMillisecondsSinceEpoch();
        this.weatherCondition = weatherReport.getWeatherCondition();
        this.coordinates = weatherReport.getCoordinates();
        this.location_time = mergeCityAndTimeIntoSingleValue(weatherReport.getCity(), reportedTimeMillisSinceEpoch);
    }

    public String getLocation_time() {
        return location_time;
    }

    public String getWeatherReportDetailsKey() {
        return weatherReportDetailsKey;
    }

    public String getLocationName() {
        return locationName;
    }

    public long getReportedTimeMillisSinceEpoch() {
        return reportedTimeMillisSinceEpoch;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Create a {@link Query} whose expected results (after evaluation) are all
     * the instances of this class, reported by the {@link LoggedInUser} whose
     * {@link LoggedInUser#getUserId() userId} is given.
     *
     * @param userId The userId.
     * @return a {@link Query} for {@link WeatherReport}s, for all entities reported
     * by the {@link LoggedInUser} whose userId is given.
     */
    @NonNull
    public static Query<String> createQueryToRetrieveByUserId(@NonNull String userId)
            throws NoSuchFieldException {
        registerThisClassForDB(WeatherReportPreview.class);
        Field usernameField = WeatherReportPreview.class.getDeclaredField("reporterUserId");
        return new Query<>(usernameField, userId);
    }

    /**
     * Create a {@link Query} whose expected results (after evaluation) are all
     * the instances of this class, reported in the specified time interval,
     * for the specified {@link City}.
     *
     * @param city A city.
     * @return a {@link Query} for {@link WeatherReport}s, for all entities with
     * the given city in the given time interval.
     */
    @NonNull
    public static Query<String> createQueryOnCityAndTime(
            @NonNull City city, @NonNull Date startDateTime, @NonNull Date endDateTime) {
        registerThisClassForDB(WeatherReportPreview.class);   // if it is not registered yet
        try {
            Field city_time_field = WeatherReportPreview.class.getDeclaredField("location_time");
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

    /**
     * This method should be used to populate the value of {@link #location_time}.
     */
    @NonNull
    private static String mergeCityAndTimeIntoSingleValue(@NonNull City city, long millisecondsSinceEpoch) {
        return city.toString() + millisecondsSinceEpoch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherReportPreview that = (WeatherReportPreview) o;
        return reportedTimeMillisSinceEpoch == that.reportedTimeMillisSinceEpoch
                && Objects.equals(weatherReportDetailsKey, that.weatherReportDetailsKey)
                && Objects.equals(locationName, that.locationName)
                && Objects.equals(reporterUserId, that.reporterUserId)
                && Objects.equals(location_time, that.location_time)
                && Objects.equals(weatherCondition, that.weatherCondition)
                && Objects.equals(coordinates, that.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                weatherReportDetailsKey, locationName, reporterUserId,
                reportedTimeMillisSinceEpoch, location_time, weatherCondition, coordinates);
    }
}
