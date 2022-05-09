package it.units.youweather.entities;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * Class describing a logged-in user.
 *
 * @author Matteo Ferfoglia
 */
public class LoggedInUser {

    /**
     * The identifier for the user.
     */
    private final String userId;

    /**
     * The name to be displayed for the user.
     */
    private final String displayName;

    /**
     * The {@link android.net.Uri} to the user's profile photo, or null if unavailable.
     */
    private final Uri profilePhotoUri;

    /**
     * Constructor.
     *
     * @param userId          The identifier for the user.
     * @param displayName     The name to be displayed for the user.
     * @param profilePhotoUri The {@link android.net.Uri} to the user's profile photo, or null if unavailable.
     */
    public LoggedInUser(
            @NonNull String userId, @NonNull String displayName, @Nullable Uri profilePhotoUri) {
        this.userId = Objects.requireNonNull(userId);
        this.displayName = Objects.requireNonNull(displayName);
        this.profilePhotoUri = profilePhotoUri;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    @NonNull
    @Override
    public String toString() {
        return "LoggedInUser{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", profilePhotoUri=" + profilePhotoUri +
                '}';
    }
}
