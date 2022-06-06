package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Clouds implements Serializable {

    private volatile int all;

    private Clouds() {
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    @NonNull
    @Override
    public String toString() {
        return "Clouds{" + "all=" + all + '}';
    }

}
