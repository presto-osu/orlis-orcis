package fr.tvbarthel.apps.simpleweatherforcast.utils;


import org.apache.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLUtils {

    /**
     * Get the body of a http response as a String.
     *
     * @param urlString the url to get.
     * @return the inputStream pointed by the url as a String.
     * @throws IOException
     */
    public static String getAsString(String urlString) throws IOException {
        final URL url = new URL(urlString);
        String resultContent = null;

        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setUseCaches(false);

        if (urlConnection.getResponseCode() == HttpStatus.SC_OK) {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            final StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line += "\n";
                stringBuilder.append(line);
            }
            resultContent = stringBuilder.toString();
        }

        return resultContent;
    }

}
