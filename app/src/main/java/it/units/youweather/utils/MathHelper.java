package it.units.youweather.utils;

public abstract class MathHelper {

    /**
     * Rounds the given the real number to a fixed number of decimal digits.
     *
     * @param number        The real number to be rounded.
     * @param numOfDecimals The fixed number of decimal digits to be present
     *                      in the output number.
     * @return The rounded real number.
     */
    public static double roundToFixedNumOfDecimals(double number, int numOfDecimals) {
        final double ROUNDING_FACTOR = Math.pow(10, numOfDecimals);
        return Math.round(ROUNDING_FACTOR * number) / ROUNDING_FACTOR;
    }

}
