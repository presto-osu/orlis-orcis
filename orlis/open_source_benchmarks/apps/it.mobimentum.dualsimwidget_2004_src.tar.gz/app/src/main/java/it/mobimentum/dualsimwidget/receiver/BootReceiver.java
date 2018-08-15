package it.mobimentum.dualsimwidget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import static it.mobimentum.dualsimwidget.SettingsActivity.*;
import static it.mobimentum.dualsimwidget.receiver.AlarmReceiver.*;

public class BootReceiver extends BroadcastReceiver {
	
	private static final String TAG = BootReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");

		// Re-schedula alarms
		AlarmReceiver.rescheduleNotifications(context);
	}
}
