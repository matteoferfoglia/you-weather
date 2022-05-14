package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentUserPageWithHistoryBinding;

/**
 * Fragment containing user's info and the history of her/his
 * weather reports.
 * This page should allow to filter past reports in a
 * range of date-time and exhibits a link to a fragment
 * where the same history data can be seen on a map.
 */
public class UserPageWithHistoryFragment extends Fragment { // TODO

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentUserPageWithHistoryBinding viewBinding = FragmentUserPageWithHistoryBinding.inflate(getLayoutInflater());

        // navigation to the map containing user's report history

        NavHostFragment navHostController = (NavHostFragment)
                requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_container_activity_main);
        NavController navController = Objects.requireNonNull(navHostController).getNavController();

        viewBinding.goToMapButton.setOnClickListener(_view ->
                navController.navigate(R.id.action_userPageWithHistoryFragment_to_mapWithReportHistoryFragment));

        // navigation to the map containing user's report history

        return viewBinding.getRoot();
    }
}