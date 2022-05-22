package it.units.youweather.entities;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import it.units.youweather.EnvironmentVariables;
import it.units.youweather.utils.Conversions;
import it.units.youweather.utils.MathHelper;

/**
 * @author Matteo Ferfoglia
 */
public class Temperature {

    /**
     * TAG for logger.
     */
    private final static String TAG = Temperature.class.getSimpleName();

    private static final int NUM_OF_DECIMAL = 1;

    public enum TEMPERATURE_MEASURE_UNIT {
        KELVIN("K"), CELSIUS("°C"), FAHRENHEIT("°F");
        private final String measure_unit;

        TEMPERATURE_MEASURE_UNIT(@NonNull String measure_unit) {
            this.measure_unit = Objects.requireNonNull(measure_unit);
        }

        @NonNull
        public String getMeasureUnit() {
            return measure_unit;
        }
    }

    private final double temperatureInKelvin;

    public Temperature(double temperatureInKelvin) {
        final double MIN_REASONABLE_KELVIN = 200;
        final double MAX_REASONABLE_KELVIN = 320;
        if (temperatureInKelvin < MIN_REASONABLE_KELVIN || temperatureInKelvin > MAX_REASONABLE_KELVIN) {
            Log.w(TAG, "Non reasonable Kelvin value (" + temperatureInKelvin + ")");
        }
        this.temperatureInKelvin = temperatureInKelvin;
    }

    public double getTemperature() {
        double convertedValue;
        switch (EnvironmentVariables.TEMPERATURE_MEASURE_UNIT) {
            case KELVIN:
                convertedValue = temperatureInKelvin;
                break;
            case CELSIUS:
                convertedValue = Conversions.temperatureFromKelvinToCelsius(temperatureInKelvin);
                break;
            case FAHRENHEIT:
                convertedValue = Conversions.temperatureFromKelvinToFahrenheit(temperatureInKelvin);
                break;
            default:
                throw new IllegalStateException("Unknown temperature measure unit");
        }

        return MathHelper.roundToFixedNumOfDecimals(convertedValue, NUM_OF_DECIMAL);
    }

    public String getTemperatureWithMeasureUnit() {
        double temperatureAlreadyConvertedInDesiredMeasureUnit =
                getTemperature();
        return temperatureAlreadyConvertedInDesiredMeasureUnit + " "
                + EnvironmentVariables.TEMPERATURE_MEASURE_UNIT.getMeasureUnit();
    }

    @NonNull
    @Override
    public String toString() {
        return temperatureInKelvin + " K";
    }
}
