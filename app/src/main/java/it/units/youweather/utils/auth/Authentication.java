package it.units.youweather.utils.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.Objects;

import it.units.youweather.EnvironmentVariables;
import it.units.youweather.R;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.utils.SharedData;
import it.units.youweather.utils.functionals.Consumer;

/**
 * Class with authentication utility methods.
 *
 * @author Matteo Ferfoglia
 */
public abstract class Authentication {

    /**
     * TAG for logger.
     */
    private static final String TAG = Authentication.class.getSimpleName();

    private static final FirebaseAuth firebaseAuth;

    static {
        firebaseAuth = FirebaseAuth.getInstance();
        if (EnvironmentVariables.USE_FIREBASE_EMULATORS) {
            firebaseAuth.useEmulator(
                    EnvironmentVariables.FIREBASE_EMULATOR_HOST,
                    EnvironmentVariables.FIREBASE_EMULATOR_AUTH_PORT);
        }
    }

    /**
     * Creates and get a sign-in {@link Intent}.
     *
     * @return A sign-in {@link Intent}.
     */
    public static Intent createAndGetSignInIntent() {
        return AuthUI.getInstance(firebaseAuth.getApp())
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(                 // list of available providers here
                        Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build())) // only email provider
                .setLogo(R.mipmap.ic_launcher)
                .build();
    }

    /**
     * Build an {@link ActivityResultLauncher} to launch and then handle the result
     * of the authentication process.
     *
     * @param applicationContext The application context (the activity handling the sign-in).
     * @param onSuccess          The {@link Consumer} to be applied on the {@link LoggedInUser}
     *                           returned from the authentication process in case of success.
     *                           The {@link Consumer} can be null if you do not want to perform
     *                           any activity on the returned {@link LoggedInUser}.
     * @param onFailure          The {@link Runnable} to be executed in case of authentication
     *                           failure, or null if you do not want to execute any action in
     *                           case of failure.
     * @return The {@link ActivityResultLauncher} for the authentication process.
     */
    public static ActivityResultLauncher<Intent> getSignInResultLauncher(
            @NonNull AppCompatActivity applicationContext,
            @Nullable Consumer<LoggedInUser> onSuccess,
            @Nullable Runnable onFailure) {
        return Objects.requireNonNull(applicationContext)
                .registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
                        (FirebaseAuthUIAuthenticationResult firebaseAuthResult) -> {
                            if (!isFirebaseSignInSucceed(firebaseAuthResult)) {
                                if (onFailure != null) {
                                    onFailure.run();
                                }
                            } else { // sign-in successful
                                if (onSuccess != null) {
                                    onSuccess.accept(Objects.requireNonNull(
                                            getCurrentlySignedInUserOrNull(applicationContext),
                                            "User should be logged but is not"));
                                }
                            }
                        });
    }

    /**
     * @param firebaseAuthResult The result of the Firebase authentication process.
     * @return true if the Firebase authentication process succeed, false otherwise.
     */
    private static boolean isFirebaseSignInSucceed(FirebaseAuthUIAuthenticationResult firebaseAuthResult) {
        return firebaseAuthResult.getResultCode() == Activity.RESULT_OK;
    }

    /**
     * Returns the currently logged in user, or null (if the user did not authenticate) and
     * updates the preference
     * {@link SharedData.SharedDataName#LOGGED_IN_USER}
     *
     * @param applicationContext The application context (the activity handling the sign-in).
     * @return the currently logged-in user or null.
     */
    public static LoggedInUser getCurrentlySignedInUserOrNull(@NonNull Context applicationContext) {
        Objects.requireNonNull(applicationContext);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        LoggedInUser loggedInUser;
        if (firebaseUser == null) { // unauthenticated
            SharedData.removeValue(
                    SharedData.SharedDataName.LOGGED_IN_USER);
            loggedInUser = null;
        } else {            // authenticated
            loggedInUser = createLoggedInUserInstanceFromFirebaseUser(firebaseUser);
            SharedData.setValue(
                    SharedData.SharedDataName.LOGGED_IN_USER,
                    loggedInUser);
        }
        return loggedInUser;
    }

    /**
     * Creates an instance of {@link FirebaseUser} starting from an instance
     * of {@link LoggedInUser}.
     *
     * @param firebaseUser The {@link FirebaseUser} instance from which an
     *                     instance of {@link LoggedInUser} is built.
     * @return an instance of {@link LoggedInUser} built from the given
     * {@link FirebaseUser} instance.
     */
    private static LoggedInUser createLoggedInUserInstanceFromFirebaseUser(
            @NonNull FirebaseUser firebaseUser) {
        return new LoggedInUser(
                Objects.requireNonNull(firebaseUser).getUid(),
                Objects.requireNonNull(firebaseUser.getDisplayName()),
                firebaseUser.getPhotoUrl());
    }

    /**
     * @return true if any user is currently logged-in, false otherwise.
     */
    public static boolean isUserSignedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Signs-out the user.
     */
    public static void signOut() {
        firebaseAuth.signOut();
        SharedData.setValue(SharedData.SharedDataName.LOGGED_IN_USER, false);
    }
}
