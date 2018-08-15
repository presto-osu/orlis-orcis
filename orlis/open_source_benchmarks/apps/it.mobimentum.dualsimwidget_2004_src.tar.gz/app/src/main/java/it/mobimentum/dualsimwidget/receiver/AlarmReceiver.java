package it.mobimentum.dualsimwidget.receiver;

import static it.mobimentum.dualsimwidget.SettingsActivity.*;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import it.mobimentum.dualsimwidget.DualSimPhone;
import it.mobimentum.dualsimwidget.R;

public class AlarmReceiver extends BroadcastReceiver {
	
	private static final String TAG = AlarmReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive()");
		
		// Weekends?
		int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		boolean excludeWeekends = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getString(R.string.pref_key_exclude_weekends), true);
		if (!excludeWeekends || (weekDay != Calendar.SUNDAY && weekDay != Calendar.SATURDAY)) {
			// Crea notifica
			Notification.Builder builder = new Notification.Builder(context)
					.setSmallIcon(R.drawable.ic_dual_sim_notif)
					.setContentTitle(context.getString(R.string.app_name))
					.setContentText(context.getString(R.string.notification))
					.setAutoCancel(true);

			// Pending intent
			Intent resultIntent = DualSimPhone.getDualSimSettingsIntent()
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent resultPendingIntent = PendingIntent.getActivity(
					context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(resultPendingIntent);

			// GO!
			NotificationManager notifMgr = (NotificationManager)
					context.getSystemService(Context.NOTIFICATION_SERVICE);
			notifMgr.notify(1, builder.build());
		}

		// Reschedule next alarm for doze mode
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			rescheduleNotifications(context);
		}
	}

	public static void rescheduleNotifications(Context context) {
		Log.i(TAG, "rescheduleNotifications()");

		// Cancel current alarms
		setAlarm(context, 1, 0, 0, true);
		setAlarm(context, 2, 0, 0, true);

		// Schedule new alarms
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean enabled = prefs.getBoolean(context.getString(R.string.pref_key_enable_notif), true);
		if (enabled) {
			String[] time1 = prefs.getString(context.getString(R.string.pref_key_alarm_start),
					context.getString(R.string.pref_alarm_start_default)).split(":");
			setAlarm(context, 1, Integer.parseInt(time1[0]), Integer.parseInt(time1[1]), false);

			String[] time2 = prefs.getString(context.getString(R.string.pref_key_alarm_end),
					context.getString(R.string.pref_alarm_end_default)).split(":");
			setAlarm(context, 2, Integer.parseInt(time2[0]), Integer.parseInt(time2[1]), false);
		}
	}

	private static void setAlarm(Context context, int alarmNum, int hours, int mins, boolean cancel) {
		AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context,
				alarmNum/*no cache*/, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if (cancel) {
			Log.i(TAG, "Alarm cancelled: "+alarmNum);
			alarmMgr.cancel(alarmIntent);

			return;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, mins);
		calendar.set(Calendar.SECOND, 0);

		// XXX DEBUG
//		calendar = Calendar.getInstance();
//		calendar.add(Calendar.SECOND, 5);

		long diff = (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000;

		Log.i(TAG, "Alarm scheduled at "+calendar.getTime()+" in "+diff+"''");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Doze mode: WARNING: this will NOT REPEAT the alarm!
			// cfr. https://developer.android.com/training/monitoring-device-state/doze-standby.html
			long nextAlarm = calendar.getTimeInMillis();
			if (nextAlarm < System.currentTimeMillis()) nextAlarm += AlarmManager.INTERVAL_DAY;
			alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarm, alarmIntent);
		}
		else {
			// Pre-marshmallow
			alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
		}
	}
}
