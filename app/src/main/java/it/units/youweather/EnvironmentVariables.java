package it.units.youweather;

/**
 * Class containing "environment variables" for the application.
 *
 * @author Matteo Ferfoglia
 */
public class EnvironmentVariables {

    /**
     * Flag: if true, Firebase emulators will be used (for development).
     */
    public static final boolean USE_FIREBASE_EMULATORS = true;

    /**
     * Android emulator IP (when {@link #USE_FIREBASE_EMULATORS} is true).
     */
    public static final String FIREBASE_EMULATOR_HOST = "10.0.2.2";

    /**
     * TCP port for the Firebase Authentication service emulator.
     */
    public static final int FIREBASE_EMULATOR_AUTH_PORT = 9099;

    /**
     * TCP port for the Firebase Realtime Database service emulator.
     */
    public static final int FIREBASE_EMULATOR_REALTIME_DB_PORT = 9000;
}
