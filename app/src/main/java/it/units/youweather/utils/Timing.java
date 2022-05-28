package it.units.youweather.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class Timing {
    /**
     * Constant value used to recognized invalid fields (e.g., uninitialized)
     * containing date time in milliseconds from epoch, UTC time.
     */
    public static final long epochTimeInvalidInitialization = -1;

    /**
     * @param secondsSinceEpoch Seconds elapsed since Unix epoch.
     * @return the {@link String} corresponding to the UTC-formatted date-time.
     */
    public static String convertEpochMillisToFormattedDate(long secondsSinceEpoch) {
        Date date = new Date(secondsSinceEpoch);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, d MMMM, yyyy hh:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    /**
     * @return the value of {@link Date#getTime()}.
     */
    public static long getMillisSinceEpoch() {
        return new Date().getTime();
    }

    /**
     * @param millisSinceEpoch Milliseconds elapsed since Epoch.
     * @return The locale date-time (as {@link String}) obtained from the conversion
     * of the given parameter (milliseconds since epoch).
     */
    public static String convertMillisSinceEpochToLocale(long millisSinceEpoch) {
        return SimpleDateFormat.getDateTimeInstance().format(new Date(millisSinceEpoch));
    }

    /**
     * @return The locale date-time (as {@link String}) at the current instant.
     */
    public static String getCurrentLocaleDateTime() {
        return convertMillisSinceEpochToLocale(Timing.getMillisSinceEpoch());
    }
}
