package it.units.youweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import it.units.youweather.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());


        // nav bar

        NavHostFragment navHostController = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = Objects.requireNonNull(navHostController).getNavController();

        BottomNavigationView bottomNavigationMenuView = viewBinding.bottomNavView;
        NavigationUI.setupWithNavController(bottomNavigationMenuView, navController);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.blankFragment) // these are the ids defined in nav_graph.xml
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // end nav bar
    }
}