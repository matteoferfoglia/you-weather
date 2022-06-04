package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import it.units.youweather.R;
import it.units.youweather.entities.City;
import it.units.youweather.utils.ResourceHelper;
import it.units.youweather.utils.Timing;

/**
 * @author Matteo Ferfoglia
 */
public class WeatherCondition implements Serializable {

    // See https://openweathermap.org/weather-conditions for description

    private enum DAY_NIGHT {
        DAY("d"), NIGHT("n");
        private final String initialLetter;

        DAY_NIGHT(String initialLetter) {
            this.initialLetter = initialLetter;
        }

        public String getInitialLetter() {
            return initialLetter;
        }
    }

    /**
     * {@link Map} with valid weather conditions (IDs as keys).
     * The keys of this map are given by the {@link #id} of the
     * {@link WeatherCondition} in the corresponding value, followed by
     * the {@link DAY_NIGHT#initialLetter} of {@link DAY_NIGHT#DAY}
     * if the instance refers to daily weather condition, or the
     * {@link DAY_NIGHT#initialLetter} of {@link DAY_NIGHT#NIGHT} for
     * a nightly one.
     */
    private static final Map<String, WeatherCondition> weatherConditions = populateWeatherDescriptions();

    /**
     * @return The {@link ArrayList} with the descriptions for aby weather conditions.
     */
    public static List<String> getWeatherDescriptions() {

        Set<String> weatherConditionsDescriptions = new HashSet<>();    // Set to avoid duplicates
        for (WeatherCondition w : weatherConditions.values()) {
            weatherConditionsDescriptions.add(w.description);
        }
        List<String> weatherConditionsDescriptionsList =
                new ArrayList<>(weatherConditionsDescriptions);
        Collections.sort(weatherConditionsDescriptionsList);

        return weatherConditionsDescriptionsList;
    }

    /**
     * @param description The index of the description. The description must be
     *                    valid, i.e., it must be present in the {@link List}
     *                    returned by {@link #getWeatherDescriptions()}.
     * @return the array of {@link WeatherCondition}s saved in {@link #weatherConditions}
     * matching the given description.
     * @throws NoSuchElementException if the given description is not found.
     */
    public static WeatherCondition[] getInstancesForDescription(@NonNull String description) {

        WeatherCondition[] instancesToReturn = new WeatherCondition[DAY_NIGHT.values().length];

        Objects.requireNonNull(description);

        int numOfAddedInstance = 0;
        for (WeatherCondition w : weatherConditions.values()) {
            if (w.getDescription().equals(description)) {
                if (numOfAddedInstance < instancesToReturn.length) {
                    instancesToReturn[numOfAddedInstance++] = w;
                } else {
                    throw new IllegalStateException("More instances than expected");
                }
            }
        }

        if (numOfAddedInstance == 0) {
            throw new NoSuchElementException("\"" + description + "\" not found");
        } else {
            return instancesToReturn;
        }
    }

