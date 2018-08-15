package com.freezingwind.animereleasenotifier.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.freezingwind.animereleasenotifier.controller.AppController;
import com.freezingwind.animereleasenotifier.helpers.AlarmHelper;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			AppController.scheduleAlarm(context);
		}
	}
}
