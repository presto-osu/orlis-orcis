package com.luk.timetable2.services.LessonNotify;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by luk on 9/29/15.
 */
public class LessonNotifyWakeReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        startWakefulService(context, new Intent(context, LessonNotifyIntent.class));

        context.stopService(new Intent(context, LessonNotifyService.class));
        context.startService(new Intent(context, LessonNotifyService.class));
    }
}