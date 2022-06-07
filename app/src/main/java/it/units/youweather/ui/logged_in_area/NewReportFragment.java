package it.units.youweather.ui.logged_in_area;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentNewReportBinding;
import it.units.youweather.entities.City;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.entities.storage.WeatherReportPreview;
import it.units.youweather.utils.ImagesHelper;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.SharedData;
import it.units.youweather.utils.Stoppable;
import it.units.youweather.utils.Utility;
import it.units.youweather.utils.auth.Authentication;
import it.units.youweather.utils.storage.DBHelper;

/**
 * Fragment allowing the user to insert a new report
 * for the current weather at her/his current location.
 */
public class NewReportFragment extends Fragment {

    /**
     * TAG for the logger.
     */
    private static final String TAG = NewReportFragment.class.getSimpleName();

    /**
     * Saves and updates the {@link City} for the user's current position.
     * This is the city chosen by the user from the drop down menu.
     */
    private City cityMatchingCurrentUserPosition = null;

    /**
     * Saves and keep updated the latitude.
     */
    private double latitude;

    /**
     * Saves and keep updated the longitude.
     */
    private double longitude;

    /**
     * View-binding with the view.
     */
    private FragmentNewReportBinding viewBinding;

    /**
     * Location listener for location features. To be stopped because
     * it might own this activity leading to memory leak after closing
     * the activity.
     */
    private Stoppable locationListener;

    /**
     * The {@link String} description for the current selected
     * {@link WeatherCondition} from the Spinner.
     */
    private AtomicReference<String> currentSelectedWeatherCondition;

    /**
     * Saves the previous id for the spinner of the weather condition.
     */
    private long oldIconIdInWeatherConditionSpinner = -1;

    private ViewGroup container;

    public NewReportFragment() {  // public no-args constructor
    }

    public static NewReportFragment newInstance() {
        Bundle args = new Bundle();
        NewReportFragment fragment = new NewReportFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (viewBinding != null) {
            viewBinding.locationName.setAdapter(null);
            viewBinding.locationName.setOnItemSelectedListener(null);
            viewBinding.weatherConditionSpinner.setAdapter(null);
            viewBinding.weatherConditionSpinner.setOnItemSelectedListener(null);
        }
        if (locationListener != null) {
            locationListener.stop();    // to be stopped because it might own this activity and lead to memory leak
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        viewBinding = FragmentNewReportBinding.inflate(getLayoutInflater());

        // Save the container for this fragment
        this.container = container;

        viewBinding.weatherConditionIcon.setVisibility(View.INVISIBLE); // will become visible after setting the last known weather condition

        // Get picture from other fragment
        final ImagesHelper.SerializableBitmap[] serializableBitmaps = new ImagesHelper.SerializableBitmap[1];   // array to make it final
        assert TakeAPhotoFragment.CAPTURED_PHOTO_REQUEST_KEY != null;
        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(TakeAPhotoFragment.CAPTURED_PHOTO_REQUEST_KEY, this,
                        (requestKey, bundle) -> {
                            Serializable capturedPictureObj = bundle.getSerializable(TakeAPhotoFragment.CAPTURED_PHOTO_BUNDLE_KEY);
                            if (capturedPictureObj instanceof ImagesHelper.SerializableBitmap) {
                                serializableBitmaps[0] = (ImagesHelper.SerializableBitmap) capturedPictureObj;
                            }
                        });

        // Insertion button
        viewBinding.insertNewReportButton.setOnClickListener(view_ -> {

            showOrHideProgressLoader(true, R.string.loading);

            new Thread(() -> {

                if (cityMatchingCurrentUserPosition != null) {
                    WeatherCondition wcToSaveOnDb = Objects.requireNonNull(
                            WeatherCondition.getInstanceForDescription(
                                    (String) viewBinding.weatherConditionSpinner.getSelectedItem(), cityMatchingCurrentUserPosition));
                    WeatherReport weatherReport = new WeatherReport(
                            Authentication.getCurrentlySignedInUserOrNull(requireContext()).getUserId(),
                            cityMatchingCurrentUserPosition,
                            new Coordinates(latitude, longitude),
                            wcToSaveOnDb,
                            serializableBitmaps[0]);
                    Runnable unableToPushErrorHandler = () -> {
                        showOrHideProgressLoader(false, R.string.blank_string);
                        Log.e(TAG, "Unable to push to DB " + weatherReport);
                    };
                    DBHelper.push(
                            weatherReport,
                            () -> {

                                WeatherReportPreview weatherReportPreview =
                                        new WeatherReportPreview(weatherReport.getId(), weatherReport);

                                DBHelper.push(weatherReportPreview,
                                        () -> {
                                            Log.d(TAG, "Pushed to DB " + weatherReport);
                                            Toast.makeText(requireContext(), R.string.weather_report_added, Toast.LENGTH_LONG)
                                                    .show();
                                            recreateThisFragment();
                                        },
                                        unableToPushErrorHandler);

                            },
                            unableToPushErrorHandler);
                } else {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(requireContext(), R.string.cannot_insert_without_location, Toast.LENGTH_LONG)
                                        .show());
                    }
                }

            }).start();
        });

