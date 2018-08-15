/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimAddress;
import net.sf.times.location.ZmanimLocationListener;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.hebrewcalendar.JewishDate;
import net.sourceforge.zmanim.util.GeoLocation;

import java.util.Calendar;

/**
 * Shows a list of halachic times (<em>zmanim</em>) for prayers in a widget.
 *
 * @author Moshe Waisberg
 */
public class ZmanimWidget extends AppWidgetProvider implements ZmanimLocationListener {

    private static final String TAG = "ZmanimWidget";

    /** The context. */
    protected Context context;
    /** Provider for locations. */
    private ZmanimLocations locations;
    /** The settings and preferences. */
    private ZmanimSettings settings;

    private final ContentObserver formatChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            notifyAppWidgetViewDataChanged(context);
        }
    };

    /**
     * Constructs a new widget.
     */
    public ZmanimWidget() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        this.context = context;

        final String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Context app = context.getApplicationContext();

            IntentFilter timeChanged = new IntentFilter(Intent.ACTION_TIME_CHANGED);
            app.registerReceiver(this, timeChanged);

            IntentFilter dateChanged = new IntentFilter(Intent.ACTION_DATE_CHANGED);
            app.registerReceiver(this, dateChanged);

            IntentFilter tzChanged = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            app.registerReceiver(this, tzChanged);

            ContentResolver resolver = context.getContentResolver();
            resolver.registerContentObserver(Uri.withAppendedPath(Settings.System.CONTENT_URI, Settings.System.TIME_12_24), true, formatChangeObserver);
        } else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            Context app = context.getApplicationContext();
            ContentResolver resolver = context.getContentResolver();
            try {
                app.unregisterReceiver(this);
                resolver.unregisterContentObserver(formatChangeObserver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "unregister receiver: " + e.getLocalizedMessage(), e);
            }
        } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
            notifyAppWidgetViewDataChanged(context);
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            notifyAppWidgetViewDataChanged(context);
        } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            notifyAppWidgetViewDataChanged(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        this.context = context;
        if (locations == null) {
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
        }
        locations.start(this);

        populateTimes(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Populate the list with times.
     *
     * @param context
     *         the context.
     * @param appWidgetManager
     *         the widget manager.
     * @param appWidgetIds
     *         the widget ids for which an update is needed - {@code null} to
     *         get ids from the manager.
     */
    protected void populateTimes(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final Class<?> clazz = getClass();
        ComponentName provider = new ComponentName(context, clazz);
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
            if (appWidgetIds == null)
                return;
        }
        if (appWidgetIds.length == 0)
            return;

        // Pass the activity to ourselves, because starting another activity is
        // not working.
        final int viewId = getIntentViewId();
        Intent activityIntent;
        PendingIntent activityPendingIntent;
        String packageName = context.getPackageName();
        RemoteViews views;
        int layoutId = getLayoutId();
        long now = System.currentTimeMillis();

        for (int appWidgetId : appWidgetIds) {
            activityIntent = new Intent(context, ZmanimActivity.class);
            activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            activityPendingIntent = PendingIntent.getActivity(context, appWidgetId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views = new RemoteViews(packageName, layoutId);

            if (isRemoteList()) {
                populateRemoteTimes(appWidgetId, views, activityPendingIntent);
            } else {
                populateRegularTimes(appWidgetId, views, activityPendingIntent, viewId, now);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        scheduleForMidnight(context, appWidgetIds);
    }

    /**
     * Populate the list with times.
     *
     * @param context
     *         the context.
     */
    protected void populateTimes(Context context) {
        populateTimes(context, AppWidgetManager.getInstance(context), null);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (locations != null)
            locations.stop(this);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        if (locations != null)
            locations.start(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        notifyAppWidgetViewDataChanged(context);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onAddressChanged(Location location, ZmanimAddress address) {
    }

    @Override
    public void onElevationChanged(Location location) {
        onLocationChanged(location);
    }

    /**
     * Schedule an update for midnight to populate the new day's list.
     *
     * @param context
     *         the context.
     * @param appWidgetIds
     *         the widget ids for which an update is needed.
     */
    private void scheduleForMidnight(Context context, int[] appWidgetIds) {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 100);
        midnight.add(Calendar.DATE, 1);
        Intent alarmIntent = new Intent(context, ZmanimWidget.class);
        alarmIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        alarmIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC, midnight.getTimeInMillis(), alarmPendingIntent);
    }

    /**
     * Bind the times to remote views.
     *
     * @param list
     *         the remote views.
     * @param adapter
     *         the list adapter.
     */
    protected void bindViews(RemoteViews list, ZmanimAdapter adapter) {
        Context context = this.context;
        final int count = adapter.getCount();
        ZmanimItem item;

        int positionSunset = -1;

        for (int position = 0; position < count; position++) {
            item = adapter.getItem(position);

            if (item.titleId == R.string.sunset) {
                positionSunset = position;
                break;
            }
        }

        int positionToday = -1;

        for (int position = 0; position < count; position++) {
            item = adapter.getItem(position);
            bindView(list, position, item);

            if ((position <= positionSunset) && !item.elapsed && (item.time != ZmanimAdapter.NEVER) && (item.timeLabel != null)) {
                positionToday = position;
            }
        }

        Calendar date = adapter.getCalendar().getCalendar();
        JewishDate jewishDate = new JewishDate(date);

        // If we are before sunset, then show "today" header.
        CharSequence dateHebrew = (positionToday >= 0) ? adapter.formatDate(context, jewishDate) : null;
        bindViewGrouping(list, 0, R.id.today_row, R.id.today_date, dateHebrew);

        jewishDate.forward();
        dateHebrew = adapter.formatDate(context, jewishDate);
        bindViewGrouping(list, 1, R.id.tomorrow_row, R.id.tomorrow_date, dateHebrew);
    }

    /**
     * Bind the item to remote views.
     *
     * @param list
     *         the remote list.
     * @param position
     *         the position index.
     * @param item
     *         the zmanim item.
     */
    protected void bindView(RemoteViews list, int position, ZmanimItem item) {
        if (item.elapsed || (item.time == ZmanimAdapter.NEVER) || (item.timeLabel == null)) {
            list.setViewVisibility(item.rowId, View.GONE);
        } else {
            list.setViewVisibility(item.rowId, View.VISIBLE);
            list.setTextViewText(item.timeId, item.timeLabel);
        }
    }

    /**
     * Is the device made by Nokia?
     *
     * @return {@code true} if either the brand or manufacturer start with
     * {@code "Nokia"}.
     */
    protected boolean isDeviceNokia() {
        return Build.BRAND.startsWith("Nokia") || Build.MANUFACTURER.startsWith("Nokia");
    }

    /**
     * Get the layout.
     *
     * @return the layout id.
     */
    protected int getLayoutId() {
        if (isRemoteList())
            return R.layout.widget_list;
        if (isDeviceNokia())
            return R.layout.times_widget_nokia;
        return R.layout.times_widget;
    }

    /**
     * Get the view for the intent click.
     *
     * @return the view id.
     */
    protected int getIntentViewId() {
        return android.R.id.list;
    }

    /**
     * Is the widget a list with remote adapter?
     *
     * @return {@code true} if remote list.
     */
    protected boolean isRemoteList() {
        return false;
    }

    /**
     * Bind the times to remote list view.
     *
     * @param appWidgetId
     *         the app widget id.
     * @param list
     *         the remote list.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void bindListView(int appWidgetId, RemoteViews list) {
        Intent service = new Intent();
        service.setClassName(context, "net.sf.times.ZmanimWidgetService");
        service.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));
        list.setRemoteAdapter(appWidgetId, android.R.id.list, service);
    }

    protected void notifyAppWidgetViewDataChanged(Context context) {
        if (isRemoteList()) {
            notifyAppWidgetViewDataChanged11(context);
        } else {
            populateTimes(context);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void notifyAppWidgetViewDataChanged11(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final Class<?> clazz = getClass();
        ComponentName provider = new ComponentName(context, clazz);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(provider);
        if ((appWidgetIds == null) || (appWidgetIds.length == 0))
            return;
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, android.R.id.list);
    }

    /**
     * Bind the date group header to a list.
     *
     * @param list
     *         the list.
     * @param position
     *         the position index.
     * @param rowId
     *         the row id.
     * @param textId
     *         the text id.
     * @param label
     *         the formatted Hebrew date label.
     */
    protected void bindViewGrouping(RemoteViews list, int position, int rowId, int textId, CharSequence label) {
        if ((position < 0) || (label == null)) {
            list.setViewVisibility(rowId, View.GONE);
        } else {
            list.setViewVisibility(rowId, View.VISIBLE);
            list.setTextViewText(textId, label);
        }
    }

    protected void populateRegularTimes(int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent, int viewId, long now) {
        views.setOnClickPendingIntent(viewId, activityPendingIntent);

        if (settings == null)
            settings = new ZmanimSettings(context);

        ZmanimLocations locations = this.locations;
        if (locations == null) {
            ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
            locations = app.getLocations();
            locations.start(this);
            this.locations = locations;
        }
        GeoLocation gloc = locations.getGeoLocation();
        if (gloc == null)
            return;

        ZmanimAdapter adapter = new ZmanimAdapter(context, settings);
        adapter.setCalendar(now);
        adapter.setGeoLocation(gloc);
        adapter.setInIsrael(locations.inIsrael());
        adapter.populate(true);
        bindViews(views, adapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void populateRemoteTimes(int appWidgetId, RemoteViews views, PendingIntent activityPendingIntent) {
        views.setPendingIntentTemplate(android.R.id.list, activityPendingIntent);
        bindListView(appWidgetId, views);
    }
}