    /**
     * To be used for initialization of {@link #weatherConditions}.
     */
    private static Map<String, WeatherCondition> populateWeatherDescriptions() {

        // constants from weather server's API
        final int clearIcon = 1;
        final int fewCloudsIcon = 2;
        final int scatteredCloudsIcon = 3;
        final int manyCloudsIcon = 4;
        final int drizzleIcon = 9;
        final int rainIcon = 10;
        final int thunderstormIcon = 11;
        final int snowIcon = 13;
        final int atmosphereIcon = 50;

        Map<String, WeatherCondition> weatherConditions = new LinkedHashMap<>();

        for (DAY_NIGHT dayNight : DAY_NIGHT.values()) {
            String iconId;
            String dayNightInitialLetter = dayNight.getInitialLetter();

            iconId = thunderstormIcon + dayNightInitialLetter;
            weatherConditions.put(200 + dayNightInitialLetter, new WeatherCondition(200, WeatherMain.THUNDERSTORM, R.string.WEATHER200, iconId));
            weatherConditions.put(201 + dayNightInitialLetter, new WeatherCondition(201, WeatherMain.THUNDERSTORM, R.string.WEATHER201, iconId));
            weatherConditions.put(202 + dayNightInitialLetter, new WeatherCondition(202, WeatherMain.THUNDERSTORM, R.string.WEATHER202, iconId));
            weatherConditions.put(210 + dayNightInitialLetter, new WeatherCondition(210, WeatherMain.THUNDERSTORM, R.string.WEATHER210, iconId));
            weatherConditions.put(211 + dayNightInitialLetter, new WeatherCondition(211, WeatherMain.THUNDERSTORM, R.string.WEATHER211, iconId));
            weatherConditions.put(212 + dayNightInitialLetter, new WeatherCondition(212, WeatherMain.THUNDERSTORM, R.string.WEATHER212, iconId));
            weatherConditions.put(221 + dayNightInitialLetter, new WeatherCondition(221, WeatherMain.THUNDERSTORM, R.string.WEATHER221, iconId));
            weatherConditions.put(230 + dayNightInitialLetter, new WeatherCondition(230, WeatherMain.THUNDERSTORM, R.string.WEATHER230, iconId));
            weatherConditions.put(231 + dayNightInitialLetter, new WeatherCondition(231, WeatherMain.THUNDERSTORM, R.string.WEATHER231, iconId));
            weatherConditions.put(232 + dayNightInitialLetter, new WeatherCondition(232, WeatherMain.THUNDERSTORM, R.string.WEATHER232, iconId));

            iconId = drizzleIcon + dayNight.getInitialLetter();
            weatherConditions.put(300 + dayNightInitialLetter, new WeatherCondition(300, WeatherMain.DRIZZLE, R.string.WEATHER300, iconId));
            weatherConditions.put(301 + dayNightInitialLetter, new WeatherCondition(301, WeatherMain.DRIZZLE, R.string.WEATHER301, iconId));
            weatherConditions.put(302 + dayNightInitialLetter, new WeatherCondition(302, WeatherMain.DRIZZLE, R.string.WEATHER302, iconId));
            weatherConditions.put(310 + dayNightInitialLetter, new WeatherCondition(310, WeatherMain.DRIZZLE, R.string.WEATHER310, iconId));
            weatherConditions.put(311 + dayNightInitialLetter, new WeatherCondition(311, WeatherMain.DRIZZLE, R.string.WEATHER311, iconId));
            weatherConditions.put(313 + dayNightInitialLetter, new WeatherCondition(313, WeatherMain.DRIZZLE, R.string.WEATHER313, iconId));
            weatherConditions.put(312 + dayNightInitialLetter, new WeatherCondition(312, WeatherMain.DRIZZLE, R.string.WEATHER312, iconId));
            weatherConditions.put(314 + dayNightInitialLetter, new WeatherCondition(314, WeatherMain.DRIZZLE, R.string.WEATHER314, iconId));
            weatherConditions.put(321 + dayNightInitialLetter, new WeatherCondition(321, WeatherMain.DRIZZLE, R.string.WEATHER321, iconId));

            iconId = rainIcon + dayNight.getInitialLetter();
            weatherConditions.put(500 + dayNightInitialLetter, new WeatherCondition(500, WeatherMain.RAIN, R.string.WEATHER500, iconId));
            weatherConditions.put(501 + dayNightInitialLetter, new WeatherCondition(501, WeatherMain.RAIN, R.string.WEATHER501, iconId));
            weatherConditions.put(502 + dayNightInitialLetter, new WeatherCondition(502, WeatherMain.RAIN, R.string.WEATHER502, iconId));
            weatherConditions.put(503 + dayNightInitialLetter, new WeatherCondition(503, WeatherMain.RAIN, R.string.WEATHER503, iconId));
            weatherConditions.put(504 + dayNightInitialLetter, new WeatherCondition(504, WeatherMain.RAIN, R.string.WEATHER504, iconId));
            weatherConditions.put(520 + dayNightInitialLetter, new WeatherCondition(520, WeatherMain.RAIN, R.string.WEATHER520, iconId));
            weatherConditions.put(521 + dayNightInitialLetter, new WeatherCondition(521, WeatherMain.RAIN, R.string.WEATHER521, iconId));
            weatherConditions.put(522 + dayNightInitialLetter, new WeatherCondition(522, WeatherMain.RAIN, R.string.WEATHER522, iconId));
            weatherConditions.put(531 + dayNightInitialLetter, new WeatherCondition(531, WeatherMain.RAIN, R.string.WEATHER531, iconId));

            iconId = snowIcon + dayNight.getInitialLetter();
            weatherConditions.put(511 + dayNightInitialLetter, new WeatherCondition(511, WeatherMain.RAIN/*freezing rain*/, R.string.WEATHER511, iconId));
            weatherConditions.put(600 + dayNightInitialLetter, new WeatherCondition(600, WeatherMain.SNOW, R.string.WEATHER600, iconId));
            weatherConditions.put(601 + dayNightInitialLetter, new WeatherCondition(601, WeatherMain.SNOW, R.string.WEATHER601, iconId));
            weatherConditions.put(602 + dayNightInitialLetter, new WeatherCondition(602, WeatherMain.SNOW, R.string.WEATHER602, iconId));
            weatherConditions.put(611 + dayNightInitialLetter, new WeatherCondition(611, WeatherMain.SNOW, R.string.WEATHER611, iconId));
            weatherConditions.put(612 + dayNightInitialLetter, new WeatherCondition(612, WeatherMain.SNOW, R.string.WEATHER612, iconId));
            weatherConditions.put(613 + dayNightInitialLetter, new WeatherCondition(613, WeatherMain.SNOW, R.string.WEATHER613, iconId));
            weatherConditions.put(615 + dayNightInitialLetter, new WeatherCondition(615, WeatherMain.SNOW, R.string.WEATHER615, iconId));
            weatherConditions.put(616 + dayNightInitialLetter, new WeatherCondition(616, WeatherMain.SNOW, R.string.WEATHER616, iconId));
            weatherConditions.put(620 + dayNightInitialLetter, new WeatherCondition(620, WeatherMain.SNOW, R.string.WEATHER620, iconId));
            weatherConditions.put(621 + dayNightInitialLetter, new WeatherCondition(621, WeatherMain.SNOW, R.string.WEATHER621, iconId));
            weatherConditions.put(622 + dayNightInitialLetter, new WeatherCondition(622, WeatherMain.SNOW, R.string.WEATHER622, iconId));

            iconId = atmosphereIcon + dayNight.getInitialLetter();
            weatherConditions.put(701 + dayNightInitialLetter, new WeatherCondition(701, WeatherMain.ATMOSPHERE, R.string.WEATHER701, iconId));
            weatherConditions.put(711 + dayNightInitialLetter, new WeatherCondition(711, WeatherMain.ATMOSPHERE, R.string.WEATHER711, iconId));
            weatherConditions.put(721 + dayNightInitialLetter, new WeatherCondition(721, WeatherMain.ATMOSPHERE, R.string.WEATHER721, iconId));
            weatherConditions.put(731 + dayNightInitialLetter, new WeatherCondition(731, WeatherMain.ATMOSPHERE, R.string.WEATHER731, iconId));
            weatherConditions.put(741 + dayNightInitialLetter, new WeatherCondition(741, WeatherMain.ATMOSPHERE, R.string.WEATHER741, iconId));
            weatherConditions.put(751 + dayNightInitialLetter, new WeatherCondition(751, WeatherMain.ATMOSPHERE, R.string.WEATHER751, iconId));
            weatherConditions.put(761 + dayNightInitialLetter, new WeatherCondition(761, WeatherMain.ATMOSPHERE, R.string.WEATHER761, iconId));
            weatherConditions.put(762 + dayNightInitialLetter, new WeatherCondition(762, WeatherMain.ATMOSPHERE, R.string.WEATHER762, iconId));
            weatherConditions.put(771 + dayNightInitialLetter, new WeatherCondition(771, WeatherMain.ATMOSPHERE, R.string.WEATHER771, iconId));
            weatherConditions.put(781 + dayNightInitialLetter, new WeatherCondition(781, WeatherMain.ATMOSPHERE, R.string.WEATHER781, iconId));

            iconId = clearIcon + dayNight.getInitialLetter();
            weatherConditions.put(800 + dayNightInitialLetter, new WeatherCondition(800, WeatherMain.CLEAR, R.string.WEATHER800, iconId));

            iconId = fewCloudsIcon + dayNight.getInitialLetter();
            weatherConditions.put(801 + dayNightInitialLetter, new WeatherCondition(801, WeatherMain.CLOUDS, R.string.WEATHER801, iconId));
            iconId = scatteredCloudsIcon + dayNight.getInitialLetter();
            weatherConditions.put(802 + dayNightInitialLetter, new WeatherCondition(802, WeatherMain.CLOUDS, R.string.WEATHER802, iconId));
            iconId = manyCloudsIcon + dayNight.getInitialLetter();
            weatherConditions.put(803 + dayNightInitialLetter, new WeatherCondition(803, WeatherMain.CLOUDS, R.string.WEATHER803, iconId));
            weatherConditions.put(804 + dayNightInitialLetter, new WeatherCondition(804, WeatherMain.CLOUDS, R.string.WEATHER804, iconId));

        }

        return weatherConditions;
    }

