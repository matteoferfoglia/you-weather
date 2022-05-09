package it.units.youweather.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.Auth;

import it.units.youweather.R;
import it.units.youweather.auth.Authentication;
import it.units.youweather.databinding.ActivityMainBinding;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.ui.logged_in_area.MainRegisteredAreaFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        ActivityMainBinding viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        viewBinding.authButton.setOnClickListener(_view -> signOut());

    }

    private void signOut() {
        Authentication.signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }
}