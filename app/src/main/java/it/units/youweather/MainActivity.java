package it.units.youweather;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

import it.units.youweather.auth.Authentication;
import it.units.youweather.databinding.ActivityMainBinding;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.ui.LoginFragment;
import it.units.youweather.ui.logged_in_area.HomeFragment;
import it.units.youweather.ui.logged_in_area.MainRegisteredAreaFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Set the first fragment to show (login page if unauthenticated user, home page otherwise)
        LoggedInUser loggedInUser =
                Authentication.getCurrentlyLoggedInUserOrNull(getApplicationContext());
        Fragment fragment = loggedInUser == null ? /*true if unauthenticated*/
                new LoginFragment() : new MainRegisteredAreaFragment();
        getSupportFragmentManager().beginTransaction()  // todo: https://developer.android.com/guide/navigation/navigation-conditional and https://developer.android.com/guide/navigation/navigation-nested-graphs
                .add(R.id.nav_host_fragment_container_activity_main, fragment)
                .commitNow();
    }
}