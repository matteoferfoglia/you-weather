package it.units.youweather.utils;

public abstract class Conversions {

    private static final double KELVIN_OFFSET = 273.15;
    private static final double FAHRENHEIT_FACTOR = 9d / 5d;
    private static final double FAHRENHEIT_OFFSET = 32d;

    public static double temperatureFromKelvinToCelsius(double temperatureInKelvin) {
        return temperatureInKelvin - KELVIN_OFFSET;
    }

    public static double temperatureFromKelvinToFahrenheit(double temperatureInKelvin) {
        return (temperatureInKelvin - KELVIN_OFFSET) * FAHRENHEIT_FACTOR + FAHRENHEIT_OFFSET;
    }
}
