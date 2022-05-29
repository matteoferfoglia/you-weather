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
import android.widget.DatePicker;
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
import java.util.Date;
import java.util.LinkedList;
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
import it.units.youweather.utils.functionals.Consumer;
import it.units.youweather.utils.functionals.Predicate;
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
     * {@link List} of all weather reports downloaded from the database.
     */
    private volatile List<WeatherReport> weatherReports;

    /**
     * {@link List} of showed weather reports.
     */
    private volatile List<WeatherReport> showedWeatherReports;

    /**
     * {@link Map} of all {@link DatePickerDialog}s allowing the user to select a date,
     * used in this object, associated with the clickable button.
     */
    private final Map<Button, DatePickerDialog> datePickerDialogMap = new ConcurrentHashMap<>();

    /**
     * The minimum date selected with filters for which {@link WeatherReport}s
     * must be shown.
     */
    private Date minDateFiltered;

    /**
     * The maximum date selected with filters for which {@link WeatherReport}s
     * must be shown.
     */
    private Date maxDateFiltered;

    /**
     * Initialize the {@link #datePickerDialogMap} for a button that, when clicked
     * on, should show the {@link #datePickerDialogMap}.
     *
     * @param selectDataButton   A buttons that, when clicked, should show the data picker.
     * @param chosenDateConsumer If non-null, the {@link Consumer} that will accept the
     *                           {@link Date} confirmed by the user, otherwise (if null)
     *                           it will not be executed.
     * @param minDate            The minimum selectable date for the
     *                           {@link android.widget.DatePicker}, or null for not setting it.
     * @param maxDate            The maximum selectable date for the
     *                           {@link android.widget.DatePicker}, or null for not setting it.
     */
    private void initDatePickerForButton(@NonNull Button selectDataButton,
                                         @Nullable Consumer<Date> chosenDateConsumer,
                                         @Nullable Date minDate,
                                         @Nullable Date maxDate) {
        Objects.requireNonNull(selectDataButton);
        DatePickerDialog.OnDateSetListener onDateSetListener =
                (datePicker, year, month/*0-based index*/, dayOfMonth) -> {
                    Date chosenDate = Timing.getDate(year, month, dayOfMonth);
                    String formattedDate = Timing.getShortFormattedDate(chosenDate);
                    selectDataButton.setText(formattedDate);
                    if (chosenDateConsumer != null) {
                        chosenDateConsumer.accept(chosenDate);
                    }
                };
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(), android.R.style.Theme_Material_Dialog,
                onDateSetListener,
                now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        DatePicker datePicker = datePickerDialog.getDatePicker();
        if (minDate != null) {
            datePicker.setMinDate(minDate.getTime());
        }
        if (maxDate != null) {
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        }
        datePickerDialogMap.put(selectDataButton, datePickerDialog);
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
        Consumer<Predicate<WeatherReport>> filterWeatherReportsByDateAndPopulateHistoryTable =
                keepWeatherReportPredicate -> {
                    showedWeatherReports.clear();
                    for (WeatherReport wr : Objects.requireNonNull(weatherReports)) {
                        if (keepWeatherReportPredicate.test(wr)) {
                            showedWeatherReports.add(wr);
                        }
                    }
                    populateReportHistoryTable();
                };
        initDatePickersForFilteringButtons(filterWeatherReportsByDateAndPopulateHistoryTable);
        for (Button filterByDateButton : filterByDateButtonArray) {
            filterByDateButton.setOnClickListener(view_ -> openDatePicker(filterByDateButton));
        }

        viewBinding.checkBoxFilterByDates.setOnClickListener(view_ -> {
            boolean filterByDateChecked = viewBinding.checkBoxFilterByDates.isChecked();
            int filterByDateButtonsVisibility = filterByDateChecked ? View.VISIBLE : View.GONE;
            for (Button filterByDateButton : filterByDateButtonArray) {
                filterByDateButton.setVisibility(filterByDateButtonsVisibility);
            }
            filterWeatherReportsByDateAndPopulateHistoryTable
                    .accept(filterByDateChecked ? getReportFilteredPredicate() : wr -> true);
            if (!filterByDateChecked) { // reset text of buttons and selected dates
                viewBinding.fromDateFilterButton.setText(R.string.from);
                viewBinding.toDateFilterButton.setText(R.string.to);
                initDatePickersForFilteringButtons(filterWeatherReportsByDateAndPopulateHistoryTable);
            }
        });

        return viewBinding.getRoot();
    }

    /**
     * Initializes {@link DatePicker}s associated with "from-date" and "to-date"
     * filtering buttons.
     *
     * @param filterWeatherReportsByDateAndPopulateHistoryTable The {@link Consumer} specifying how to filter weather reports by dates.
     */
    private void initDatePickersForFilteringButtons(
            Consumer<Predicate<WeatherReport>> filterWeatherReportsByDateAndPopulateHistoryTable) {
        initDatePickerForButton(viewBinding.fromDateFilterButton,
                date -> fromDateFilterAction(filterWeatherReportsByDateAndPopulateHistoryTable, date),
                null, Timing.getTodayDate());
        initDatePickerForButton(viewBinding.toDateFilterButton,
                date -> toDateFilterAction(filterWeatherReportsByDateAndPopulateHistoryTable, date),
                null,
                Timing.getTodayDate());
    }

    /**
     * The action to perform with the data obtained from the {@link DatePickerDialog}
     * associated with the "from date" button for filtering reports.
     *
     * @param toDate                                            The date chosen by the user thanks to the {@link DatePicker} associated
     *                                                          with the "to-date" button.
     * @param filterWeatherReportsByDateAndPopulateHistoryTable The {@link Consumer} that will use the selected {@link Date} for filtering
     *                                                          the weather reports to show.
     */
    private void toDateFilterAction(
            @NonNull Consumer<Predicate<WeatherReport>> filterWeatherReportsByDateAndPopulateHistoryTable,
            @NonNull Date toDate) {
        maxDateFiltered = Timing.getEndOfDay(Objects.requireNonNull(toDate));
        Log.d(TAG, "Selected date: " + maxDateFiltered);
        filterWeatherReportsByDateAndPopulateHistoryTable.accept(getReportFilteredPredicate());

        // Note: need to re-init the "other" date picker to set the max date properly (it cannot be greater than the "to-date")
        initDatePickerForButton(viewBinding.fromDateFilterButton,
                date_ -> fromDateFilterAction(filterWeatherReportsByDateAndPopulateHistoryTable, date_),
                null, maxDateFiltered);
    }

    /**
     * The action to perform with the data obtained from the {@link DatePickerDialog}
     * associated with the "to date" button for filtering reports.
     *
     * @param fromDate                                          The date chosen by the user thanks to the {@link DatePicker} associated
     *                                                          with the "from-date" button.
     * @param filterWeatherReportsByDateAndPopulateHistoryTable The {@link Consumer} that will use the selected {@link Date} for filtering
     *                                                          the weather reports to show.
     */
    private void fromDateFilterAction(
            @NonNull Consumer<Predicate<WeatherReport>> filterWeatherReportsByDateAndPopulateHistoryTable,
            @NonNull Date fromDate) {
        minDateFiltered = Timing.getStartOfDay(Objects.requireNonNull(fromDate));
        Log.d(TAG, "Selected date: " + minDateFiltered);
        filterWeatherReportsByDateAndPopulateHistoryTable.accept(getReportFilteredPredicate());

        // Note: need to re-init the "other" date picker to set the min date properly (it cannot be lower than the "from-date")
        initDatePickerForButton(viewBinding.toDateFilterButton,
                date_ -> toDateFilterAction(filterWeatherReportsByDateAndPopulateHistoryTable, date_),
                minDateFiltered, null);
    }

    /**
     * @return The {@link Predicate} to filter and keep in the report history
     * table only reports for the selected dates.
     */
    @NonNull
    private Predicate<WeatherReport> getReportFilteredPredicate() {
        return weatherReport ->
                (minDateFiltered == null || weatherReport.getMillisecondsSinceEpoch() >= minDateFiltered.getTime())
                        && (maxDateFiltered == null || weatherReport.getMillisecondsSinceEpoch() <= maxDateFiltered.getTime());
    }

    @Override
    public void onResume() {
        super.onResume();
        viewBinding.checkBoxFilterByDates.setChecked(false);     // TODO: temporary solution: if we go to the map and come back to this fragment, it must not happen to have the checkbox checked but not showing filtering buttons.
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
            DBHelper.pull(WeatherReport.class,  // TODO: only reports of this user should be retrieved
                    retrievedWeatherReports -> {
                        weatherReports = new LinkedList<>(Objects.requireNonNull(retrievedWeatherReports));
                        weatherReports.sort((a, b) -> (int) (a.getMillisecondsSinceEpoch() - b.getMillisecondsSinceEpoch()));
                        Log.i(TAG, retrievedWeatherReports.size() + " elements retrieved from the DB");

                        showedWeatherReports = new LinkedList<>(weatherReports);

                        // TODO : update should be periodic, and if filters are set, they must be considered

                        populateReportHistoryTable();
                    },
                    () -> {
                        String errorMsg = getString(R.string.Unable_to_retrieve_entities_from_DB);
                        Log.e(TAG, errorMsg);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show());
                    });
        }).start();

    }

    /**
     * Asynchronously (on a new thread) populate the report history table,
     * with data from {@link #showedWeatherReports}.
     */
    private void populateReportHistoryTable() {

        new Thread(() -> {
            List<TableRow> sortedTableRowList = new ArrayList<>();

            int rowNumber = 0;
            for (WeatherReport wr : Objects.requireNonNull(showedWeatherReports)) {
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
                viewBinding.historyReportsTable.removeAllViews();
                for (TableRow tr : sortedTableRowList) {
                    viewBinding.historyReportsTable.addView(tr);
                }
            });
        }).start();

    }
}