    private int id;
    private String main;
    private String description;
    private String icon; // Each description provides both the icon for the day and for the night.
    //                      It would be better to have separate fields for daily and nightly icon,
    //                      but the server sends an object of this type, so this Java uses the
    //                      same fields to deserialize the object received from the server directly

    /**
     * Each possible weather condition owns an icon for the day and another
     * for the night. This method returns the URL (as {@link String} for the
     * daily icon of the instance having the given description.
     *
     * @param description The {@link #description} for the {@link WeatherCondition}
     *                    for which the icon is desired.
     * @return the URL of the daily icon for the current {@link WeatherCondition}.
     * @throws NoSuchElementException if the icon is not available.
     */
    @NonNull
    public static String getDailyIconUrl(@NonNull String description) {
        return getIconURLFromDescription(description, DAY_NIGHT.DAY);
    }

    private static String getIconURLFromDescription(
            @NonNull String weatherDescription, @NonNull DAY_NIGHT dayNight) {
        return getInstancesForDescription(Objects.requireNonNull(weatherDescription))[0]
                .getIconForDayOrNight(dayNight);
    }

    /**
     * Like {@link #getDailyIconUrl(String)}, but for the night.
     */
    @NonNull
    public static String getNightlyIconUrl(@NonNull String description) {
        return getIconURLFromDescription(description, DAY_NIGHT.NIGHT);
    }

