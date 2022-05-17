package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import it.units.youweather.utils.Timing;

public class Sys {

    private int type;
    private int id;
    private double message;
    private String country;
    private long sunrise;
    private long sunset;

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
