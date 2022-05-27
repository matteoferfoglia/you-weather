package it.units.youweather.entities.storage;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

import it.units.youweather.entities.City;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.storage.entities.DBEntity;
import it.units.youweather.utils.storage.helpers.DBHelper;

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
     * The {@link Bitmap photo} taken by the user for this report.
     */
    private volatile Bitmap picture;

    /**
     * The {@link it.units.youweather.entities.LoggedInUser} who reported this instance.
     */
    private volatile LoggedInUser reporter;

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
     * @param reporter         The {@link LoggedInUser} that made this report.
     * @param city             The {@link City} to which this instance refers to.
     * @param coordinates      The {@link Coordinates} (more precise than the
     *                         {@link City}) to which this instance refers to.
     * @param weatherCondition The {@link WeatherCondition} for this instance.
     * @param picture          The {@link Bitmap photo} for this instance.
     */
    public WeatherReport(@NonNull LoggedInUser reporter,
                         @NonNull City city, @NonNull Coordinates coordinates,
                         @NonNull WeatherCondition weatherCondition, @Nullable Bitmap picture) {
        this();
        this.reporter = Objects.requireNonNull(reporter);
        this.city = Objects.requireNonNull(city);
        this.coordinates = Objects.requireNonNull(coordinates);
        this.weatherCondition = Objects.requireNonNull(weatherCondition);
        this.picture = picture;
        this.millisecondsSinceEpoch = Timing.getMillisSinceEpoch();
    }

    public WeatherReport(@NonNull LoggedInUser reporter,
                         @NonNull City city, @NonNull Coordinates coordinates,
                         @NonNull WeatherCondition weatherCondition) {
        this(reporter, city, coordinates, weatherCondition, null);
    }

    private WeatherReport() {
        super();
    }

    public City getCity() {
        return city;
    }

    public void setCity(@NonNull City city) {
        this.city = Objects.requireNonNull(city);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(@NonNull Coordinates coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(@NonNull WeatherCondition weatherCondition) {
        this.weatherCondition = Objects.requireNonNull(weatherCondition);
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(@NonNull Bitmap picture) {
        this.picture = Objects.requireNonNull(picture);
    }

    public long getMillisecondsSinceEpoch() {
        return millisecondsSinceEpoch;
    }

    public void setMillisecondsSinceEpoch(long millisecondsSinceEpoch) {
        this.millisecondsSinceEpoch = millisecondsSinceEpoch;
    }

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
