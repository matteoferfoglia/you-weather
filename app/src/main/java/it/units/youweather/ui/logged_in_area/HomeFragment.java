package it.units.youweather.ui.logged_in_area;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentHomeBinding;
import it.units.youweather.entities.City;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.ui.LoginActivity;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.ResourceHelper;
import it.units.youweather.utils.auth.Authentication;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    // TODO: on a new device, when the user has not granted the permission to use the location
    //       an error is printed in Logcat and the view does not update even after granting.
    // TODO: the same happens fot Take A Photo (which does not appear) in the NewReportFragment

    /**
     * TAG for logger.
     */
    private final static String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding viewBinding;

    /**
     * Cache for the current user's location.
     */
    private Location userLocation = null;

    /**
     * Signs the user out.
     */
    private void signOut() {
        Log.i(TAG, "Sign-out request");
        Authentication.signOut();
        startActivity(new Intent(requireActivity(), LoginActivity.class));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.search_bar_results);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // TODO: why  E/RecyclerView: No adapter attached; skipping layout  each time the page is loaded?

        viewBinding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                LocationHelper.getCitiesFromNameAndConsume(
                        query,
                        cities -> {
                            LocationsAdapter cityNamesArrayAdapter = new LocationsAdapter(cities, viewBinding.searchBarResults, viewBinding.searchBar);
                            requireActivity().runOnUiThread(() ->
                                    viewBinding.searchBarResults.setAdapter(cityNamesArrayAdapter));
                            Log.d(TAG, "Cities matching the query: " + Arrays.toString(cities));

                            if (cities.length == 0) {
                                final String errorMsg = ResourceHelper.getResString(R.string.no_results);
                                requireActivity()
                                        .runOnUiThread(() ->
                                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG)
                                                        .show());
                            }

                            recyclerView.addOnItemTouchListener(
                                    new RecyclerView.OnItemTouchListener() {
                                        @Override
                                        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                                            // TODO: see logcat: why multiple prints??
                                            // TODO: add a small star icon to add the view to the favourite locations (it should appear in the weather viewer fragment)
                                            // TODO: solve warnings (see them by cleaning [clean] the build and then re-build)
                                            return false;
                                        }

                                        @Override
                                        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                                        }

                                        @Override
                                        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                                        }
                                    }
                            );
                        });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        try {
            new LocationHelper(requireActivity()).addPositionChangeListener(location -> {
                if (location != null) {
                    this.userLocation = location;
                }
            });
        } catch (PermissionsHelper.MissingPermissionsException e) {
            Log.e(TAG, "Error getting user's location", e);
        }

        viewBinding.useCurrentPositionButton.setOnClickListener(view_ -> {
            new Thread(() -> {
                City[] citiesForCurrentUserPosition = userLocation == null
                        ? new City[0]
                        : LocationHelper.getCitiesFromCoordinates(
                        new Coordinates(
                                userLocation.getLatitude(),
                                userLocation.getLongitude()));
                if (citiesForCurrentUserPosition.length > 0) {
                    showWeatherForCity(getFragmentManager(requireView()), citiesForCurrentUserPosition[0]);
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), R.string.Not_found_city_for_user_position, Toast.LENGTH_LONG)
                                    .show());
                }
            }).start();
        });
        viewBinding.authButton.setOnClickListener(_view -> signOut());

        collapseKeyboardForSearchBox();
    }

    @Override
    public void onResume() {
        super.onResume();
        collapseKeyboardForSearchBox();
    }

    /**
     * Avoid the keyboard to show up by default for text insertion
     * in the search box. This method should be invoked in {@link #onViewCreated(View, Bundle)}
     * and in {@link #onResume()} to avoid the keyboard to automatic show up.
     * The desired behaviour is that the keyboard shows up only after the user
     * clicked on the search box.
     */
    private void collapseKeyboardForSearchBox() {
        viewBinding.searchBar.clearFocus();
        requireView().getRootView().requestFocus();
        viewBinding.searchBar.onActionViewCollapsed();
    }

    /**
     * Adapter with a nested ViewHolder that displays a list of data.
     * Adapted from <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">here</a>
     * and <a href="https://stackoverflow.com/a/24471410/17402378">here</a>.
     */
    private static class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

        /**
         * Data for the adapter.
         */
        private final City[] localDataset;

        /**
         * The {@link RecyclerView} for which this adapter is created.
         */
        private final RecyclerView searchBarResults;

        /**
         * The {@link SearchView searchbar}.
         */
        private final SearchView searchBar;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder). Bind with the XML file.
         */
        private static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView);
            }

            public TextView getTextView() {
                return textView;
            }
        }

        /**
         * Initialize the dataset of the Adapter.
         *
         * @param dataSet          {@link City}[] containing the data to populate views to be used
         *                         by RecyclerView.
         * @param searchBarResults The {@link RecyclerView} for which this adapter is created.
         * @param searchBar        The {@link SearchView searchbar}.
         */
        public LocationsAdapter(@NonNull City[] dataSet,
                                @NonNull RecyclerView searchBarResults,
                                @NonNull SearchView searchBar) {
            this.localDataset = Objects.requireNonNull(dataSet);
            this.searchBarResults = Objects.requireNonNull(searchBarResults);
            this.searchBar = Objects.requireNonNull(searchBar);
        }

        /**
         * Create new views (invoked by the layout manager)
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.text_row_item_in_dropdown, parent, false);

            FragmentManager parentFragmentManager = HomeFragment.getFragmentManager(parent.getRootView());

            view.setOnClickListener(view_ -> {  // define onClickListener for items

                // Find this item position iterating over all items of the recyclerView (parent)
                int itemPosition = -1;
                for (int i = 0; i < getItemCount(); i++) {
                    if (parent.getChildAt(i).equals(view)) {
                        itemPosition = i;
                    }
                }
                if (itemPosition > Objects.requireNonNull(localDataset).length || itemPosition < 0) {
                    throw new IllegalStateException("Impossible to get an item out of bounds");
                }
                City clickedCity = localDataset[itemPosition];

                showWeatherForCity(parentFragmentManager, clickedCity);

                // Clear the old query from the search bar
                searchBar.setQuery("", false);  // remove the old query
                searchBarResults.setAdapter(new LocationsAdapter(new City[0], searchBarResults, searchBar));   // Replace the adapter with a new one (clear previous results)
            });
            return new ViewHolder(view);
        }

        /**
         * Replaces the contents of a view (invoked by the layout manager).
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            holder.getTextView().setText(localDataset[position].toString());
        }

        @Override
        public int getItemCount() {
            return localDataset.length;
        }

    }

    /**
     * @param view Th {@link View}
     * @return The {@link FragmentManager} for the given {@link View}.
     */
    @NonNull
    private static FragmentManager getFragmentManager(@NonNull View view) {

        @SuppressLint("UseRequireInsteadOfGet") // cannot be replaced with require in this case
        FragmentContainerView navHostFragmentContainer = Objects.requireNonNull(view)
                .getRootView()
                .findViewById(R.id.nav_host_fragment_container_activity_main);

        // The parent fragment manager needed for data exchanging with other fragments
        return navHostFragmentContainer.getFragment().getParentFragmentManager();
    }

    /**
     * Pass the city for which the weather must be showed to {@link WeatherViewerFragment}
     * and show the weather.
     *
     * @param parentFragmentManager The parent fragment manager to handle
     *                              fragments.
     * @param city                  The {@link City} for which the weather forecast
     *                              must be showed.
     */
    private static void showWeatherForCity(@NonNull FragmentManager parentFragmentManager,
                                           @NonNull City city) {
        Bundle selectedLocationForWeatherViewerFragment_bundle = new Bundle();
        selectedLocationForWeatherViewerFragment_bundle
                .putSerializable(
                        WeatherViewerFragment.CITY_TO_BE_SHOWN_BUNDLE_KEY,
                        Objects.requireNonNull(city));
        Objects.requireNonNull(parentFragmentManager)
                .setFragmentResult(
                        WeatherViewerFragment.CITY_TO_BE_SHOWN_REQUEST_KEY,
                        Objects.requireNonNull(selectedLocationForWeatherViewerFragment_bundle));
    }

}