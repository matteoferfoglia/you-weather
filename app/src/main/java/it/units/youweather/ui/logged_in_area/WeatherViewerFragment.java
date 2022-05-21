package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import it.units.youweather.R;
import it.units.youweather.entities.City;

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
    public final static String CITY_TO_BE_SHOWED_REQUEST_KEY = "cityToBeShowedRequestKey";

    /**
     * The bundle key for which this fragment expects to find the result
     * for the request identified by the key {@link #CITY_TO_BE_SHOWED_REQUEST_KEY}.
     * Conceptually different from {@link #CITY_TO_BE_SHOWED_REQUEST_KEY},
     * but pratically the same. This is the key to get the result from the
     * parent fragment and it is useful if more results are provided for
     * the same {@link #CITY_TO_BE_SHOWED_REQUEST_KEY}; in this case, this
     * fragment receives the city only.
     */
    public static final String CITY_TO_BE_SHOWED_BUNDLE_KEY = CITY_TO_BE_SHOWED_REQUEST_KEY;

    private static final String TAG = WeatherViewerFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather_viewer, container, false);

        Fragment fragmentContainer = ((FragmentContainerView) container.getRootView()
                .findViewById(R.id.nav_host_fragment_container_activity_main))
                .getFragment();
        fragmentContainer.getParentFragmentManager()
                .setFragmentResultListener(CITY_TO_BE_SHOWED_REQUEST_KEY, this,
                        (requestKey, bundle) -> {
                            Object dataFromOtherFragment = bundle.getSerializable(CITY_TO_BE_SHOWED_BUNDLE_KEY);
                            if (dataFromOtherFragment instanceof City) {
                                City cityToSearchForTheWeather = (City) dataFromOtherFragment;
                                Log.d(TAG, "Received: " + cityToSearchForTheWeather);
                            }
                        });

        return view;
    }
}