    @NonNull
    private String getIconForDayOrNight(DAY_NIGHT dayNight) {
        WeatherCondition storedInstance =
                weatherConditions.get(id + Objects.requireNonNull(dayNight).getInitialLetter());
        if (storedInstance == null) {
            throw new NoSuchElementException("Not a valid weather condition for the icon");
        } else {
            return Objects.requireNonNull(storedInstance.getIconUrl());
        }
    }

    /**
     * @param weatherDescription The {@link #description} for the weather.
     * @param city               The {@link City} for which the weather refers: it is used to
     *                           detect sunrise and sunset times to get the correct icon.
     * @return The URL (as {@link String}) for the given weather description.
     */
    public static String getIconUrlForDescription(
            @NonNull String weatherDescription, @NonNull City city) {

        final int SECONDS_TO_MILLIS_FACTOR = 1_000;
        final long currentSecondsSinceEpoch = Timing.getMillisSinceEpoch() / SECONDS_TO_MILLIS_FACTOR;
        long sunriseAtCityInSecondsSinceEpoch = city.getSunriseUTCTimeInSecondsSinceEpochOrInvalidInitialization();
        long sunsetAtCityInSecondsSinceEpoch = city.getSunsetUTCTimeInSecondsSinceEpochOrInvalidInitialization();

        if (sunriseAtCityInSecondsSinceEpoch == Timing.epochTimeInvalidInitialization
                || sunsetAtCityInSecondsSinceEpoch == Timing.epochTimeInvalidInitialization) {  // unknown sunrise or sunset time

            // Set canonical times for sunset and sunrise

            long millisFromEpochTo8am, millisFromEpochTo8pm;
            {
                Calendar eightAMOfToday = Calendar.getInstance();
                eightAMOfToday.set(Calendar.HOUR, 8);
                eightAMOfToday.set(Calendar.MINUTE, 0);
                eightAMOfToday.set(Calendar.SECOND, 0);
                eightAMOfToday.set(Calendar.MILLISECOND, 0);

                Calendar eightPMOfToday = Calendar.getInstance();
                eightPMOfToday.setTime(eightAMOfToday.getTime());
                eightPMOfToday.add(Calendar.HOUR, 12);

                millisFromEpochTo8am = eightAMOfToday.getTimeInMillis();
                millisFromEpochTo8pm = eightPMOfToday.getTimeInMillis();
            }

            sunriseAtCityInSecondsSinceEpoch = millisFromEpochTo8am / SECONDS_TO_MILLIS_FACTOR;
            sunsetAtCityInSecondsSinceEpoch = millisFromEpochTo8pm / SECONDS_TO_MILLIS_FACTOR;

        }

        boolean isDay =
                sunriseAtCityInSecondsSinceEpoch <= currentSecondsSinceEpoch
                        && currentSecondsSinceEpoch <= sunsetAtCityInSecondsSinceEpoch;

        if (isDay) {
            return getDailyIconUrl(Objects.requireNonNull(weatherDescription));
        } else {
            return getNightlyIconUrl(Objects.requireNonNull(weatherDescription));
        }
    }

