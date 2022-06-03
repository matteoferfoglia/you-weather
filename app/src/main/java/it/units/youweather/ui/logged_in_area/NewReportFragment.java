package it.units.youweather.ui.logged_in_area;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentNewReportBinding;
import it.units.youweather.entities.City;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.utils.ImagesHelper;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.ResourceHelper;
import it.units.youweather.utils.auth.Authentication;
import it.units.youweather.utils.storage.DBHelper;

/**
 * Fragment allowing the user to insert a new report
 * for the current weather at her/his current location.
 */
public class NewReportFragment extends Fragment {

    // TODO: this fragment is very slow to load on real devices (the problem is with the location)
    // TODO: try to refactor

    // TODO: on a new device, when the user has not granted the permission to use the location
    //       an error is printed in Logcat and the view does not update even after granting.
    // TODO: the same happens fot Take A Photo (which does not appear) in the NewReportFragment

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO : problems with daily/nightly icons
        // Inflate the layout for this fragment
        FragmentNewReportBinding viewBinding = FragmentNewReportBinding.inflate(getLayoutInflater());

        // Set user's current location in the view
        try {
            LocationHelper locationHelper = new LocationHelper(requireActivity());
            locationHelper.addPositionChangeListener(newLocation -> {
                if (newLocation != null) {  // TODO: test if coordinates update when location change

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

                                        activity_.runOnUiThread(() -> {
                                            viewBinding.locationName.setAdapter(arrayAdapter);
                                            viewBinding.waitingForLocationLayout.setVisibility(View.GONE);
                                            viewBinding.newReportMainLayout.setVisibility(View.VISIBLE);
                                        });
                                        viewBinding.locationName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long cityIndex) {
                                                cityMatchingCurrentUserPosition = cities[(int) cityIndex];

                                                // TODO: refactor needed

                                                // TODO : what if coordinates do not map to any city?
                                                // TODO : add fields to create a Forecast object (temperature, ...): only the location and the weather condition are mandatory, otherwise user cannot proceed with insertion

                                                // Reference to the current selected weather condition
                                                final AtomicReference<String> currentSelectedWeatherCondition = new AtomicReference<>(null);

                                                // Weather icon setter
                                                final Runnable weatherIconSetter = () -> {
                                                    new Thread(() -> {
                                                        String currentSelectedWeatherConditionLocal = currentSelectedWeatherCondition.get();
                                                        try {
                                                            if (currentSelectedWeatherConditionLocal == null) {
                                                                currentSelectedWeatherConditionLocal = ResourceHelper.getResString(R.string.WEATHER800);
                                                            }
                                                            InputStream iconIS = new URL(WeatherCondition
                                                                    .getIconUrlForDescription(currentSelectedWeatherConditionLocal, cityMatchingCurrentUserPosition))
                                                                    .openStream();
                                                            Drawable weatherIcon = Drawable.createFromStream(iconIS, "weatherIcon");

                                                            activity_.runOnUiThread(() ->
                                                                    viewBinding.weatherConditionIcon.setImageDrawable(weatherIcon));
                                                        } catch (NullPointerException | IOException e) {
                                                            Log.e(TAG, "Error getting icon for weather condition \""
                                                                    + currentSelectedWeatherConditionLocal + "\"", e);
                                                        }
                                                    }).start();
                                                };
                                                weatherIconSetter.run();

                                                // Drop-down menu for choosing the weather condition
                                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                                        activity_, android.R.layout.simple_spinner_item, WeatherCondition.getWeatherDescriptions());
                                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                final int spinnerPosition_sunny = arrayAdapter.getPosition(ResourceHelper.getResString(R.string.WEATHER800)); // clear sky
                                                viewBinding.weatherConditionSpinner.setAdapter(arrayAdapter);
                                                viewBinding.weatherConditionSpinner.setSelection(spinnerPosition_sunny);
                                                viewBinding.weatherConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {    // TODO
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                        currentSelectedWeatherCondition.set(arrayAdapter.getItem((int) id));
                                                        weatherIconSetter.run();
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
            // TODO: do not allow to insert anything if missing permissions (hide the textview
            //       for location and cords and show error message in a text view)
        }

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
            new Thread(() -> {

                if (cityMatchingCurrentUserPosition != null) {
                    WeatherReport weatherReport = new WeatherReport(
                            Authentication.getCurrentlySignedInUserOrNull(requireContext()).getUserId(),
                            cityMatchingCurrentUserPosition,
                            new Coordinates(latitude, longitude),
                            WeatherCondition.getInstancesForDescription((String) viewBinding.weatherConditionSpinner.getSelectedItem())[0/* TODO: create the real WeatherCondition instance, with the correct icon, and save it */],
                            serializableBitmaps[0]); // TODO: picture needed!!
                    DBHelper.push(
                            weatherReport,
                            () -> {
                                Log.d(TAG, "Pushed to DB " + weatherReport);
                                Toast.makeText(requireContext(), R.string.weather_report_added, Toast.LENGTH_LONG)
                                        .show();
                            },
                            () -> Log.e(TAG, "Unable to push to DB " + weatherReport));
                } else {
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(requireContext(), R.string.cannot_insert_without_location, Toast.LENGTH_LONG)
                                        .show());
                    }
                }

                Activity activity = getActivity();
                if (activity != null) {

                    // recreate this fragment
                    activity.runOnUiThread(() ->
                            getParentFragmentManager()
                                    .beginTransaction()
                                    .detach(this)
                                    .attach(this)
                                    .commitNow());  // TODO : not working, reset fragment when a new report is added
                }

            }).start();
        });

        return viewBinding.getRoot();
    }
}