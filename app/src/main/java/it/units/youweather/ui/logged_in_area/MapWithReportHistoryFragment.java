package it.units.youweather.ui.logged_in_area;

import android.Manifest;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import it.units.youweather.R;
import it.units.youweather.databinding.FragmentMapWithReportHistoryBinding;
import it.units.youweather.entities.forecast_fields.Coordinates;
import it.units.youweather.entities.storage.WeatherReport;
import it.units.youweather.ui.MainActivity;
import it.units.youweather.utils.PermissionsHelper;
import it.units.youweather.utils.Timing;

/**
 * Fragment containing the map on which the history of the user's reports
 * is shown.
 *
 * @author Matteo Ferfoglia
 */
public class MapWithReportHistoryFragment extends Fragment {

    /**
     * TAG for the logger.
     */
    private static final String TAG = MapWithReportHistoryFragment.class.getSimpleName();

    /**
     * The key for the {@link Bundle} used to pass to this fragment the weather reports to
     * show on the map (in this fragment).
     */
    public static final String WEATHER_REPORTS_TO_SHOW_ON_MAP_BUNDLE_KEY = "weatherReportsToShowOnMap";

    /**
     * The key for the {@link Bundle} used to pass to this fragment the minimum date (included)
     * for showed reports (e.g., if a kind of filtering was used).
     */
    public static final String WEATHER_REPORTS_TO_SHOW_ON_MAP_MIN_DATE_BUNDLE_KEY = "weatherReportsToShowOnMap_minDate";

    /**
     * The key for the {@link Bundle} used to pass to this fragment the maximum date (included)
     * for showed reports (e.g., if a kind of filtering was used).
     */
    public static final String WEATHER_REPORTS_TO_SHOW_ON_MAP_MAX_DATE_BUNDLE_KEY = "weatherReportsToShowOnMap_maxDate";

    /**
     * The key of the request of this fragment when this fragment want to get the
     * weather reports to show on the map.
     */
    public static final String WEATHER_REPORTS_TO_SHOW_ON_MAP_REQUEST_KEY = "weatherReportsToShowOnMap_requestKey";

    /**
     * The {@link MapView} for the map.
     */
    private MapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentMapWithReportHistoryBinding viewBinding
                = FragmentMapWithReportHistoryBinding.inflate(getLayoutInflater());

        hideBottomMenu();

        mapSetUp(viewBinding);

