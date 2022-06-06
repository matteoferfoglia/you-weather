package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import java.io.Serializable;

import it.units.youweather.utils.Timing;

public class Sys implements Serializable {

    private volatile int type;
    private volatile int id;
    private volatile double message;
    private volatile String country;
    private volatile long sunrise;
    private volatile long sunset;

    private Sys() {
    }

    public String getCountry() {
        return country;
    }

    public long getSunriseUTCTimeInSecondsSinceEpoch() {
        return sunrise;
    }

    public long getSunsetUTCTimeInSecondsSinceEpoch() {
        return sunset;
    }

    @NonNull
    @Override
    public String toString() {
        return "Sys{"/* + "type=" + type
                + ", id=" + id
                + ", message=" + message
                + ","*/ + "country=" + country
                + ", sunrise=" + Timing.convertEpochMillisToFormattedDate(sunrise)
                + ", sunset=" + Timing.convertEpochMillisToFormattedDate(sunset)
                + '}';
    }

}
