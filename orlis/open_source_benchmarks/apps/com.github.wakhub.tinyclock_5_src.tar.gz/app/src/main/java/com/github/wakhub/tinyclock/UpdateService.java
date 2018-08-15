package com.github.wakhub.tinyclock;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class UpdateService extends Service {

    private static final String TAG = UpdateService.class.getSimpleName();

    private static final int REQUEST_CODE_UPDATE = 100;

    public static final String ACTION_UPDATE = "com.github.wakhub.tinyclock.ACTION_UPDATE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(null, flags, startId);
        }

        String action = intent.getAction();
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        Log.d(TAG, String.format("onStartCommand: %s", action));

        if (action.equals(ACTION_UPDATE)) {
            int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            for (int widgetId : allWidgetIds) {
                updateWidget(widgetId);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWidget(int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget);

        remoteViews.setOnClickPendingIntent(
                R.id.root,
                PendingIntent.getActivity(
                        this,
                        REQUEST_CODE_UPDATE,
                        new Intent(this, SettingsActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));


        Resources res = getResources();
        Settings settings = new Settings(this);
        Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);

        int minSize = res.getDimensionPixelSize(R.dimen.widget_size_min);

        float minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, minSize);
        float maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minSize);
        float density = res.getDisplayMetrics().density;
        int width = Math.round(minWidth * density);
        int height = Math.round(maxHeight * density);
        remoteViews.setImageViewBitmap(R.id.imageView, settings.getBackgroundBitmap(width, height));

        int textColor = settings.getTextColor();
        remoteViews.setTextColor(R.id.dateText, textColor);
        remoteViews.setTextColor(R.id.timeText, textColor);
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}


