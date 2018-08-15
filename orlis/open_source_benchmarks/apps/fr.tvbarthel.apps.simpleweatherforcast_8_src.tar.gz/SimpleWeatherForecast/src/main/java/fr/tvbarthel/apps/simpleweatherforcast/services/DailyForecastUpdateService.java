package fr.tvbarthel.apps.simpleweatherforcast.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Locale;

import fr.tvbarthel.apps.simpleweatherforcast.R;
import fr.tvbarthel.apps.simpleweatherforcast.receivers.WeatherWidgetReceiver;
import fr.tvbarthel.apps.simpleweatherforcast.utils.ConnectivityUtils;
import fr.tvbarthel.apps.simpleweatherforcast.utils.LocationUtils;
import fr.tvbarthel.apps.simpleweatherforcast.utils.SharedPreferenceUtils;
import fr.tvbarthel.apps.simpleweatherforcast.utils.URLUtils;

/**
 * A Simple  {@link android.app.Service} used to update the daily forecast.
 */
public class DailyForecastUpdateService extends Service implements LocationListener {

    // update action
    public static final String ACTION_UPDATE = "DailyForecastUpdateService.Actions.Update";
    // update error action
    public static final String ACTION_UPDATE_ERROR = "DailyForecastUpdateService.Actions.UpdateError";
    // update error extra
    public static final String EXTRA_UPDATE_ERROR = "DailyForecastUpdateService.Extra.UpdateError";

    private LocationManager mLocationManager;
    private Looper mServiceLooper;
    private Handler mServiceHandler;
    private Boolean mIsUpdatingTemperature;

    public static void startForUpdate(Context context) {
        final Intent intent = new Intent(context, DailyForecastUpdateService.class);
        intent.setAction(ACTION_UPDATE);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        HandlerThread thread = new HandlerThread("DailyForecastUpdateService", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new Handler(mServiceLooper);
        mIsUpdatingTemperature = false;
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't support binding.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_UPDATE.equals(intent.getAction()) && !mIsUpdatingTemperature) {
            if (ConnectivityUtils.isConnected(getApplicationContext())) {
                mIsUpdatingTemperature = true;
                // First get a new location
                getNewLocation();
            } else {
                broadcastErrorAndStop(R.string.toast_no_connection_available);
            }
        }
        return START_STICKY;
    }

    private void updateForecast(final Location newLocation) {
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final String JSON = getForecastAsJSON(newLocation);
                    SharedPreferenceUtils.storeWeather(DailyForecastUpdateService.this, JSON);

                    // Broadcast change to the widget provider
                    Intent intent = new Intent(DailyForecastUpdateService.this, WeatherWidgetReceiver.class);
                    intent.setAction(WeatherWidgetReceiver.APPWIDGET_DATA_CHANGED);
                    sendBroadcast(intent);
                    mIsUpdatingTemperature = false;
                    DailyForecastUpdateService.this.stopSelf();
                } catch (SocketTimeoutException e) {
                    broadcastErrorAndStop(R.string.error_message_server_not_available);
                } catch (MalformedURLException e) {
                    broadcastErrorAndStop(R.string.error_message_malformed_url);
                } catch (IOException e) {
                    broadcastErrorAndStop(R.string.error_message_io_exception);
                }
            }
        });
    }

    private void getNewLocation() {
        final String provider = LocationUtils.getBestCoarseProvider(this);
        if (provider == null) {
            broadcastErrorAndStop(R.string.toast_not_allowed_to_access_location);
        } else {
            mLocationManager.requestSingleUpdate(provider, this, null);
        }
    }

    private void broadcastErrorAndStop(int resourceId) {
        final String message = getString(resourceId);
        final Intent intent = new Intent(ACTION_UPDATE_ERROR);
        intent.putExtra(EXTRA_UPDATE_ERROR, message);
        sendBroadcast(intent);
        stopSelf();
    }

    private String getForecastAsJSON(Location location) throws IOException {
        final String lang = Locale.getDefault().getLanguage();
        //Forge the url for the open weather map API.
        final String url = getString(R.string.url_open_weather_map_api, lang, location.getLatitude(), location.getLongitude());
        return URLUtils.getAsString(url);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateForecast(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
