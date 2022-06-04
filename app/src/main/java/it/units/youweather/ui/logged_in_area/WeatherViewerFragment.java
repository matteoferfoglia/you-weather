package it.units.youweather.ui.logged_in_area;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentWeatherViewerBinding;
import it.units.youweather.entities.City;
import it.units.youweather.entities.Temperature;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.MainForecastData;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.Utility;

/**
 * Fragment used to show the weather for a specific location.
 * The {@link it.units.youweather.entities.City location} must be passed
 * to this fragment.
 * This weather <strong>must</strong> be the child fragment of the one
 * knowing the {@link it.units.youweather.entities.City} for which the
 * weather has to been shown. This is needed for the implemented
 * data exchange mechanism (which is of type "parent-to-child data
 * sending").
 *
 * @author Matteo Ferfoglia
 */
public class WeatherViewerFragment extends Fragment {

    /**
     * The request key used to implement the communication mechanism with
     * the parent fragment: the latter has to send to this fragment the
     * {@link it.units.youweather.entities.City} for which the weather has
     * to be shown.
     */
    public final static String CITY_TO_BE_SHOWN_REQUEST_KEY =
            WeatherViewerFragment.class.getCanonicalName();

    /**
     * The bundle key for which this fragment expects to find the result
     * for the request identified by the key {@link #CITY_TO_BE_SHOWN_REQUEST_KEY}.
     * Conceptually different from {@link #CITY_TO_BE_SHOWN_REQUEST_KEY},
     * but practically the same. This is the key to get the result from the
     * parent fragment and it is useful if more results are provided for
     * the same {@link #CITY_TO_BE_SHOWN_REQUEST_KEY}; in this case, this
     * fragment receives the city only.
     */
    public static final String CITY_TO_BE_SHOWN_BUNDLE_KEY = "cityToBeShownRequestKey";

    private static final String TAG = WeatherViewerFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentWeatherViewerBinding viewBinding = FragmentWeatherViewerBinding.inflate(getLayoutInflater());
        View view = viewBinding.getRoot();

        Fragment fragmentContainer =
                ((FragmentContainerView) Objects.requireNonNull(container).getRootView()
                        .findViewById(R.id.nav_host_fragment_container_activity_main))
                        .getFragment();
        assert CITY_TO_BE_SHOWN_REQUEST_KEY != null;
        fragmentContainer.getParentFragmentManager()
                .setFragmentResultListener(CITY_TO_BE_SHOWN_REQUEST_KEY, this,
                        (requestKey, bundle) -> {
                            Serializable dataFromOtherFragment = bundle.getSerializable(CITY_TO_BE_SHOWN_BUNDLE_KEY);
                            if (dataFromOtherFragment instanceof City) {
                                City cityToSearchForTheWeather = (City) dataFromOtherFragment;
                                Log.d(TAG, "Received: " + cityToSearchForTheWeather);

                                LocationHelper.getForecastForCoordinates(
                                        new Coordinates(cityToSearchForTheWeather.getLat(), cityToSearchForTheWeather.getLon()),
                                        forecast -> {
                                            WeatherCondition[] weatherConditions = forecast.getWeather();
                                            String weatherDescription_tmp = "";
                                            Drawable weatherIcon_tmp = null;
                                            if (weatherConditions.length > 0) {
                                                weatherDescription_tmp = weatherConditions[0].getDescription();
                                                try (InputStream iconIS =
                                                             new URL(weatherConditions[0].getIconUrl()).openStream()) {
                                                    weatherIcon_tmp = Drawable.createFromStream(iconIS, "weatherIcon");
                                                } catch (IOException exception) {
                                                    Log.e(TAG, "Weather icon not showed due to an exception", exception);
                                                }
                                            }

                                            MainForecastData mainForecastData = forecast.getForecastData();

                                            // Need to copy variables to make them effectively final before using in another thread
                                            final Drawable weatherIcon = weatherIcon_tmp;
                                            final String weatherDescription = weatherDescription_tmp.length() > 0   // Correct case
                                                    ? Character.toUpperCase(weatherDescription_tmp.charAt(0)) + weatherDescription_tmp.substring(1).toLowerCase()
                                                    : "";

                                            // Temperature conversions
                                            final String actualTemperature = new Temperature(mainForecastData.getTemp()).getTemperatureWithMeasureUnit();
                                            final String minTemperature = new Temperature(mainForecastData.getTemp_min()).getTemperatureWithMeasureUnit();
                                            final String maxTemperature = new Temperature(mainForecastData.getTemp_max()).getTemperatureWithMeasureUnit();

                                            Utility.runOnUiThread(
                                                    getActivity(),
                                                    () -> {

                                                        if (weatherIcon != null) {
                                                            viewBinding.weatherConditionIcon.setImageDrawable(weatherIcon);
                                                        }
                                                        viewBinding.cityName.setText(forecast.getCityName());
                                                        viewBinding.weatherDescription.setText(weatherDescription);
                                                        viewBinding.actualTemperature.setText(actualTemperature);
                                                        viewBinding.minTemperature.setText(minTemperature);
                                                        viewBinding.maxTemperature.setText(maxTemperature);

                                                        viewBinding.progressLoader.setVisibility(View.GONE);

                                                        getParentFragmentManager()
                                                                .beginTransaction()
                                                                .show(this)
                                                                .commitNow();
                                                    });
                                        },
                                        exception -> Log.e(TAG, "Error while getting forecast", exception))
                                ;
                            }
                        });

        return view;
    }
}