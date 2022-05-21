package it.units.youweather.entities.forecast_fields;

import java.io.Serializable;

public class Clouds implements Serializable {

    private int all;

    private Clouds() {
    }

    public int getAll() {
        return all;
    }

    public void setAll(int all) {
        this.all = all;
    }

    @Override
    public String toString() {
        return "Clouds{" + "all=" + all + '}';
    }

}
