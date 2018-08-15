package com.luk.timetable2.services.LessonNotify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by luk on 9/29/15.
 */
public class LessonNotifyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(context, LessonNotifyWakeReceiver.class));
    }
}