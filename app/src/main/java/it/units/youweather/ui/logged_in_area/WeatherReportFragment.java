package it.units.youweather.ui.logged_in_area;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentWeatherReportBinding;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.utils.Timing;

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

    /**
     * TAG for logger.
     */
    private static final String TAG = WeatherViewerFragment.class.getSimpleName();

    // TODO: Rename and change types of parametersù

    // TODO: make layout scrollable

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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentWeatherReportBinding viewBinding = FragmentWeatherReportBinding.inflate(inflater);

        new Thread(() -> {
            Drawable imageTmp = null;
            if (weatherReport.getPicture() != null) {
                imageTmp = new BitmapDrawable(getResources(), weatherReport.getPicture().getBitmap());
            } else {
                try (InputStream iconIS =
                             new URL(weatherReport.getWeatherCondition().getIconUrl()).openStream()) {
                    imageTmp = Drawable.createFromStream(iconIS, "weatherIcon");
                } catch (IOException exception) {
                    Log.e(TAG, "Weather icon not showed due to an exception", exception);
                    imageTmp = ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_wb_sunny_24);    // TODO: replace with app icon
                }
            }

            double reportLatitude = weatherReport.getCoordinates().getLat();
            double reportLongitude = weatherReport.getCoordinates().getLon();

            assert imageTmp != null;
            final Drawable image = imageTmp;    // copy image to effectively final variable

            Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    viewBinding.reportImageOrWeatherConditionIcon.setImageDrawable(image);
                    viewBinding.cityName.setText(getString(R.string.city, weatherReport.getCity().toString()));
                    viewBinding.weatherDescription.setText(weatherReport.getWeatherCondition().getDescription());
                    viewBinding.coordinates.setText(getString(R.string.coordinates, reportLatitude, reportLongitude));
                    viewBinding.reportedDateTime.setText(getString(R.string.reported_on_date, Timing.convertEpochMillisToFormattedDate(weatherReport.getMillisecondsSinceEpoch())));
                });
            }

        }).start();

        viewBinding.reportImageOrWeatherConditionIcon
                .setOnClickListener(view_ -> showFullScreenWeatherReportImage(viewBinding));

        return viewBinding.getRoot();
    }

    /**
     * Shows the weather report image to full screen.
     * Adapted from <a href="https://stackoverflow.com/a/12089733/17402378">here</a>.
     */
    @SuppressLint("ResourceAsColor")
    private void showFullScreenWeatherReportImage(FragmentWeatherReportBinding viewBinding) {
        Dialog imageFullScreenPreview = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        imageFullScreenPreview.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imageFullScreenPreview.setContentView(R.layout.preview_image);
        imageFullScreenPreview.show();
        ImageView ivPreview = (ImageView) imageFullScreenPreview.findViewById(R.id.preview_image);
        ivPreview.setImageDrawable(viewBinding.reportImageOrWeatherConditionIcon.getDrawable());
        ivPreview.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
    }
}