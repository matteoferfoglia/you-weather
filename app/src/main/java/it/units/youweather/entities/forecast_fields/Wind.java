package it.units.youweather.entities.forecast_fields;

public class Wind {
    
    private double speed;
    private int deg;
    
    private Wind(){};

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

    @Override
    public String toString() {
        return "Wind{" + "speed=" + speed + ", deg=" + deg + '}';
    }
    
}