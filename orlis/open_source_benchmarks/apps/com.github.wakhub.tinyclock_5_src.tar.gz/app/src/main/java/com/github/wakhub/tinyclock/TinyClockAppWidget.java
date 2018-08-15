package com.github.wakhub.tinyclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TinyClockAppWidget extends AppWidgetProvider {

    private static final String TAG = TinyClockAppWidget.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName componentName = new ComponentName(context, TinyClockAppWidget.class);
        onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        ComponentName componentName = new ComponentName(context, TinyClockAppWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

        Intent intent = new Intent(
                context.getApplicationContext(),
                UpdateService.class);
        intent.setAction(UpdateService.ACTION_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intent);
    }
}


