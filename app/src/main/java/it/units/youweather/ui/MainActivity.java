package it.units.youweather.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import it.units.youweather.R;
import it.units.youweather.databinding.ActivityMainBinding;
import it.units.youweather.utils.ActivityStaticResourceHandler;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        ActivityStaticResourceHandler.initialize(this);

        // nav bar

        NavHostFragment navHostController = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container_activity_main);
        NavController navController = Objects.requireNonNull(navHostController).getNavController();

        bottomNavigationMenuView = viewBinding.bottomNavView;
        NavigationUI.setupWithNavController(bottomNavigationMenuView, navController);

        if (getSupportActionBar() != null) {   // if the action bar exists
            // For navigation, do not set action bar
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.homeFragment, R.id.newReportFragment, R.id.userPageWithHistoryFragment) // these are the ids defined in nav_graph.xml
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }

        // end nav bar

    }

    public BottomNavigationView getBottomNavigationMenuView() {
        return Objects.requireNonNull(bottomNavigationMenuView);
    }
}