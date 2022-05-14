package it.units.youweather.ui.logged_in_area;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.units.youweather.R;
import it.units.youweather.auth.Authentication;
import it.units.youweather.databinding.ActivityMainBinding;
import it.units.youweather.databinding.FragmentHomeBinding;
import it.units.youweather.ui.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    /**
     * TAG for logger.
     */
    private final static String TAG = HomeFragment.class.getSimpleName();

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
        FragmentHomeBinding viewBinding = FragmentHomeBinding.inflate(getLayoutInflater());
        viewBinding.authButton.setOnClickListener(_view -> signOut());
        return viewBinding.getRoot();
    }
}