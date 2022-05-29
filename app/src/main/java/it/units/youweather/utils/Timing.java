package it.units.youweather.utils;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import it.units.youweather.R;

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

    /**
     * Returns the {@link Date} instance for given year, month (0-based) and day input parameters.
     *
     * @param year       The year.
     * @param month      The index for the month (0 for January, 1 for February, ..., 11 for December).
     * @param dayOfMonth The day of the month (0..31)
     * @return the {@link Date} for the input parameters.
     * @throws IllegalArgumentException if invalid parameter.
     */
    @NonNull
    public static Date getDate(int year, int month, int dayOfMonth) {
        final int MIN_MONTH = 0, MAX_MONTH = 11;

        if (month < MIN_MONTH || month > MAX_MONTH) {
            throw new IllegalArgumentException(
                    "Months (input param) must be " + MIN_MONTH + " <= month <= " + MAX_MONTH);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int actualMinDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int actualMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        if (dayOfMonth < actualMinDay && dayOfMonth > actualMaxDay) {
            throw new IllegalArgumentException(
                    "For month " + month + " (month 0 is January) in year " + year
                            + " the day (input param) must be " + actualMinDay + " <= day <= " + actualMaxDay);
        }

        calendar.set(year, month, dayOfMonth);

        return calendar.getTime();
    }

    /**
     * @param date The input date to be formatted.
     * @return The short-formatted {@link String} representation (dd-MMM-yyyy) for
     * the input {@link Date}.
     */
    @NonNull
    public static String getShortFormattedDate(@NonNull Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                ResourceHelper.getResString(R.string.date_short_format), Locale.getDefault());
        return sdf.format(Objects.requireNonNull(date));
    }

    /**
     * @return the {@link Date} for today.
     */
    @NonNull
    public static Date getTodayDate() {
        Calendar today = Calendar.getInstance();
        return getDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Increments of one day the given {@link Date}.
     *
     * @param date A {@link Date}
     * @return The next day.
     */
    public static Date getNextDay(@NonNull Date date) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.DATE, 1); // add one day
        return calendar.getTime();
    }

    /**
     * @param date Input date.
     * @return the start of day {@link Date} for the given {@link Date}.
     */
    @NonNull
    public static Date getStartOfDay(@NonNull Date date) {
        Calendar calendar = dateToCalendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * @param date Input date.
     * @return the end of day {@link Date} for the given {@link Date}.
     */
    @NonNull
    public static Date getEndOfDay(@NonNull Date date) {
        Calendar calendar = dateToCalendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    @NonNull
    private static Calendar dateToCalendar(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(date));
        return calendar;
    }
}
