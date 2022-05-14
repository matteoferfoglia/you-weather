package it.units.youweather.entities.forecast_fields;

public class Clouds {

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
