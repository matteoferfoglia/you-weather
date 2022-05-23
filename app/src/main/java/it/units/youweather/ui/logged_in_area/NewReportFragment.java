package it.units.youweather.ui.logged_in_area;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentNewReportBinding;
import it.units.youweather.entities.City;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.forecast_fields.WeatherCondition;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.ResourceHelper;

/**
 * Fragment allowing the user to insert a new report
 * for the current weather at her/his current location.
 */
public class NewReportFragment extends Fragment {

    // TODO: this fragment is very slow to load

    /**
     * TAG for the logger.
     */
    private static final String TAG = NewReportFragment.class.getSimpleName();

    /**
     * Saves and updates the {@link City} for the user's current position.
     * This is the city chosen by the user from the drop down menu.
     */
    private City cityMatchingCurrentUserPosition = null;

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

                    final double newLat = newLocation.getLatitude();
                    final double newLon = newLocation.getLongitude();

                    requireActivity().runOnUiThread(() -> { // changes on the view must be executed by the main thread
                        viewBinding.locationLatitude.setText(String.valueOf(newLat));
                        viewBinding.locationLongitude.setText(String.valueOf(newLon));
                    });

                    LocationHelper.getCitiesFromCoordinatesAndConsume(
                            new Coordinates(newLat, newLon),
                            cities -> {

                                Log.d(TAG, "Locations matching the user's current position: "
                                        + Arrays.toString(cities));

                                // Saves location names
                                String[] locationNames = new String[cities.length];
                                for (int i = 0; i < cities.length; i++) {
                                    locationNames[i] = cities[i].toString();
                                }

                                // Drop-down menu for choosing the correct location among ones matching coordinates from the user's current position
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                                        requireActivity(), android.R.layout.simple_spinner_item, locationNames);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                requireActivity().runOnUiThread(() ->
                                        viewBinding.locationName.setAdapter(arrayAdapter));
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

                                                    requireActivity().runOnUiThread(() ->
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
                                                requireActivity(), android.R.layout.simple_spinner_item, WeatherCondition.getWeatherDescriptions());
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


                                        // Insertion button
                                        viewBinding.insertNewReportButton.setOnClickListener(_view -> {
                                            // TODO : insert action when click on button
                                            Log.i(TAG, "Selected: " + currentSelectedWeatherCondition.get());
                                        });

                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                        if (cities.length > 0) {
                                            cityMatchingCurrentUserPosition = cities[0];
                                        }
                                    }
                                });
                            });
                }
            });
        } catch (PermissionsHelper.MissingPermissionsException e) {
            Log.i(TAG, "Missing permissions for location.");
            // TODO: do not allow to insert anything if missing permissions (hide the textview
            //       for location and coords and show error message in a text view)
        }

        return viewBinding.getRoot();
    }
}