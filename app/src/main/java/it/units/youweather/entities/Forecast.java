package it.units.youweather.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Arrays;

import it.units.youweather.entities.forecast_fields.Clouds;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.MainForecastData;
import it.units.youweather.entities.forecast_fields.Sys;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.entities.forecast_fields.Wind;
import it.units.youweather.utils.Timing;

/**
 * Class representing a Weather forecast, with its fields,
 * used for de/serialization of information.
 *
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("unused") // used for de/serialization
public class Forecast implements Serializable {
    private volatile Coordinates coord;
    private volatile WeatherCondition[] weather;
    private volatile MainForecastData main;
    private volatile int visibility;
    private volatile Wind wind;
    private volatile Clouds clouds;
    private volatile long dt;
    private volatile Sys sys;
    private volatile int timezone;
    private volatile long id;
    private volatile String name;

    private Forecast() {
    }

    public Coordinates getCoord() {
        return coord;
    }

    public WeatherCondition[] getWeather() {
        return weather;
    }

    public MainForecastData getForecastData() {
        return main;
    }

    /**
     * @return the visibility in meter, max value is 10_000 m.
     */
    public int getVisibility() {
        return visibility;
    }

    public Wind getWind() {
        return wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    /**
     * @return the date-time of the data calculation.
     */
    public String getDataCalculationDateTime() {
        return Timing.convertEpochMillisToFormattedDate(dt);
    }

    public long getCityId() {
        return id;
    }

    public String getCityName() {
        return name;
    }

    /**
     * See {@link Sys#getSunriseUTCTimeInSecondsSinceEpoch()}.
     */
    public long getSunriseUTCTimeInSecondsSinceEpoch() {
        return sys.getSunriseUTCTimeInSecondsSinceEpoch();
    }

    /**
     * See {@link Sys#getSunsetUTCTimeInSecondsSinceEpoch()}.
     */
    public long getSunsetUTCTimeInSecondsSinceEpoch() {
        return sys.getSunsetUTCTimeInSecondsSinceEpoch();
    }

    @NonNull
    @Override
    public String toString() {
        return "Forecast{"
                + "coord=" + coord
                + ", weather=" + Arrays.toString(weather)
                + ", main=" + main
                + ", visibility=" + visibility
                + ", wind=" + wind
                + ", clouds=" + clouds
                + ", dt=" + Timing.convertEpochMillisToFormattedDate(dt)
                + ", sys=" + sys
                + ", timezone=" + timezone
                + ", id=" + id
                + ", name=" + name
                + '}';
    }

}
