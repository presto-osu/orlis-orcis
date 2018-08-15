package net.iexos.musicalarm;

import android.app.AlarmManager;
import android.content.Context;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import java.util.Calendar;
import java.text.DateFormat;

public final class AlarmUtils {
    private static final String LOGGING_TAG = "AlarmUtils";

    // this class is never instantiated
    private AlarmUtils() {
    }

    private static AlarmManager alarmManager(Context con) {
        return (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
    }

    public static void dismissAlarm(Context con) {
        Log.v(LOGGING_TAG, "Dismissing alarm");
        alarmManager(con).cancel(getAlarmIntent(con));
        alarmManager(con).cancel(getRingIntent(con));
        con.stopService(new Intent(con, AlarmService.class));
        updateAlarmInfo(con, false, 0);
    }

    public static void dismissRingAlarm(Context con) {
        alarmManager(con).cancel(getRingIntent(con));
        updateAlarmInfo(con, false, 0);
    }

    public static AlarmClockInfo setAlarm(Context con, int hour, int min) {
        Calendar now = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();
        alarm.set(Calendar.HOUR_OF_DAY, hour);
        alarm.set(Calendar.MINUTE, min);
        alarm.set(Calendar.SECOND, 0);
        if (alarm.before(now)) alarm.add(Calendar.HOUR_OF_DAY, 24);  // alarm should ring in future
        String alarmDateText = DateFormat.getDateTimeInstance().format(alarm.getTime());
        Log.v(LOGGING_TAG, "Alarm time set to " + alarmDateText);

        long triggerTime = alarm.getTimeInMillis();
        AlarmClockInfo acInfo = setAlarm(con, triggerTime);

        Toast.makeText(con, con.getString(R.string.toast_alarm_set), Toast.LENGTH_LONG).show();
        return acInfo;
    }

    public static AlarmClockInfo setAlarm(Context con, long triggerTime) {
        if (triggerTime < System.currentTimeMillis()) return null;
        AlarmClockInfo acInfo = new AlarmClockInfo(triggerTime, getShowIntent(con));
        alarmManager(con).setAlarmClock(acInfo, getAlarmIntent(con));
        updateAlarmInfo(con, true, triggerTime);
        return acInfo;
    }

    public static AlarmClockInfo setRingAlarm(Context con, int relMin) {
        long triggerTime = System.currentTimeMillis() + 60000*relMin;
        AlarmClockInfo acInfo = new AlarmClockInfo(triggerTime, getShowIntent(con));
        alarmManager(con).setAlarmClock(acInfo, getRingIntent(con));
        updateAlarmInfo(con, true, triggerTime);
        return acInfo;
    }

    public static void updateAlarmInfo(Context con, boolean alarmSet, long triggerTime) {
        SharedPreferences settings = con.getSharedPreferences(AlarmViewActivity.PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(AlarmViewActivity.PREF_ALARM_SET, alarmSet);
        editor.putLong(AlarmViewActivity.PREF_TRIGGER_TIME, triggerTime);
        editor.apply();
    }

    private static PendingIntent getRingIntent(Context con) {
        return AlarmService.getPendingStateChangeIntent(con, AlarmService.StateChange.START_RINGING);
    }

    private static PendingIntent getAlarmIntent(Context con) {
        return AlarmService.getPendingStateChangeIntent(con, AlarmService.StateChange.START_PLAYBACK);
    }

    public static PendingIntent getShowIntent(Context con) {
        Intent showIntent = new Intent(con, AlarmViewActivity.class);
        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(con, 0, showIntent, 0);
    }

    public static long getTriggerTime (Context con) {
        SharedPreferences settings = con.getSharedPreferences(AlarmViewActivity.PREFERENCES, 0);
        return settings.getLong(AlarmViewActivity.PREF_TRIGGER_TIME, 0);
    }

    public static boolean isAlarmSet(Context con) {
        SharedPreferences settings = con.getSharedPreferences(AlarmViewActivity.PREFERENCES, 0);
        return settings.getBoolean(AlarmViewActivity.PREF_ALARM_SET, false);
    }
}
