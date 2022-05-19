package it.units.youweather.ui.logged_in_area;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentHomeBinding;
import it.units.youweather.entities.City;
import it.units.youweather.ui.LoginActivity;
import it.units.youweather.utils.LocationHelper;
import it.units.youweather.utils.Utility;
import it.units.youweather.utils.auth.Authentication;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    /**
     * TAG for logger.
     */
    private final static String TAG = HomeFragment.class.getSimpleName();
    private FragmentHomeBinding viewBinding;

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
                            String[] cityNames = Utility.map(cities, City::toString);
                            LocationNamesAdapter cityNamesArrayAdapter = new LocationNamesAdapter(
                                    cityNames != null ? cityNames : new String[0]);
                            requireActivity().runOnUiThread(() ->
                                    viewBinding.searchBarResults.setAdapter(cityNamesArrayAdapter));
                            Log.d(TAG, "Consumed cities: " + Arrays.toString(cities));

                            recyclerView.addOnItemTouchListener(
                                    new RecyclerView.OnItemTouchListener() {
                                        @Override
                                        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                                            if (e.getAction() == MotionEvent.ACTION_UP) {
                                                Log.d(TAG, "onInterceptTouchEvent: " + Arrays.toString(cities));
                                                // TODO: see logcat: why multiple prints??
                                                // TODO: when an item of the search bar results is clicked,
                                                //  ask to the server for the weather forecast at the
                                                //  given place and show them in a small below here below.
                                                //  Add a small start to add the view to the favourite locations
                                            }
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
        viewBinding.authButton.setOnClickListener(_view -> signOut());
    }

    /**
     * Adapter with a nested ViewHolder that displays a list of data.
     * Adapted from <a href="https://developer.android.com/guide/topics/ui/layout/recyclerview">here</a>.
     */
    private static class LocationNamesAdapter extends RecyclerView.Adapter<LocationNamesAdapter.ViewHolder> {

        /**
         * Data for the adapter.
         */
        String[] localDataset;

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
         * @param dataSet {@link String[]} containing the data to populate views to be used
         *                by RecyclerView.
         */
        public LocationNamesAdapter(@NonNull String[] dataSet) {
            localDataset = Objects.requireNonNull(dataSet);
        }

        /**
         * Create new views (invoked by the layout manager)
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.text_row_item_in_dropdown, parent, false));
        }

        /**
         * Replaces the contents of a view (invoked by the layout manager).
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            holder.getTextView().setText(localDataset[position]);
        }

        @Override
        public int getItemCount() {
            return localDataset.length;
        }

    }

}