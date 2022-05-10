package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.units.youweather.R;

/**
 * Fragment containing user's info and the history of her/his
 * weather reports.
 */
public class UserPageWithHistoryFragment extends Fragment { // TODO

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_page_with_history, container, false);
    }
}