package com.luk.timetable2.services.DateChange;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.luk.timetable2.services.RegisterReceivers;
import com.luk.timetable2.services.WidgetRefresh.WidgetRefreshIntent;

/**
 * Created by luk on 9/29/15.
 */
public class DateChangeReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startWakefulService(context, new Intent(context, WidgetRefreshIntent.class));
        context.sendBroadcast(new Intent(context, RegisterReceivers.class));
    }
}