        // Get data to show on the map (listener)
        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(WEATHER_REPORTS_TO_SHOW_ON_MAP_REQUEST_KEY, this,
                        (requestKey, bundle) -> {


                            // Retrieve data for parent fragment

                            LinkedList<WeatherReport> weatherReports = new LinkedList<>();
                            Date minDateForReportsToShow = Timing.getTodayDate();
                            Date maxDateForReportsToShow = Timing.getTodayDate();

                            Serializable weatherReportsObj = bundle.getSerializable(WEATHER_REPORTS_TO_SHOW_ON_MAP_BUNDLE_KEY);
                            if (weatherReportsObj instanceof List
                                    && ((List<?>) weatherReportsObj).size() > 0
                                    && ((List<?>) weatherReportsObj).get(0) instanceof WeatherReport) {

                                // Conversion checked in the previous if
                                //noinspection unchecked
                                weatherReports.addAll((List<WeatherReport>) weatherReportsObj);

                                Log.d(TAG, "Received reports for key "
                                        + WEATHER_REPORTS_TO_SHOW_ON_MAP_BUNDLE_KEY
                                        + ": " + weatherReports);
                            } else {
                                Log.d(TAG, "No data received from parent fragment (it may be an empty list or an error)" +
                                        " for key " + WEATHER_REPORTS_TO_SHOW_ON_MAP_BUNDLE_KEY);
                            }

                            Serializable minDateObj = bundle.getSerializable(WEATHER_REPORTS_TO_SHOW_ON_MAP_MIN_DATE_BUNDLE_KEY);
                            if (minDateObj != null) {
                                if (minDateObj instanceof Date) {
                                    minDateForReportsToShow = (Date) minDateObj;
                                    Log.d(TAG, "Received for key "
                                            + WEATHER_REPORTS_TO_SHOW_ON_MAP_MIN_DATE_BUNDLE_KEY
                                            + ": " + minDateForReportsToShow);
                                } else {
                                    Log.e(TAG, "Error retrieving data for key " + WEATHER_REPORTS_TO_SHOW_ON_MAP_MIN_DATE_BUNDLE_KEY);
                                }
                            } else {
                                minDateForReportsToShow = null;
                            }

                            Serializable maxDateObj = bundle.getSerializable(WEATHER_REPORTS_TO_SHOW_ON_MAP_MAX_DATE_BUNDLE_KEY);
                            if (maxDateObj != null) {
                                if (maxDateObj instanceof Date) {
                                    maxDateForReportsToShow = (Date) maxDateObj;
                                    Log.d(TAG, "Received for key "
                                            + WEATHER_REPORTS_TO_SHOW_ON_MAP_MAX_DATE_BUNDLE_KEY
                                            + ": " + maxDateForReportsToShow);
                                } else {
                                    Log.e(TAG, "Error retrieving data for key " + WEATHER_REPORTS_TO_SHOW_ON_MAP_MAX_DATE_BUNDLE_KEY);
                                }
                            } else {
                                maxDateForReportsToShow = null;
                            }

                            if (minDateForReportsToShow != null || maxDateForReportsToShow != null) {
                                // Show date interval for which reports are shown
                                viewBinding.filteredDatesTextView.setText(getString(
                                        R.string.filtered_reports_from_to,
                                        minDateForReportsToShow == null ? getString(R.string.ever) : Timing.getShortFormattedDate(minDateForReportsToShow),
                                        maxDateForReportsToShow == null ? Timing.getShortFormattedDate(Timing.getTodayDate()) : Timing.getShortFormattedDate(maxDateForReportsToShow)));
                            }


                            // Add markers on the map

                            List<Overlay> mapOverlays = mapView.getOverlays();
                            CustomMarkerOverlay overlays = new CustomMarkerOverlay(
                                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_push_pin_48),
                                    mapView);

                            for (WeatherReport wr : weatherReports) {
                                Coordinates coordinates = wr.getCoordinates();
                                GeoPoint geoPoint = new GeoPoint(coordinates.getLat(), coordinates.getLon());
                                OverlayItem overlayItem = new OverlayItem(wr.toString(), wr.toString(), geoPoint);
                                overlays.addOverlayItem(overlayItem);
                            }

                            mapOverlays.add(overlays);

                        });

        return viewBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomMenu();
    }

    private void hideBottomMenu() {
        setBottomMenuVisibility(View.GONE);
    }

    private void setBottomMenuVisibility(int visibility) {
        BottomNavigationView bottomMenu = ((MainActivity) requireActivity()).getBottomNavigationMenuView();
        bottomMenu.setVisibility(visibility);
    }

    private void showBottomMenu() {
        setBottomMenuVisibility(View.VISIBLE);
    }

    /**
     * Setup for the Map.
     *
     * @param viewBinding The view-binding with the view.
     */
    private void mapSetUp(FragmentMapWithReportHistoryBinding viewBinding) {

        //load/initialize the osmdroid configuration
        Context ctx = requireContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mapView = viewBinding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        // enable rotation and multitouch gestures
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true); // add also the ability to zoom with 2 fingers
        mapView.getOverlays().add(mRotationGestureOverlay);

        // request permission if necessary
        PermissionsHelper.requestPermissionsForActivityIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE // required to show the map
        }, requireActivity());

        // Map controller
        IMapController mapController = mapView.getController();
        mapController.setZoom(9.5);

        // Add the My Location overlay, used to center the map on user's location
        MyLocationNewOverlay myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();   // center the map on user's location
        mapView.getOverlays().add(myLocationOverlay);

        // Add Map Scale bar overlay
        final DisplayMetrics dm = requireContext().getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(mScaleBarOverlay);
    }

    /**
     * Class to add markers on the map.
     * From <a href="https://stackoverflow.com/a/13721842/17402378">here</a>.
     */
    private static class CustomMarkerOverlay extends ItemizedOverlay<OverlayItem> {

        private final List<OverlayItem> overlayItems = new ArrayList<>();
        private final MapView mapView;

        public CustomMarkerOverlay(Drawable drawable, MapView mapView) {
            super(drawable);
            this.mapView = mapView;
        }

        public void addOverlayItem(OverlayItem item) {
            overlayItems.add(item);
            populate();
        }

        @Override
        protected OverlayItem createItem(int index) {
            return overlayItems.get(index);
        }

        @Override
        public int size() {
            return overlayItems.size();
        }

        @Override
        protected boolean onTap(int index) {
            return true;
        }

        @Override
        public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
            return false;
        }
    }
}