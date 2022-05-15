package it.units.youweather.utils.http;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class to handle an HTTP response.
 *
 * @author Matteo Ferfoglia
 */
public class HTTPResponse {

    /**
     * The {@link String} with the response received from the server.
     */
    private final String response;

    /**
     * Constructor.
     *
     * @param req The {@link HTTPRequest} for which you want to get the response.
     * @throws IOException in case of errors.
     */
    public HTTPResponse(HTTPRequest req) throws IOException {
        try (
                BufferedReader responseReader = new BufferedReader(
                        new InputStreamReader(req.getConnection().getInputStream()))) {

            // read response
            StringBuilder responseSb = new StringBuilder();
            String line;
            while ((line = responseReader.readLine()) != null) {
                responseSb.append(line).append(System.lineSeparator());
            }
            response = responseSb.toString();
        }
    }

    public String getResponse() {
        return response;
    }
}
