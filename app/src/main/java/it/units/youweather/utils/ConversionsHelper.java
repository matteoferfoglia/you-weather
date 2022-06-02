package it.units.youweather.utils;

public abstract class ConversionsHelper {

    private static final double KELVIN_OFFSET = 273.15;
    private static final double FAHRENHEIT_FACTOR = 9d / 5d;
    private static final double FAHRENHEIT_OFFSET = 32d;

    public static double temperatureFromKelvinToCelsius(double temperatureInKelvin) {
        return temperatureInKelvin - KELVIN_OFFSET;
    }

    public static double temperatureFromKelvinToFahrenheit(double temperatureInKelvin) {
        return (temperatureInKelvin - KELVIN_OFFSET) * FAHRENHEIT_FACTOR + FAHRENHEIT_OFFSET;
    }

    /**
     * Convert density-independent pixels (dp) into pixels.
     * From <a href="https://stackoverflow.com/a/5255256/17402378">here</a>.
     *
     * @param dp The input density-independent pixels to be converted.
     * @return the rounded number of pixels converted from the input dp.
     */
    public static int dpToPx(double dp) {
        final double scale = ResourceHelper.getAppContext().getResources()
                .getDisplayMetrics().density;
        return (int) Math.round(dp * scale);
    }
}
