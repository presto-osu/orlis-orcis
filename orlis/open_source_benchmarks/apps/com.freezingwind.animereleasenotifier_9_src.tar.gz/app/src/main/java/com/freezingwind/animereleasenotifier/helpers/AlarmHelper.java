package com.freezingwind.animereleasenotifier.helpers;

import android.content.Context;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Example: schedule about twice a day, and wait at least 10 minutes before the first alarm
 *
 * AlarmHelper myServiceAlarmHelper = new AlarmHelper(context, AlarmManager.INTERVAL_HALF_DAY, 1000 * 60 * 10) {
 *     @Override
 *     protected PendingIntent pendingIntent(Context context, int flags) {
 *         Intent intent = new Intent(context, MyService.class).putExtra("foo", "bar");
 *         return PendingIntent.getService(context, 0, intent, flags);
 *     }
 * };
 * myServiceAlarmHelper.scheduleIfMissing();
 */
public abstract class AlarmHelper {
	private final Context context;
	private final long inexactAlarmIntervalMs;
	private final long firstAlarmMinDelayMs;

	public AlarmHelper(Context context, long inexactAlarmIntervalMs, long firstAlarmMinDelayMs) {
		this.context = context.getApplicationContext();
		this.inexactAlarmIntervalMs = inexactAlarmIntervalMs;
		this.firstAlarmMinDelayMs = firstAlarmMinDelayMs;
	}

	/**
	 * Schedule the alarm only if it hasn't been scheduled yet. To be used on every app start, e.g. in the onCreate() method
	 * of the Application subclass.
	 *
	 * Even if this is called after the app stopped or crashed, it won't overwrite the previously-scheduled alarm.
	 */
	public void scheduleIfMissing() {
		if(false){//pendingIntent(context, PendingIntent.FLAG_NO_CREATE) != null) {
			// Assume that finding the intent means the alarm has been scheduled.
			Log.d("AlarmHelper", "Found pending intent, not scheduling the alarm");
		} else {
			Log.d("AlarmHelper", "Didn't find the pending intent, scheduling the alarm");
			scheduleUnconditionally();
		}
	}

	/**
	 * Schedule the alarm unconditionally. To be used after boot.
	 */
	public void scheduleUnconditionally() {
		PendingIntent pendingIntent = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);

		// Check approximately every alarm interval, don't wake the device up, wait initially for at least firstAlarmMinDelayMs
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(
			AlarmManager.ELAPSED_REALTIME_WAKEUP,
			SystemClock.elapsedRealtime() + firstAlarmMinDelayMs,
			inexactAlarmIntervalMs,
			pendingIntent
		);
	}

	protected abstract PendingIntent pendingIntent(Context context, int flags);
}