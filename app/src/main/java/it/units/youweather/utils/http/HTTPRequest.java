package it.units.youweather.utils.http;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * Class to handle an HTTP request.
 *
 * @author Matteo Ferfoglia
 */
public class HTTPRequest {

    private final HttpURLConnection connection;

    public enum HTTPRequestMethod {GET, POST, PUT, HEAD}

    /**
     * Open a connection to the given URL or throws in case of errors.
     *
     * @param requestMethod The {@link HTTPRequestMethod} to use for the request.
     * @param url           The URL (as {@link String}) to which the connection should be opened.
     * @throws IOException In case of errors.
     */
    public HTTPRequest(@NonNull String url, @NonNull HTTPRequestMethod requestMethod)
            throws IOException {
        // Create connection
        connection = (HttpURLConnection)
                new URL(Objects.requireNonNull(url)).openConnection();
        connection.setRequestProperty(
                "Content-Type", "application/x-www-form-urlencoded");
        connection.setUseCaches(false);
        connection.setRequestMethod(Objects.requireNonNull(requestMethod).name());
    }

    /**
     * Like {@link #HTTPRequest(String, HTTPRequestMethod)}, but this constructor
     * use the GET method as default for {@link HTTPRequestMethod}.
     */
    public HTTPRequest(@NonNull String url) throws IOException {
        this(url, HTTPRequestMethod.GET);
    }

    public HttpURLConnection getConnection() {
        return connection;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection.disconnect();
    }

}