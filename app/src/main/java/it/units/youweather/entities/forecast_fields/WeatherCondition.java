package it.units.youweather.entities.forecast_fields;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import it.units.youweather.R;
import it.units.youweather.utils.ActivityStaticResourceHandler;

public class WeatherCondition {
    // See https://openweathermap.org/weather-conditions for description

    /**
     * {@link Map} with valid weather conditions (IDs as keys).
     */
    private static Map<Integer, WeatherCondition> weatherConditions = null;

    /**
     * @return The {@link ArrayList} with the descriptions for aby weather conditions.
     */
    public static List<String> getWeatherDescriptions() {

        if(weatherConditions==null) {
            populateWeatherDescriptions();
        }

        ArrayList<String> weatherConditionsDescriptions = new ArrayList<>();
        for (WeatherCondition w : weatherConditions.values()) {
            weatherConditionsDescriptions.add(w.description);
        }
        Collections.sort(weatherConditionsDescriptions);

        return weatherConditionsDescriptions;
    }

    private static void populateWeatherDescriptions() {
        final int clearIcon = 1;
        final int fewCloudsIcon = 2;
        final int scatteredCloudsIcon = 3;
        final int manyCloudsIcon = 4;
        final int drizzleIcon = 9;
        final int rainIcon = 10;
        final int thunderstormIcon = 11;
        final int snowIcon = 13;
        final int atmosphereIcon = 50;

        weatherConditions = new ConcurrentHashMap<>();


        for (String dayNight : new String[]{"d", "n"}) {
            String iconId;

            iconId = thunderstormIcon + dayNight;
            weatherConditions.put(200, new WeatherCondition(200, WeatherMain.THUNDERSTORM, R.string.WEATHER200, iconId));
            weatherConditions.put(201, new WeatherCondition(201, WeatherMain.THUNDERSTORM, R.string.WEATHER201, iconId));
            weatherConditions.put(202, new WeatherCondition(202, WeatherMain.THUNDERSTORM, R.string.WEATHER202, iconId));
            weatherConditions.put(210, new WeatherCondition(210, WeatherMain.THUNDERSTORM, R.string.WEATHER210, iconId));
            weatherConditions.put(211, new WeatherCondition(211, WeatherMain.THUNDERSTORM, R.string.WEATHER211, iconId));
            weatherConditions.put(212, new WeatherCondition(212, WeatherMain.THUNDERSTORM, R.string.WEATHER212, iconId));
            weatherConditions.put(221, new WeatherCondition(221, WeatherMain.THUNDERSTORM, R.string.WEATHER221, iconId));
            weatherConditions.put(230, new WeatherCondition(230, WeatherMain.THUNDERSTORM, R.string.WEATHER230, iconId));
            weatherConditions.put(231, new WeatherCondition(231, WeatherMain.THUNDERSTORM, R.string.WEATHER231, iconId));
            weatherConditions.put(232, new WeatherCondition(232, WeatherMain.THUNDERSTORM, R.string.WEATHER232, iconId));

            iconId = drizzleIcon + dayNight;
            weatherConditions.put(300, new WeatherCondition(300, WeatherMain.DRIZZLE, R.string.WEATHER300, iconId));
            weatherConditions.put(301, new WeatherCondition(301, WeatherMain.DRIZZLE, R.string.WEATHER301, iconId));
            weatherConditions.put(302, new WeatherCondition(302, WeatherMain.DRIZZLE, R.string.WEATHER302, iconId));
            weatherConditions.put(310, new WeatherCondition(310, WeatherMain.DRIZZLE, R.string.WEATHER310, iconId));
            weatherConditions.put(311, new WeatherCondition(311, WeatherMain.DRIZZLE, R.string.WEATHER311, iconId));
            weatherConditions.put(313, new WeatherCondition(313, WeatherMain.DRIZZLE, R.string.WEATHER313, iconId));
            weatherConditions.put(312, new WeatherCondition(312, WeatherMain.DRIZZLE, R.string.WEATHER312, iconId));
            weatherConditions.put(314, new WeatherCondition(314, WeatherMain.DRIZZLE, R.string.WEATHER314, iconId));
            weatherConditions.put(321, new WeatherCondition(321, WeatherMain.DRIZZLE, R.string.WEATHER321, iconId));

            iconId = rainIcon + dayNight;
            weatherConditions.put(500, new WeatherCondition(500, WeatherMain.RAIN, R.string.WEATHER500, iconId));
            weatherConditions.put(501, new WeatherCondition(501, WeatherMain.RAIN, R.string.WEATHER501, iconId));
            weatherConditions.put(502, new WeatherCondition(502, WeatherMain.RAIN, R.string.WEATHER502, iconId));
            weatherConditions.put(503, new WeatherCondition(503, WeatherMain.RAIN, R.string.WEATHER503, iconId));
            weatherConditions.put(504, new WeatherCondition(504, WeatherMain.RAIN, R.string.WEATHER504, iconId));
            weatherConditions.put(520, new WeatherCondition(520, WeatherMain.RAIN, R.string.WEATHER520, iconId));
            weatherConditions.put(521, new WeatherCondition(521, WeatherMain.RAIN, R.string.WEATHER521, iconId));
            weatherConditions.put(522, new WeatherCondition(522, WeatherMain.RAIN, R.string.WEATHER522, iconId));
            weatherConditions.put(531, new WeatherCondition(531, WeatherMain.RAIN, R.string.WEATHER531, iconId));

            iconId = snowIcon + dayNight;
            weatherConditions.put(511, new WeatherCondition(511, WeatherMain.RAIN/*freezing rain*/, R.string.WEATHER511, iconId));
            weatherConditions.put(600, new WeatherCondition(600, WeatherMain.SNOW, R.string.WEATHER600, iconId));
            weatherConditions.put(601, new WeatherCondition(601, WeatherMain.SNOW, R.string.WEATHER601, iconId));
            weatherConditions.put(602, new WeatherCondition(602, WeatherMain.SNOW, R.string.WEATHER602, iconId));
            weatherConditions.put(611, new WeatherCondition(611, WeatherMain.SNOW, R.string.WEATHER611, iconId));
            weatherConditions.put(612, new WeatherCondition(612, WeatherMain.SNOW, R.string.WEATHER612, iconId));
            weatherConditions.put(613, new WeatherCondition(613, WeatherMain.SNOW, R.string.WEATHER613, iconId));
            weatherConditions.put(615, new WeatherCondition(615, WeatherMain.SNOW, R.string.WEATHER615, iconId));
            weatherConditions.put(616, new WeatherCondition(616, WeatherMain.SNOW, R.string.WEATHER616, iconId));
            weatherConditions.put(620, new WeatherCondition(620, WeatherMain.SNOW, R.string.WEATHER620, iconId));
            weatherConditions.put(621, new WeatherCondition(621, WeatherMain.SNOW, R.string.WEATHER621, iconId));
            weatherConditions.put(622, new WeatherCondition(622, WeatherMain.SNOW, R.string.WEATHER622, iconId));

            iconId = atmosphereIcon + dayNight;
            weatherConditions.put(701, new WeatherCondition(701, WeatherMain.ATMOSPHERE, R.string.WEATHER701, iconId));
            weatherConditions.put(711, new WeatherCondition(711, WeatherMain.ATMOSPHERE, R.string.WEATHER711, iconId));
            weatherConditions.put(721, new WeatherCondition(721, WeatherMain.ATMOSPHERE, R.string.WEATHER721, iconId));
            weatherConditions.put(731, new WeatherCondition(731, WeatherMain.ATMOSPHERE, R.string.WEATHER731, iconId));
            weatherConditions.put(741, new WeatherCondition(741, WeatherMain.ATMOSPHERE, R.string.WEATHER741, iconId));
            weatherConditions.put(751, new WeatherCondition(751, WeatherMain.ATMOSPHERE, R.string.WEATHER751, iconId));
            weatherConditions.put(761, new WeatherCondition(761, WeatherMain.ATMOSPHERE, R.string.WEATHER761, iconId));
            weatherConditions.put(762, new WeatherCondition(762, WeatherMain.ATMOSPHERE, R.string.WEATHER762, iconId));
            weatherConditions.put(771, new WeatherCondition(771, WeatherMain.ATMOSPHERE, R.string.WEATHER771, iconId));
            weatherConditions.put(781, new WeatherCondition(781, WeatherMain.ATMOSPHERE, R.string.WEATHER781, iconId));

            iconId = clearIcon + dayNight;
            weatherConditions.put(800, new WeatherCondition(800, WeatherMain.CLEAR, R.string.WEATHER800, iconId));

            iconId = fewCloudsIcon + dayNight;
            weatherConditions.put(801, new WeatherCondition(801, WeatherMain.CLOUDS, R.string.WEATHER801, iconId));
            iconId = scatteredCloudsIcon + dayNight;
            weatherConditions.put(802, new WeatherCondition(802, WeatherMain.CLOUDS, R.string.WEATHER802, iconId));
            iconId = manyCloudsIcon + dayNight;
            weatherConditions.put(803, new WeatherCondition(803, WeatherMain.CLOUDS, R.string.WEATHER803, iconId));
            weatherConditions.put(804, new WeatherCondition(804, WeatherMain.CLOUDS, R.string.WEATHER804, iconId));

        }
    }

    private int id;
    private String main;
    private String description;
    private String icon;

    private WeatherCondition() {
    }

    public WeatherCondition(
            int id, @NonNull WeatherMain main, int descriptionId, @NonNull String iconId) {
        this.id = id;
        this.main = Objects.requireNonNull(main).getMainDescription();
        this.description = ActivityStaticResourceHandler.getResString(descriptionId);
        this.icon = Objects.requireNonNull(iconId);
    }

    /**
     * @return the url (as a {@link String} to retrieve the icon for this
     * {@link WeatherCondition}.
     */
    public String getIconUrl() {
        return "http://openweathermap.org/img/wn/" + icon + "@2x.png";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

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
