package it.units.youweather.entities.forecast_fields;

import java.io.Serializable;

public class MainForecastData implements Serializable {

    private double temp;        // in Kelvin
    private double feels_like;
    private double temp_min;    // in Kelvin
    private double temp_max;    // in Kelvin
    private double pressure;
    private double humidity;

    public MainForecastData() {
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getFeels_like() {
        return feels_like;
    }

    public void setFeels_like(double feels_like) {
        this.feels_like = feels_like;
    }

    public double getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(double temp_min) {
        this.temp_min = temp_min;
    }

    public double getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(double temp_max) {
        this.temp_max = temp_max;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    @Override
    public String toString() {
        return "MainForecastData{" + "temp=" + temp + ", feels_like=" + feels_like + ", temp_min=" + temp_min + ", temp_max=" + temp_max + ", pressure=" + pressure + ", humidity=" + humidity + '}';
    }

}
