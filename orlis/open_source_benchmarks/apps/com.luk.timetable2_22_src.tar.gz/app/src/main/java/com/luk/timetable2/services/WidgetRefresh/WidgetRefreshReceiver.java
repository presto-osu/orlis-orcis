package com.luk.timetable2.services.WidgetRefresh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.luk.timetable2.services.DateChange.DateChangeReceiver;

/**
 * Created by luk on 9/29/15.
 */
public class WidgetRefreshReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(context, DateChangeReceiver.class));
    }
}