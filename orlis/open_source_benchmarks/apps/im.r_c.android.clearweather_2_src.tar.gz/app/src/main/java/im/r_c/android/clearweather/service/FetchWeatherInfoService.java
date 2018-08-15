package im.r_c.android.clearweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import im.r_c.android.clearweather.db.WeatherInfoDAO;
import im.r_c.android.clearweather.model.Consts;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.model.WeatherInfo;
import im.r_c.android.clearweather.receiver.AutoUpdateReceiver;
import im.r_c.android.clearweather.util.L;
import im.r_c.android.clearweather.util.SharedPrefsHelper;
import im.r_c.android.clearweather.util.WeatherInfoFetcher;

/**
 * ClearWeather
 * Created by richard on 16/5/4.
 */
public class FetchWeatherInfoService extends Service {
    private static final String TAG = "FetchWeatherInfoService";

    public static void start(Context context) {
        Intent starter = new Intent(context, FetchWeatherInfoService.class);
        context.startService(starter);
    }

    public static void start(Context context, County county) {
        Intent starter = new Intent(context, FetchWeatherInfoService.class);
        starter.putExtra(Consts.EXTRA_COUNTY, county);
        context.startService(starter);
    }

    public static void startAutoUpdate(Context context) {
        Intent starter = new Intent(context, FetchWeatherInfoService.class);
        starter.putExtra(Consts.EXTRA_AUTO_UPDATE, true);
        context.startService(starter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(null, flags, startId);
        }

        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);

        List<County> countyList;
        County countyExtra = (County) intent.getSerializableExtra(Consts.EXTRA_COUNTY);
        if (countyExtra == null) {
            countyList = prefsHelper.getCounties();
        } else {
            countyList = new ArrayList<>();
            countyList.add(countyExtra);
        }

        for (final County county : countyList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WeatherInfoDAO dao = new WeatherInfoDAO(FetchWeatherInfoService.this);
                    WeatherInfo info = WeatherInfoFetcher.fetch(county);
                    if (info != null) {
                        dao.save(info);
                    }
                    dao.close();
                    L.v(TAG, "Fetched weather info of " + county.getName());
                }
            }).start();
        }

        if (intent.getBooleanExtra(Consts.EXTRA_AUTO_UPDATE, false)) {
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            long triggerAtTime = SystemClock.elapsedRealtime() + 4 * 60 * 60 * 1000;
            Intent i = new Intent(this, AutoUpdateReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
