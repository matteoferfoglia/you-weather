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

    public static String convertEpochMillisToFormattedDate(long secondsSinceEpoch) {
        Date date = new Date(secondsSinceEpoch * 1000); // Date ctor expects milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
