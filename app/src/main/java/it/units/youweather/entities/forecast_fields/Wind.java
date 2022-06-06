package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Wind implements Serializable {

    private volatile double speed;
    private volatile int deg;

    private Wind() {
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getDeg() {
        return deg;
    }

    public void setDeg(int deg) {
        this.deg = deg;
    }

    @NonNull
    @Override
    public String toString() {
        return "Wind{" + "speed=" + speed + ", deg=" + deg + '}';
    }

}
