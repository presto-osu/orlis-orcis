package com.luk.timetable2.services.LessonNotify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.luk.timetable2.Utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by luk on 9/29/15.
 */
public class LessonNotifyService extends Service {
    private static String TAG = "LessonNotifyService";
    private static AlarmManager sAlarmManager;
    private static PendingIntent sPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        long nearestDate = getNearestDate();

        if (!sharedPref.getBoolean("notifications_vibrate", false) || nearestDate == -1) return;

        Intent intent = new Intent(getApplicationContext(), LessonNotifyReceiver.class);

        sAlarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        sPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        Log.v(TAG, "nD: " + nearestDate);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sAlarmManager.setExact(AlarmManager.RTC_WAKEUP, nearestDate, sPendingIntent);
            return;
        }

        sAlarmManager.set(AlarmManager.RTC_WAKEUP, nearestDate, sPendingIntent);
    }

    @Override
    public void onDestroy() {
        if (sAlarmManager != null) {
            sAlarmManager.cancel(sPendingIntent);
        }
    }

    private long getNearestDate() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int vibrationTime =
                Integer.parseInt(sharedPref.getString("notifications_vibrate_time", "0"));

        long currentTime = Calendar.getInstance().getTimeInMillis();
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        HashMap<Integer, ArrayList<List<String>>> hours = new HashMap<>();
        ArrayList<Long> timestamps = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            ArrayList<List<String>> _hours = Utils.getHours(getApplicationContext(), i);

            if (_hours != null) {
                hours.put(i, _hours);
            }
        }

        for (int day = 0; day < hours.size(); day++) {
            for (int hour = 0; hour < hours.get(day).size(); hour++) {
                String[] time = hours.get(day).get(hour).get(0).split("-");
                SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm", Locale.getDefault());

                try {
                    Timestamp timestamp = new Timestamp(
                            dateFormat.parse(time[0]).getTime() - (vibrationTime * 60000));
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_WEEK, day + 2);
                    calendar.set(Calendar.HOUR_OF_DAY, timestamp.getHours());
                    calendar.set(Calendar.MINUTE, timestamp.getMinutes());
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    if (day < currentDay) {
                        calendar.add(Calendar.DATE, 7);
                    }

                    timestamps.add(calendar.getTimeInMillis());
                } catch (ParseException e) {
                    Log.e(TAG, "", e);
                }
            }
        }

        Collections.sort(timestamps);

        for (Long timestamp : timestamps) {
            if (timestamp > currentTime) {
                return timestamp;
            }
        }

        return -1;
    }
}
