package com.luk.timetable2.services.WidgetRefresh;

import android.app.IntentService;
import android.content.Intent;

import com.luk.timetable2.Utils;
import com.luk.timetable2.services.DateChange.DateChangeReceiver;

/**
 * Created by luk on 9/29/15.
 */
public class WidgetRefreshIntent extends IntentService {
    public WidgetRefreshIntent() {
        super("WakefulService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Utils.refreshWidgets(getApplicationContext());
        DateChangeReceiver.completeWakefulIntent(intent);
    }
}