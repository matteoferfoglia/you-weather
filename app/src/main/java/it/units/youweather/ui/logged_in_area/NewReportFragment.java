package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.units.youweather.databinding.FragmentNewReportBinding;
import it.units.youweather.ui.MainActivity;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.Permissions;
import it.units.youweather.utils.Stoppable;

/**
 * Fragment allowing the user to insert a new report
 * for the current weather at her/his current location.
 */
public class NewReportFragment extends Fragment {

    /**
     * TAG for the logger.
     */
    private static final String TAG = NewReportFragment.class.getSimpleName();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentNewReportBinding viewBinding = FragmentNewReportBinding.inflate(getLayoutInflater());

        // Set user's current location in the view
        try {
            LocationHelper locationHelper = new LocationHelper(requireActivity());
            locationHelper.addPositionChangeListener(newLocation ->
                    requireActivity().runOnUiThread(() -> { // changes on the view must be executed by the main thread
                        if (newLocation != null) {  // TODO: test if coordinates update when location change
                            viewBinding.locationLatitude.setText(String.valueOf(newLocation.getLatitude()));
                            viewBinding.locationLongitude.setText(String.valueOf(newLocation.getLongitude()));
                        }
                    }));
        } catch (Permissions.MissingPermissionsException e) {
            Log.i(TAG, "Missing permissions for location.");
            // TODO: do not allow to insert anything if missing permissions (hide the textview
            //       for location and coords and show error message in a text view)
        }

        // TODO : insert action when click on button

        return viewBinding.getRoot();
    }
}