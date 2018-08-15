package com.luk.timetable2.services.WidgetRefresh;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Created by luk on 9/29/15.
 */
public class WidgetRefreshService extends Service {
    private static AlarmManager sAlarmManager;
    private static PendingIntent sPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Intent intent = new Intent(getApplicationContext(), WidgetRefreshReceiver.class);

        sAlarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        sPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    sPendingIntent
            );

            return;
        }

        sAlarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                sPendingIntent
        );
    }

    @Override
    public void onDestroy() {
        sAlarmManager.cancel(sPendingIntent);
    }
}
