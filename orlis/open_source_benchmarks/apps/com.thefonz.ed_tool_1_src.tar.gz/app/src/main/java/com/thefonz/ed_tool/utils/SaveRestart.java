package com.thefonz.ed_tool.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.thefonz.ed_tool.MainActivity;

/**
 * Created by thefonz on 05/04/15.
 */
public class SaveRestart extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags()));
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
