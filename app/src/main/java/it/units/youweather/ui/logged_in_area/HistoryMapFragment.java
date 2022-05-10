package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.units.youweather.R;

/**
 * Shows the history of weather reports of the currently signed-in
 * user on the map.
 */
public class HistoryMapFragment extends Fragment {  // TODO : reach this fragment via navigation wehn the user click on "see on the map" button

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_map, container, false);
    }
}