package it.units.youweather.entities.forecast_fields;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * @author Matteo Ferfoglia
 */
public class Coordinates implements Serializable {

    private static final String TAG = Coordinates.class.getSimpleName();

    private volatile double lon;
    private volatile double lat;

    private Coordinates() {
    }

    public Coordinates(double latitude, double longitude) {
        final double MAX_ABS_LAT_VALUE = 90d;    // the maximum valid absolute value for latitude  (it must be in [-90,+90])
        final double MAX_ABS_LON_VALUE = 180d;   // the maximum valid absolute value for longitude (it must be in [-180,+180])

        this.lat = validateCoordinateValue(latitude, MAX_ABS_LAT_VALUE);
        this.lon = validateCoordinateValue(longitude, MAX_ABS_LON_VALUE);
    }

    /**
     * Validates a coordinate value, to be invoked before assigning it to the internal state.
     */
    private double validateCoordinateValue(
            double inputCoordinateValue, double maxAbsoluteValueForCoordinate) {
        double validatedCoordinateValue;
        if (Math.abs(inputCoordinateValue) <= maxAbsoluteValueForCoordinate) {
            validatedCoordinateValue = inputCoordinateValue;
        } else {
            validatedCoordinateValue = (inputCoordinateValue + maxAbsoluteValueForCoordinate) % (2 * maxAbsoluteValueForCoordinate) - maxAbsoluteValueForCoordinate;
            Log.w(TAG, "Illegal coordinate value: expected -"
                    + maxAbsoluteValueForCoordinate + "<=latitude<=" + maxAbsoluteValueForCoordinate
                    + ", found coordinate=" + inputCoordinateValue
                    + ", set coordinate=" + validatedCoordinateValue);
        }
        return validatedCoordinateValue;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @NonNull
    @Override
    public String toString() {
        return "Coordinate{" + "lon=" + lon + ", lat=" + lat + '}';
    }
}
