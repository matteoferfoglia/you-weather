package it.units.youweather.ui.logged_in_area;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentUserPageWithHistoryBinding;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.storage.helpers.DBHelper;

/**
 * Fragment containing user's info and the history of her/his
 * weather reports.
 * This page should allow to filter past reports in a
 * range of date-time and exhibits a link to a fragment
 * where the same history data can be seen on a map.
 */
public class UserPageWithHistoryFragment extends Fragment {

    /**
     * The TAG for the logger.
     */
    private static final String TAG = UserPageWithHistoryFragment.class.getSimpleName();

    private FragmentUserPageWithHistoryBinding viewBinding;

    private List<WeatherReport> weatherReports;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentUserPageWithHistoryBinding.inflate(getLayoutInflater());


        // navigation to the map containing user's report history

        NavHostFragment navHostController = (NavHostFragment)
                requireActivity()
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_container_activity_main);
        NavController navController = Objects.requireNonNull(navHostController).getNavController();

        viewBinding.goToMapButton.setOnClickListener(_view ->
                navController.navigate(R.id.action_userPageWithHistoryFragment_to_mapWithReportHistoryFragment));

        // end - navigation to the map containing user's report history


        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getReportsFromDBAndPopulateView();

    }

    private void getReportsFromDBAndPopulateView() {
        new Thread(() -> {
            WeatherReport.registerThisClassForDB();
            DBHelper.pull(WeatherReport.class,
                    retrievedWeatherReports -> {
                        this.weatherReports = new ArrayList<>(Objects.requireNonNull(retrievedWeatherReports));
                        this.weatherReports.sort((a, b) -> (int) (a.getMillisecondsSinceEpoch() - b.getMillisecondsSinceEpoch()));
                        Log.i(TAG, retrievedWeatherReports.size() + " elements retrieved from the DB");

                        List<TableRow> sortedTableRowList = new ArrayList<>();
                        for (WeatherReport wr : Objects.requireNonNull(weatherReports)) {
                            final TableRow tableRow = new TableRow(requireContext());
                            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            final String dateTime = Timing.convertEpochMillisToFormattedDate(wr.getMillisecondsSinceEpoch());
                            final String location = wr.getCity().toString();
                            final String weather = wr.getWeatherConditionToString();
                            for (String cellContent : new String[]{dateTime, location, weather}) {
                                final TextView tableCell = new TextView(requireContext());
                                tableCell.setText(cellContent);
                                tableCell.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT));
                                tableCell.setGravity(Gravity.CENTER);
                                tableRow.addView(tableCell);
                            }

                            sortedTableRowList.add(tableRow);
                        }

                        requireActivity().runOnUiThread(() -> {
                            for (TableRow tr : sortedTableRowList) {
                                viewBinding.historyReportsTable.addView(tr);
                            }
                        });
                    },
                    () -> {
                        String errorMsg = getString(R.string.Unable_to_retrieve_entities_from_DB);
                        Log.e(TAG, errorMsg);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show());
                    });
        }).start();

    }
}