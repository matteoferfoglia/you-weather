package it.units.youweather.ui.logged_in_area;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import it.units.youweather.databinding.FragmentMapWithReportHistoryBinding;
import it.units.youweather.ui.MainActivity;
import it.units.youweather.utils.Permissions;

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

        MapView mapView = viewBinding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);

        // enable rotation and multitouch gestures
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true); // add also the ability to zoom with 2 fingers
        mapView.getOverlays().add(mRotationGestureOverlay);

        // request permission if necessary
        Permissions.requestPermissionsForActivityIfNecessary(new String[]{
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

        // Add a compass overlay    // works only on actual hardware (not in emulator) // TODO: check
        CompassOverlay myCompassOverlay = new CompassOverlay(
                requireContext(),
                new InternalCompassOrientationProvider(requireContext()),
                mapView);
        myCompassOverlay.enableCompass();
        mapView.getOverlays().add(myCompassOverlay);

//        // Add the Grid line Overlay - for displaying latitude/longitude grid lines
//        LatLonGridlineOverlay2 overlay = new LatLonGridlineOverlay2();
//        mapView.getOverlays().add(overlay);

        // Add Map Scale bar overlay
        final DisplayMetrics dm = requireContext().getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(mScaleBarOverlay);
    }
}