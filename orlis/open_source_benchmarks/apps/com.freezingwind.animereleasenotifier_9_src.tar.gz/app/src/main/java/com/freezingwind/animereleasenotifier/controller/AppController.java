package com.freezingwind.animereleasenotifier.controller;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.freezingwind.animereleasenotifier.helpers.AlarmHelper;
import com.freezingwind.animereleasenotifier.helpers.NetworkManager;
import com.freezingwind.animereleasenotifier.receiver.AlarmReceiver;

import java.util.HashMap;
import java.util.Map;

public class AppController extends Application {
	public static Map<String, Bitmap> imageCache = new HashMap<String, Bitmap>();

	@Override
	public void onCreate() {
		super.onCreate();

		init();
	}

	private void init() {
		NetworkManager.init(this);
	}

	// Schedule alarm
	public static void scheduleAlarm(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		int updateInterval = Integer.parseInt(sharedPrefs.getString("updateInterval", "60"));

		AlarmHelper alarmHelper = new AlarmHelper(context, 1000 * 60 * updateInterval, 1000) {
			@Override
			protected PendingIntent pendingIntent(Context context, int flags) {
				Intent intent = new Intent(context, AlarmReceiver.class);
				return PendingIntent.getBroadcast(context, 0, intent, flags);
			}
		};

		alarmHelper.scheduleUnconditionally();
	}
}
