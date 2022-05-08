package it.units.youweather.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Objects;

import it.units.youweather.EnvironmentVariables;
import it.units.youweather.R;
import it.units.youweather.databinding.FragmentLoginBinding;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.utils.SharedPreferences;


/**
 * Fragment to handle the Firebase authentication process.
 * This code was adapted form <a href="https://github.com/firebase/FirebaseUI-Android/blob/master/auth/">GitHub</a>.
 */
public class LoginFragment extends Fragment {

    // TODO: move the authentication logic to interface Authentication

    /**
     * TAG for logger.
     */
    private final static String FIREBASE_AUTH_TAG = "FirebaseAuth";

    /**
     * Firebase authenticator.
     */
    private FirebaseAuth firebaseAuth = null;

    /**
     * Instance for view-binding.
     */
    private FragmentLoginBinding viewBinding = null;

    /**
     * Build a sign in {@link Intent} using {@link AuthUI#createSignInIntentBuilder()}.
     */
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(), // requires dependency 'com.firebaseui:firebase-ui-auth:8.0.1'
            this::onSignInResult);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentLoginBinding.inflate(getLayoutInflater());
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if (EnvironmentVariables.USE_FIREBASE_EMULATORS) {
            firebaseAuth.useEmulator(
                    EnvironmentVariables.FIREBASE_EMULATOR_HOST,
                    EnvironmentVariables.FIREBASE_EMULATOR_AUTH_PORT);
        }

        // "on-click" buttons handlers
        viewBinding.authButton.setOnClickListener(_view -> startSignIn());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewBinding = null;
    }

    /**
     * Signs out the currently authenticated Firebase user.
     */
    private void signOut() {
        AuthUI.getInstance().signOut(requireContext());
        updateUI();
    }

    /**
     * Starts the Firebase Sign-in process.
     * This method build an {@link Intent} and demands the task to {@link AuthUI}.
     */
    private void startSignIn() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders( // list of available providers here
                        Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build())) // only email provider
                .setLogo(R.mipmap.ic_launcher)
                .build();
        signInLauncher.launch(intent);
    }

    /**
     * Handler for the actions to be performed with the firebaseAuthResult obtained
     * from the Firebase authentication process.
     *
     * @param firebaseAuthResult The result of the Firebase authentication process.
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult firebaseAuthResult) {
        if (!isFirebaseSignInSucceed(firebaseAuthResult)) {
            Toast.makeText(getContext(), R.string.signInFailed, Toast.LENGTH_SHORT).show();
        } else {
            LoggedInUser user =
                    new LoggedInUser(Objects.requireNonNull(firebaseAuth.getCurrentUser()));
            SharedPreferences.setValue(
                    requireContext(),
                    SharedPreferences.SharedPreferenceName.LOGGED_IN_USER,
                    user);
            LoggedInUser user2 = SharedPreferences.getValue(requireContext(), SharedPreferences.SharedPreferenceName.LOGGED_IN_USER);
        }
        updateUI(); // independently from sign in success
    }

    /**
     * @param firebaseAuthResult The result of the Firebase authentication process.
     * @return true if the Firebase authentication process succeed, false otherwise.
     */
    private static boolean isFirebaseSignInSucceed(FirebaseAuthUIAuthenticationResult firebaseAuthResult) {
        return firebaseAuthResult.getResultCode() == Activity.RESULT_OK;
    }

    /**
     * Updates the UI for the signed-in user.
     */
    private void updateUI() {
        // TODO
    }

}