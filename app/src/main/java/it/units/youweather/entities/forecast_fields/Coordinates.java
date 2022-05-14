package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

public class Coordinates {
    private double lon;
    private double lat;

    private Coordinates() {
    }

    public Coordinates(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
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
