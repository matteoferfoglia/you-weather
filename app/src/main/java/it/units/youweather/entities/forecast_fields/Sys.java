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
    
    private Sys(){}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMessage() {
        return message;
    }

    public void setMessage(double message) {
        this.message = message;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    @NonNull
    @Override
    public String toString() {
        return "Sys{" + "type=" + type 
                + ", id=" + id 
                + ", message=" + message 
                + ", country=" + country 
                + ", sunrise=" + Timing.convertEpochMillisToFormattedDate(sunrise)
                + ", sunset=" + Timing.convertEpochMillisToFormattedDate(sunset)
                + '}';
    }
       
}
