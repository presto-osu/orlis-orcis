package im.r_c.android.clearweather.util;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * FirstLineCodePractice
 * Created by richard on 16/4/26.
 */
public class HttpUtils {
    public static final int DEFAULT_CONNECTION_TIME_OUT = 10000;
    public static final int DEFAULT_READ_TIME_OUT = 10000;

    public static void get(final String urlString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getSync(urlString);
            }
        }).start();
    }

    public static void get(final String urlString, @NonNull final HttpCallbackListener listener, @NonNull final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = getSync(urlString);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (response != null) {
                            listener.onSuccess(response);
                        } else {
                            listener.onFailure();
                        }
                    }
                });
            }
        }).start();
    }

    public static String getSync(String urlString) {
        String response = null;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(DEFAULT_CONNECTION_TIME_OUT);
            connection.setReadTimeout(DEFAULT_READ_TIME_OUT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line)
                        .append("\n");
            }
            response = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return response;
    }

    public static abstract class HttpCallbackListener {
        public abstract void onSuccess(String response);

        public abstract void onFailure();
    }
}
