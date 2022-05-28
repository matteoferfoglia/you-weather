package it.units.youweather.ui.logged_in_area;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentUserPageWithHistoryBinding;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.utils.Conversions;
import it.units.youweather.utils.Timing;
import it.units.youweather.utils.auth.Authentication;
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

    /**
     * {@link List} of weather reports downloaded from the database.
     */
    private volatile List<WeatherReport> weatherReports;

    /**
     * {@link Map} of all {@link DatePickerDialog}s allowing the user to select a date,
     * used in this object, associated with the clickable button.
     */
    private final Map<Button, DatePickerDialog> datePickerDialogMap = new ConcurrentHashMap<>();


    /**
     * Initialize the {@link #datePickerDialogMap} for a button that, when clicked
     * on, should show the {@link #datePickerDialogMap}.
     *
     * @param selectDataButton A buttons that, when clicked, should show the data picker.
     */
    private void initDatePickerForButton(@NonNull Button selectDataButton) {
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (datePicker, year, month/*0-based index*/, dayOfMonth) -> {
                    String formattedDate = Timing.getShortFormattedDate(
                            Timing.getDate(year, month, dayOfMonth));
                    Objects.requireNonNull(selectDataButton).setText(formattedDate);
                };
        Calendar now = Calendar.getInstance();
        datePickerDialogMap.put(selectDataButton, new DatePickerDialog(
                requireContext(), android.R.style.Theme_Material_Dialog,
                onDateSetListener,
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)));

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


        viewBinding.helloText.setText(getString(
                R.string.hello,
                Objects.requireNonNull(Authentication
                        .getCurrentlySignedInUserOrNull(requireContext()))
                        .getDisplayName()));

        Button[] filterByDateButtonArray = new Button[]{viewBinding.fromDateFilterButton, viewBinding.toDateFilterButton};
        viewBinding.checkBoxFilterByDates.setChecked(false);                        // TODO: maybe remove?
        for (Button filterByDateButton : filterByDateButtonArray) {
            filterByDateButton.setVisibility(View.GONE);  // initial default value        // TODO: maybe remove?
            initDatePickerForButton(filterByDateButton);
            filterByDateButton.setOnClickListener(view_ -> openDatePicker(filterByDateButton));

            // TODO: for data picker:
            //  1) add action (e.g., Consumer to pass to initDatePickerForButton) to be invoked when
            //     the date is confirmed: for the "From date" button the action should be to hide
            //     all reports previous than the selected date, analogously for the "To date" button
            //  2) when "Filter by date" is unchecked, clear dates from buttons and show again all
            //     reports
        }

        viewBinding.checkBoxFilterByDates.setOnClickListener(view_ -> {
            boolean showFilterByDateButtons = viewBinding.checkBoxFilterByDates.isChecked();
            int filterByDateButtonsVisibility = showFilterByDateButtons ? View.VISIBLE : View.GONE;
            for (Button filterByDateButton : filterByDateButtonArray) {
                filterByDateButton.setVisibility(filterByDateButtonsVisibility);
            }
        });

        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getReportsFromDBAndPopulateView();
    }

    /**
     * Opens the {@link #datePickerDialogMap}.
     *
     * @param button The {@link Button} that, when clicked, causes the
     *               {@link DatePickerDialog} to open.
     */
    private void openDatePicker(@NonNull Button button) {
        Objects.requireNonNull(
                datePickerDialogMap.get(Objects.requireNonNull(button)),
                "Button not registered").show();
    }

    /**
     * Starts a new task to asynchronously download the data from the DB
     * and populate this view.
     */
    private void getReportsFromDBAndPopulateView() {
        new Thread(() -> {
            WeatherReport.registerThisClassForDB();
            DBHelper.pull(WeatherReport.class,
                    retrievedWeatherReports -> {
                        this.weatherReports = new ArrayList<>(Objects.requireNonNull(retrievedWeatherReports));
                        this.weatherReports.sort((a, b) -> (int) (a.getMillisecondsSinceEpoch() - b.getMillisecondsSinceEpoch()));
                        Log.i(TAG, retrievedWeatherReports.size() + " elements retrieved from the DB");

                        List<TableRow> sortedTableRowList = new ArrayList<>();

                        int rowNumber = 0;
                        for (WeatherReport wr : Objects.requireNonNull(weatherReports)) {
                            final TableRow tableRow = new TableRow(requireContext());
                            tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            rowNumber++;
                            final String dateTime = Timing.convertEpochMillisToFormattedDate(wr.getMillisecondsSinceEpoch());
                            final String location = wr.getCity().toString();
                            final String weather = wr.getWeatherConditionToString();
                            final String[] columns = new String[]{String.valueOf(rowNumber), dateTime, location, weather};
                            for (int i = 0; i < columns.length; i++) {
                                String cellContent = columns[i];
                                int cellWidthInPx = 0;  // 0 dp allows to inherits property from the container (e.g., stretchColumns)
                                if (i == 0) {  // column containing the row number
                                    @SuppressLint("ResourceType") // dimen resource is saved as string
                                    double cellWidthInDp = Double.parseDouble(getString(R.dimen.history_table_heading_row_number_width)
                                            .replaceAll("[^\\d.]", ""));
                                    cellWidthInPx = Conversions.dpToPx(cellWidthInDp);
                                }
                                final TextView tableCell = new TextView(requireContext());
                                tableCell.setText(cellContent);
                                tableCell.setLayoutParams(new TableRow.LayoutParams(cellWidthInPx, TableRow.LayoutParams.MATCH_PARENT));
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