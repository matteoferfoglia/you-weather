package it.units.youweather.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Timing {
    public static String convertEpochMillisToFormattedDate(long secondsSinceEpoch) {
        Date date = new Date(secondsSinceEpoch*1000); // Date ctor expects milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