    private WeatherCondition() {
    }

    private WeatherCondition(
            int id, @NonNull WeatherMain main, int descriptionId, @NonNull String iconId) {
        this.id = id;
        this.main = Objects.requireNonNull(main).getMainDescription();
        this.description = ResourceHelper.getResString(descriptionId);

        // icon ids must have 2 digits and a letter
        final int fixedNumOfDigitsForIconId = 3;
        this.icon = new String(new char[fixedNumOfDigitsForIconId - iconId.length()])
                .replace('\0', '0') + iconId;
    }

    /**
     * @return the url (as a {@link String} to retrieve the icon for this
     * {@link WeatherCondition}.
     */
    public String getIconUrl() {
        return "http://openweathermap.org/img/wn/" + icon + "@4x.png";
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    @NonNull
    @Override
    public String toString() {
        return "WeatherCondition{" + "id=" + id + ", main=" + main + ", description=" + description + ", icon=" + icon + '}';
    }

    enum WeatherMain {
        THUNDERSTORM("Thunderstorm"),
        DRIZZLE("Drizzle"),
        RAIN("Rain"),
        SNOW("Snow"),
        ATMOSPHERE("Atmosphere"),
        CLEAR("Clear"),
        CLOUDS("Clouds");

        private final String mainDescription;

        WeatherMain(@NonNull String weatherMainDescription) {
            this.mainDescription = weatherMainDescription;
        }

        public String getMainDescription() {
            return mainDescription;
        }
    }

}
