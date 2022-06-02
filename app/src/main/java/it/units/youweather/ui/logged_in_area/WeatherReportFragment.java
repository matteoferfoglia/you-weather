package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import it.units.youweather.databinding.FragmentWeatherReportBinding;
import it.units.youweather.entities.storage.WeatherReport;

/**
 * {@link Fragment} used to show a {@link it.units.youweather.entities.storage.WeatherReport}.
 *
 * @author Matteo Ferfoglia
 */
public class WeatherReportFragment extends Fragment {

    /**
     * Fragment initialization parameter.
     */
    private static final String WEATHER_REPORT_ARG_NAME =
            WeatherReportFragment.class.getCanonicalName() + "weatherReport";

    // TODO: Rename and change types of parameters
    private WeatherReport weatherReport;

    private WeatherReportFragment() {
    } // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param weatherReport The {@link WeatherReport} to show.
     * @return A new instance of fragment {@link WeatherReportFragment}.
     */
    public static WeatherReportFragment newInstance(@NonNull WeatherReport weatherReport) {
        WeatherReportFragment fragment = new WeatherReportFragment();
        Bundle args = new Bundle();
        args.putSerializable(WEATHER_REPORT_ARG_NAME, Objects.requireNonNull(weatherReport));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Object weatherReportInputParamObj =
                    Objects.requireNonNull(
                            requireArguments().get(WEATHER_REPORT_ARG_NAME),
                            "Invalid null input argument");
            if (weatherReportInputParamObj instanceof WeatherReport) {
                this.weatherReport = (WeatherReport) weatherReportInputParamObj;
            } else {
                throw new IllegalArgumentException(
                        "Provided argument is not an instance of " + WeatherReport.class);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentWeatherReportBinding viewBinding = FragmentWeatherReportBinding.inflate(inflater);

        viewBinding.cityName.setText(weatherReport.getCity().getName());
        viewBinding.weatherDescription.setText(weatherReport.getWeatherCondition().getDescription());

        return viewBinding.getRoot();
    }
}