        return viewBinding.getRoot();
    }

    private void recreateThisFragment() {
        Activity activity = getActivity();
        if (activity != null
                && getParentFragmentManager().getFragments().contains(this) /*check the fragment has not changed for external reasons*/) {

            // recreate this fragment
            activity.runOnUiThread(() ->
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(container.getId(), new NewReportFragment())
                            .commitNow());
        }
    }

    @Override
    public void onStart() {

        super.onStart();

        // Force Locale to update (like system language)
        Context context = requireContext();
        getResources().updateConfiguration(context.getResources().getConfiguration(), context.getResources().getDisplayMetrics());

        showOrHideProgressLoader(true, R.string.waiting_for_location);

        // Set user's current location in the view
        try {
            LocationHelper locationHelper = new LocationHelper(requireActivity());
            locationListener = locationHelper.addPositionChangeListener(newLocation -> {
                if (newLocation != null) {

                    latitude = newLocation.getLatitude();
                    longitude = newLocation.getLongitude();

                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> { // changes on the view must be executed by the main thread
                            viewBinding.locationLatitudeAndLongitude.setText(getString(R.string.latitude_and_longitude, latitude, longitude));
                        });
                    }

                    AtomicBoolean errorRetrievingLocation = new AtomicBoolean(false);
                    LocationHelper.getCitiesFromCoordinatesAndConsume(
                            new Coordinates(latitude, longitude),
                            cities -> {

                                if (errorRetrievingLocation.get()) {
                                    Log.d(TAG, "Unable to retrieve current user's location");
                                } else {

                                    Log.d(TAG, "Locations matching the user's current position: "
                                            + Arrays.toString(cities));

                                    // Saves location names
                                    String[] locationNames = new String[cities.length];
                                    for (int i = 0; i < cities.length; i++) {
                                        locationNames[i] = cities[i].toString();
                                    }

                                    // Drop-down menu for choosing the correct location among ones matching coordinates from the user's current position
                                    Activity activity_ = getActivity();
                                    if (activity_ != null) {
                                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                                requireActivity(), android.R.layout.simple_spinner_item, locationNames);
                                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                        Utility.runOnUiThread(
                                                activity_,
                                                () -> {
                                                    viewBinding.locationName.setAdapter(arrayAdapter);
                                                    showOrHideProgressLoader(false, R.string.blank_string);
                                                });
                                        viewBinding.locationName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long cityIndex) {
                                                cityMatchingCurrentUserPosition = cities[(int) cityIndex];

                                                // Reference to the current selected weather condition
                                                currentSelectedWeatherCondition = new AtomicReference<>(null);
                                                final AtomicBoolean lastKnownWeatherConditionSet = new AtomicBoolean(false);

                                                // Weather icon setter
                                                final Runnable weatherIconSetter = () -> new Thread(() -> {

                                                    final int DELAY_RETRY_MILLIS = 50;
                                                    final int MAX_NUM_OF_RETRIES = 50;
                                                    int retryNum = 0;
                                                    while (!lastKnownWeatherConditionSet.get() && retryNum++ < MAX_NUM_OF_RETRIES) {
                                                        try {
                                                            Thread.sleep(DELAY_RETRY_MILLIS);
                                                        } catch (InterruptedException e) {
                                                            Log.i(TAG, "Interrupted but not a problem", e);
                                                        }
                                                    }

                                                    String currentSelectedWeatherConditionLocal = currentSelectedWeatherCondition.get();
                                                    try {
                                                        if (currentSelectedWeatherConditionLocal == null) {
                                                            currentSelectedWeatherConditionLocal = getString(R.string.WEATHER800);
                                                        }
                                                        InputStream iconIS = new URL(WeatherCondition
                                                                .getIconUrlForDescription(currentSelectedWeatherConditionLocal, cityMatchingCurrentUserPosition))
                                                                .openStream();
                                                        Drawable weatherIcon = Drawable.createFromStream(iconIS, "weatherIcon");

                                                        Utility.runOnUiThread(
                                                                activity_,
                                                                () -> {
                                                                    viewBinding.weatherConditionIcon.setImageDrawable(weatherIcon);
                                                                    viewBinding.weatherConditionIcon.setVisibility(View.VISIBLE);
                                                                    viewBinding.progressLoaderWeatherIcon.setVisibility(View.INVISIBLE);
                                                                });

                                                    } catch (NullPointerException | IOException e) {
                                                        Log.e(TAG, "Error getting icon for weather condition \""
                                                                + currentSelectedWeatherConditionLocal + "\"", e);
                                                    } catch (NoSuchElementException noSuchElementException) {
                                                        // Exception due to delay for update Locale settings (e.g., if the user change the system language while using the system)
                                                        Log.e(TAG, "Errors with translations. Recreating this fragment.");
                                                        recreateThisFragment();
                                                    }
                                                }).start();
                                                weatherIconSetter.run();

                                                // Drop-down menu for choosing the weather condition
                                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                                        activity_, android.R.layout.simple_spinner_item, WeatherCondition.getWeatherDescriptions());
                                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                viewBinding.weatherConditionSpinner.setAdapter(arrayAdapter);
                                                String lastKnownChosenWeatherCondition = SharedData.getValue(SharedData.SharedDataName.NEW_REPORT_LAST_INSERTED_WEATHER_CONDITION);
                                                int lastKnownSpinnerPosition = arrayAdapter.getPosition(lastKnownChosenWeatherCondition);
                                                currentSelectedWeatherCondition.set(lastKnownChosenWeatherCondition);
                                                if (lastKnownSpinnerPosition >= 0) {
                                                    viewBinding.weatherConditionSpinner.setSelection(lastKnownSpinnerPosition);
                                                } else {
                                                    final int spinnerPosition_sunny = arrayAdapter.getPosition(getString(R.string.WEATHER800)); // clear sky
                                                    viewBinding.weatherConditionSpinner.setSelection(spinnerPosition_sunny);
                                                }
                                                lastKnownWeatherConditionSet.set(true);
                                                viewBinding.weatherConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        if (id != oldIconIdInWeatherConditionSpinner) {
                                                            viewBinding.progressLoaderWeatherIcon.setVisibility(View.VISIBLE);
                                                            viewBinding.weatherConditionIcon.setVisibility(View.INVISIBLE);

                                                            String selectedWeatherCondition = arrayAdapter.getItem((int) id);
                                                            currentSelectedWeatherCondition.set(selectedWeatherCondition);
                                                            weatherIconSetter.run();

                                                            SharedData.setValue(SharedData.SharedDataName.NEW_REPORT_LAST_INSERTED_WEATHER_CONDITION, selectedWeatherCondition);
                                                            oldIconIdInWeatherConditionSpinner = id;
                                                        }
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {
                                                        weatherIconSetter.run();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {
                                                if (cities.length > 0) {
                                                    cityMatchingCurrentUserPosition = cities[0];
                                                }
                                            }
                                        });
                                    }
                                }
                            },
                            errorMsgIdRes -> {
                                errorRetrievingLocation.set(true);
                                Activity activity_ = getActivity();
                                if (activity_ != null) {
                                    activity_.runOnUiThread(() ->
                                            Toast.makeText(requireContext(), errorMsgIdRes, Toast.LENGTH_LONG).show());
                                }
                            });

                }
            });
        } catch (PermissionsHelper.MissingPermissionsException e) {
            Log.i(TAG, "Missing permissions for location.");
        }

    }

    /**
     * Show or hide the progress loader from the view.
     *
     * @param showProgressLoader true to show the progress loader (and hide the rest),
     *                           false for the opposite.
     * @param textToShowResId    If you want to show the progress loader, this parameter
     *                           is for the {@link IdRes} of the text to show.
     */
    private void showOrHideProgressLoader(boolean showProgressLoader, @StringRes int textToShowResId) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                viewBinding.progressLoaderTextView.setText(textToShowResId);
                viewBinding.progressLoaderLayout.setVisibility(showProgressLoader ? View.VISIBLE : View.GONE);
                viewBinding.newReportMainLayout.setVisibility(showProgressLoader ? View.GONE : View.VISIBLE);
            });
        } else {
            Log.e(TAG, "No activity available");
        }
    }
}