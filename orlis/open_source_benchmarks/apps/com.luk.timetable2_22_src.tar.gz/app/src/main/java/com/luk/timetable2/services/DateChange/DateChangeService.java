package com.luk.timetable2.services.DateChange;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * Created by luk on 9/29/15.
 */
public class DateChangeService extends Service {
    private static DateChangeReceiver sDateChangeReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sDateChangeReceiver = new DateChangeReceiver();

        getApplicationContext().registerReceiver(
                sDateChangeReceiver,
                new IntentFilter(Intent.ACTION_TIME_CHANGED)
        );
    }

    @Override
    public void onDestroy() {
        getApplicationContext().unregisterReceiver(sDateChangeReceiver);
        sDateChangeReceiver = null;
    }
}