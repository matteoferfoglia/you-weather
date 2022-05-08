package it.units.youweather.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import it.units.youweather.entities.LoggedInUser;
import it.units.youweather.utils.SharedPreferences;

public interface Authentication {

    /**
     * Returns the currently logged in user, or null (if the user did not authenticate) and
     * updates the preference
     * {@link it.units.youweather.utils.SharedPreferences.SharedPreferenceName#LOGGED_IN_USER}
     *
     * @return the currently logged-in user or null.
     */
    static LoggedInUser getCurrentlyLoggedInUserOrNull(@NonNull Context applicationContext) {
        Objects.requireNonNull(applicationContext);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        LoggedInUser loggedInUser;
        if (user == null) { // unauthenticated
            SharedPreferences.removeValue(
                    applicationContext, SharedPreferences.SharedPreferenceName.LOGGED_IN_USER);
            loggedInUser = null;
        } else {            // authenticated
            loggedInUser = new LoggedInUser(user);
            SharedPreferences.setValue(
                    applicationContext,
                    SharedPreferences.SharedPreferenceName.LOGGED_IN_USER,
                    loggedInUser);
        }
        return loggedInUser;
    }
}
