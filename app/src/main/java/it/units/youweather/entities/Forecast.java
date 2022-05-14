package it.units.youweather.entities;

import androidx.annotation.NonNull;

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
public class Forecast {
    private Coordinates coord;
    private WeatherCondition[] weather;
    private String base;
    private MainForecastData main;
    private int visibility;
    private Wind wind;
    private Clouds clouds;
    private long dt;
    private Sys sys;
    private int timezone;
    private long id;
    private String name;
    private int cod;

    private Forecast() {
    }

    public Coordinates getCoord() {
        return coord;
    }

    public void setCoord(Coordinates coord) {
        this.coord = coord;
    }

    public WeatherCondition[] getWeather() {
        return weather;
    }

    public void setWeather(WeatherCondition[] weather) {
        this.weather = weather;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public MainForecastData getMain() {
        return main;
    }

    public void setMain(MainForecastData main) {
        this.main = main;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public long getDt() {
        return dt;
    }

    public void setDt(long dt) {
        this.dt = dt;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public int getTimezone() {
        return timezone;
    }

    public void setTimezone(int timezone) {
        this.timezone = timezone;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return cod;
    }

    public void setCode(int code) {
        this.cod = code;
    }

    @NonNull
    @Override
    public String toString() {
        return "Forecast{"
                + "coord=" + coord
                + ", weather=" + Arrays.toString(weather)
                + ", base=" + base
                + ", main=" + main
                + ", visibility=" + visibility
                + ", wind=" + wind
                + ", clouds=" + clouds
                + ", dt=" + Timing.convertEpochMillisToFormattedDate(dt)
                + ", sys=" + sys
                + ", timezone=" + timezone
                + ", id=" + id
                + ", name=" + name
                + ", cod=" + cod + '}';
    }

}
