package com.example.tobiastrumm.freifunkautoconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BootCompletedReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            // Check if NotificationService should run.
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if(sharedPref.getBoolean("pref_notification", false)){
                Intent startServiceIntent = new Intent(context, NotificationService.class);
                startWakefulService(context, startServiceIntent);
            }
        }
    }
}
