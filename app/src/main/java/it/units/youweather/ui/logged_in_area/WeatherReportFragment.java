package it.units.youweather.ui.logged_in_area;

import android.annotation.SuppressLint;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentWeatherReportBinding;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.entities.storage.WeatherReportPreview;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.Utility;
import it.units.youweather.utils.storage.DBEntity;
import it.units.youweather.utils.storage.DBHelper;

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

    private WeatherReport weatherReport;
    private FragmentWeatherReportBinding viewBinding;

    private WeatherReportFragment() {
    } // Required empty public constructor

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param weatherReportPreview The {@link WeatherReportPreview} for the {@link WeatherReport} to show.
     * @return A new instance of fragment {@link WeatherReportFragment}.
     */
    public static WeatherReportFragment newInstance(@NonNull WeatherReportPreview weatherReportPreview) {
        WeatherReportFragment fragment = new WeatherReportFragment();
        Bundle args = new Bundle();
        args.putSerializable(WEATHER_REPORT_ARG_NAME, Objects.requireNonNull(weatherReportPreview));
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentWeatherReportBinding.inflate(inflater);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Thread(() -> {

            if (getArguments() != null) {
                Object weatherReportInputParamObj =
                        Objects.requireNonNull(
                                requireArguments().get(WEATHER_REPORT_ARG_NAME),
                                "Invalid null input argument");
                if (weatherReportInputParamObj instanceof WeatherReportPreview) {

                    WeatherReportPreview weatherReportPreview = (WeatherReportPreview) weatherReportInputParamObj;

                    DBEntity.registerThisClassForDB(WeatherReport.class);
                    DBHelper.pullByKey(
                            weatherReportPreview.getWeatherReportDetailsKey(),
                            WeatherReport.class,
                            (WeatherReport weatherReportDetails) -> {

                                new Thread(() -> {

                                    this.weatherReport = weatherReportDetails;

                                    Drawable imageTmp = null;
                                    if (weatherReport.getPicture() != null) {
                                        imageTmp = new BitmapDrawable(getResources(), weatherReport.getPicture().getBitmap());
                                    } else {
                                        try (InputStream iconIS =
                                                     new URL(weatherReport.getWeatherCondition().getIconUrl()).openStream()) {
                                            imageTmp = Drawable.createFromStream(iconIS, "weatherIcon");
                                        } catch (IOException exception) {
                                            Log.e(TAG, "Weather icon not showed due to an exception", exception);
                                            imageTmp = ContextCompat.getDrawable(requireContext(), R.drawable.app_icon);
                                        }
                                    }

                                    double reportLatitude = weatherReport.getCoordinates().getLat();
                                    double reportLongitude = weatherReport.getCoordinates().getLon();

                                    assert imageTmp != null;
                                    final Drawable image = imageTmp;    // copy image to effectively final variable

                                    {
                                        // Prepare data for the UI thread to minimize its work

                                        final String cityName = getString(R.string.city, weatherReport.getCity().toString());
                                        final String weatherDescription = weatherReport.getWeatherCondition().getDescription();
                                        final String coordinates = getString(R.string.coordinates, reportLatitude, reportLongitude);
                                        final String reportedDateTime = getString(R.string.reported_on_date, Timing.convertEpochMillisToFormattedDate(weatherReport.getMillisecondsSinceEpoch()));

                                        Utility.runOnUiThread(
                                                getActivity(),
                                                () -> {
                                                    viewBinding.reportImageOrWeatherConditionIcon.setImageDrawable(image);
                                                    viewBinding.cityName.setText(cityName);
                                                    viewBinding.weatherDescription.setText(weatherDescription);
                                                    viewBinding.coordinates.setText(coordinates);
                                                    viewBinding.reportedDateTime.setText(reportedDateTime);

                                                    viewBinding.weatherViewerMainLayout.setVisibility(View.VISIBLE);
                                                    viewBinding.progressLoader.setVisibility(View.GONE);

                                                    viewBinding.reportImageOrWeatherConditionIcon
                                                            .setOnClickListener(view_ -> showFullScreenWeatherReportImage(viewBinding));
                                                });
                                    }

                                }).start();
                            },
                            () -> {
                                Log.e(TAG, "Unable to retrieve details");
                                Toast.makeText(requireContext().getApplicationContext(), R.string.error_unable_to_retrieve_data, Toast.LENGTH_LONG)
                                        .show();
                            });


                } else {
                    throw new IllegalArgumentException(
                            "Provided argument is not an instance of " + WeatherReport.class);
                }
            }

        }).start();

    }

    /**
     * Shows the weather report image to full screen.
     * Adapted from <a href="https://stackoverflow.com/a/12089733/17402378">here</a>.
     */
    private void showFullScreenWeatherReportImage(FragmentWeatherReportBinding viewBinding) {
        Dialog imageFullScreenPreview = new Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        imageFullScreenPreview.requestWindowFeature(Window.FEATURE_NO_TITLE);
        imageFullScreenPreview.setContentView(R.layout.preview_image);
        imageFullScreenPreview.show();
        ImageView ivPreview = imageFullScreenPreview.findViewById(R.id.preview_image);
        ivPreview.setImageDrawable(viewBinding.reportImageOrWeatherConditionIcon.getDrawable());
        ivPreview.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white));
    }
}