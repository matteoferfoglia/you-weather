package it.units.youweather.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;

import it.units.youweather.R;
import it.units.youweather.auth.Authentication;
import it.units.youweather.databinding.ActivityLoginBinding;
import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.utils.SharedPreferences;
import it.units.youweather.utils.functionals.Consumer;

public class LoginActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> signInIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // view binding
        ActivityLoginBinding viewBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        if (Authentication.isUserSignedIn()) {
            startMainActivity();
        } else {

            // Create sign-in intent
            Consumer<LoggedInUser> onSuccessfulAuthentication = loggedInUser -> {
                SharedPreferences.setValue(
                        getApplicationContext(),
                        SharedPreferences.SharedPreferenceName.LOGGED_IN_USER,
                        loggedInUser);
                startMainActivity();
            };
            Runnable onFailureAuthentication = () ->
                    Toast.makeText(getApplicationContext(), R.string.signInFailed, Toast.LENGTH_SHORT)
                            .show();
            signInIntent = Authentication.getSignInResultLauncher(
                    this, onSuccessfulAuthentication, onFailureAuthentication);

            viewBinding.authButton.setOnClickListener(_view -> startSignIn());
        }

    }

    /**
     * Starts the Firebase Sign-in process.
     * This method build an {@link Intent} and demands the task to {@link AuthUI}.
     */
    private void startSignIn() {
        signInIntent.launch(Authentication.createAndGetSignInIntent());
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);

        // Clear back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();   // ends the current activity if it has its own context
    }

}