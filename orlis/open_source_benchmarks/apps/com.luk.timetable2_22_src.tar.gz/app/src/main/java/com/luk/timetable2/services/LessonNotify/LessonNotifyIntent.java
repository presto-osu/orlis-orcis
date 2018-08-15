package com.luk.timetable2.services.LessonNotify;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

/**
 * Created by luk on 9/29/15.
 */
public class LessonNotifyIntent extends IntentService {
    public LessonNotifyIntent() {
        super("WakefulService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Integer vibrationLength =
                Integer.parseInt(sharedPref.getString("notifications_vibrate_length", "250"));

        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(vibrationLength);

        LessonNotifyWakeReceiver.completeWakefulIntent(intent);
    }
}