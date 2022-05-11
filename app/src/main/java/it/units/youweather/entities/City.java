package it.units.youweather.entities;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * Class representing a city, with its information, used for
 * de/serialization of information.
 *
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("unused") // used for de/serialization
public class City {
    private String name;
    private Map<String, String> local_names;
    private double lat;
    private double lon;
    private String country;
    private String state;

    private City() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLocal_names() {
        return local_names;
    }

    public void setLocal_names(Map<String, String> local_names) {
        this.local_names = local_names;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "City{" + "name=" + name
                + ", local_names=" + local_names
                + ", lat=" + lat
                + ", lon=" + lon
                + ", country=" + country
                + ", state=" + state + '}';
    }
}
