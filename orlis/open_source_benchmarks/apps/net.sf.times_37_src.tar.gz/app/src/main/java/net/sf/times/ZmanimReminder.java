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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateUtils;
import android.util.Log;

import net.sf.times.ZmanimAdapter.ZmanimItem;
import net.sf.times.location.ZmanimLocations;
import net.sf.times.preference.ZmanimSettings;
import net.sourceforge.zmanim.hebrewcalendar.JewishCalendar;
import net.sourceforge.zmanim.util.GeoLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Reminders. Receive alarm events, or date-time events, to update reminders.
 *
 * @author Moshe Waisberg
 */
public class ZmanimReminder extends BroadcastReceiver {

    private static final String TAG = "ZmanimReminder";

    /**
     * Reminder id for all notifications.<br>
     * Newer notifications will override current notifications.
     */
    private static final int ID_NOTIFY = 1;
    /** Reminder id for alarms. */
    private static final int ID_ALARM = 2;

    private static final long WAS_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;
    private static final long SOON_DELTA = 30 * DateUtils.SECOND_IN_MILLIS;
    /** The number of days per week. */
    private static final int DAYS_PER_WEEK = 7;

    private static final int LED_COLOR = Color.YELLOW;
    private static final int LED_ON = 750;
    private static final int LED_OFF = 500;

    /** Extras name for the reminder title. */
    private static final String EXTRA_REMINDER_TITLE = "reminder_title";
    /** Extras name for the reminder text. */
    private static final String EXTRA_REMINDER_TEXT = "reminder_text";
    /** Extras name for the reminder time. */
    private static final String EXTRA_REMINDER_TIME = "reminder_time";

    private Context context;
    private SimpleDateFormat dateFormat;
    /** The adapter. */
    private ZmanimAdapter adapter;
    private Method setLatestEventInfo;

    /**
     * Creates a new reminder manager.
     *
     * @param context
     *         the context.
     */
    public ZmanimReminder(Context context) {
        this.context = context;
    }

    /** No-argument constructor for broadcast receiver. */
    public ZmanimReminder() {
    }

    /**
     * Setup the first reminder for today.
     *
     * @param settings
     *         the settings.
     */
    public void remind(ZmanimSettings settings) {
        final Context context = this.context;
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        ZmanimLocations locations = app.getLocations();
        GeoLocation gloc = locations.getGeoLocation();
        // Have we been destroyed?
        if (gloc == null)
            return;

        ZmanimAdapter adapter = this.adapter;
        if (adapter == null) {
            adapter = new ZmanimAdapter(context, settings);
            this.adapter = adapter;
        }
        adapter.setCalendar(System.currentTimeMillis());
        adapter.setGeoLocation(gloc);
        adapter.setInIsrael(locations.inIsrael());
        adapter.populate(false);

        remind(settings, adapter);
    }

    /**
     * Setup the first reminder for the week.
     *
     * @param settings
     *         the settings.
     * @param adapter
     *         the populated adapter.
     */
    private void remind(ZmanimSettings settings, ZmanimAdapter adapter) {
        Log.i(TAG, "remind");

        final Context context = this.context;
        final Calendar gcal = Calendar.getInstance();
        final long now = gcal.getTimeInMillis();
        final long latest = settings.getLatestReminder();
        Log.i(TAG, "remind latest [" + formatDateTime(latest) + "]");
        final long was = now - WAS_DELTA;
        final long soon = now + SOON_DELTA;
        ZmanimItem item;
        ZmanimItem itemFirst = null;
        long before;
        long when;
        long whenFirst = Long.MAX_VALUE;
        boolean nextDay = true;
        int id;
        int count;

        JewishCalendar jcal = new JewishCalendar(gcal);
        ZmanimApplication app = (ZmanimApplication) context.getApplicationContext();
        ZmanimLocations locations = app.getLocations();
        jcal.setInIsrael(locations.inIsrael());
        Calendar cal = adapter.getCalendar().getCalendar();

        // Find the first reminder in the upcoming week.
        for (int day = 1; nextDay && (day <= DAYS_PER_WEEK); day++) {
            if (day > 1) {
                gcal.add(Calendar.DAY_OF_MONTH, 1);
                jcal.setDate(gcal);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                adapter.populate(false);
            }

            count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                item = adapter.getItem(i);
                id = item.timeId;
                before = settings.getReminder(id);

                if ((before >= 0L) && (item.time != ZmanimAdapter.NEVER) && allowReminder(item, jcal, settings)) {
                    when = item.time - before;
                    if (nextDay && (latest < was) && (was <= when) && (when <= soon)) {
                        notifyNow(context, settings, item);
                        nextDay = false;
                    }
                    if ((now < when) && (when < whenFirst)) {
                        itemFirst = item;
                        whenFirst = when;
                    }
                }
            }
        }
        if (itemFirst != null) {
            String whenFormat = formatDateTime(whenFirst);
            String timeFormat = formatDateTime(itemFirst.time);
            Log.i(TAG, "notify at [" + whenFormat + "] for [" + timeFormat + "]");
            notifyFuture(context, itemFirst, whenFirst);
        }
    }

    /**
     * Cancel all reminders.
     */
    public void cancel() {
        Log.i(TAG, "cancel");
        final Context context = this.context;
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = createAlarmIntent(context, null);
        alarms.cancel(alarmIntent);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    /**
     * Notify now.
     *
     * @param context
     *         the context.
     * @param settings
     *         the settings.
     * @param item
     *         the zmanim item to notify about.
     */
    private void notifyNow(Context context, ZmanimSettings settings, ZmanimItem item) {
        CharSequence contentTitle = context.getText(item.titleId);
        CharSequence contentText = item.summary;
        long when = item.time;
        ZmanimReminderItem reminderItem = new ZmanimReminderItem(contentTitle, contentText, when);

        notifyNow(context, settings, reminderItem);
    }

    /**
     * Notify now.
     *
     * @param context
     *         the context.
     * @param settings
     *         the settings.
     * @param item
     *         the reminder item.
     */
    private void notifyNow(Context context, ZmanimSettings settings, ZmanimReminderItem item) {
        // Clicking on the item will launch the main activity.
        PendingIntent contentIntent = createActivityIntent(context);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notification = createNotification(context, settings, item, contentIntent);
        } else {
            notification = createNotificationEclair(context, settings, item, contentIntent);
        }

        postNotification(notification, settings);
    }

    /**
     * Set alarm manager to alert us for the next reminder.
     *
     * @param context
     *         the context.
     * @param item
     *         the zmanim item to notify about.
     * @param triggerAt
     *         the upcoming reminder.
     */
    private void notifyFuture(Context context, ZmanimItem item, long triggerAt) {
        CharSequence contentTitle = context.getText(item.titleId);
        long when = item.time;

        Log.i(TAG, "notify future [" + contentTitle + "] at [" + formatDateTime(triggerAt) + "] for " + formatDateTime(when));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = createAlarmIntent(context, item);
        manager.set(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent);
    }

    private PendingIntent createActivityIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ID_NOTIFY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private PendingIntent createAlarmIntent(Context context, ZmanimItem item) {
        Intent intent = new Intent(context, ZmanimReminder.class);

        if (item != null) {
            CharSequence contentTitle = context.getText(item.titleId);
            CharSequence contentText = item.summary;
            long when = item.time;

            intent.putExtra(EXTRA_REMINDER_TITLE, contentTitle);
            intent.putExtra(EXTRA_REMINDER_TEXT, contentText);
            intent.putExtra(EXTRA_REMINDER_TIME, when);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String nowFormat = formatDateTime(System.currentTimeMillis());
        Log.i(TAG, "onReceive " + intent + " [" + nowFormat + "]");

        this.context = context;
        boolean update = false;
        ZmanimSettings settings = new ZmanimSettings(context);

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            update = true;
        } else if (Intent.ACTION_DATE_CHANGED.equals(action)) {
            update = true;
        } else if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            update = true;
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            update = true;
        } else {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.getInt(Intent.EXTRA_ALARM_COUNT, 0) > 0) {
                    CharSequence contentTitle = extras.getCharSequence(EXTRA_REMINDER_TITLE);
                    CharSequence contentText = extras.getCharSequence(EXTRA_REMINDER_TEXT);
                    long when = extras.getLong(EXTRA_REMINDER_TIME, 0L);

                    if ((contentTitle != null) && (contentText != null) && (when != 0L)) {
                        ZmanimReminderItem reminderItem = new ZmanimReminderItem(contentTitle, contentText, when);
                        notifyNow(context, settings, reminderItem);
                    }
                    update = true;
                }
            }
        }

        if (update) {
            remind(settings);
        }
    }

    /**
     * Format the date and time with seconds.<br>
     * The pattern is "{@code yyyy-MM-dd HH:mm:ss.SSS}"
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     */
    private String formatDateTime(Date time) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        }
        return dateFormat.format(time);
    }

    /**
     * Format the date and time with seconds.
     *
     * @param time
     *         the time to format.
     * @return the formatted time.
     * @see #formatDateTime(Date)
     */
    private String formatDateTime(long time) {
        return formatDateTime(new Date(time));
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private Notification createNotificationEclair(Context context, ZmanimSettings settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        CharSequence contentTitle = item.getTitle();
        CharSequence contentText = item.getText();
        long when = item.getTime();
        Log.i(TAG, "notify now [" + contentTitle + "] for " + formatDateTime(when));

        int audioStreamType = settings.getReminderStream();
        Uri sound = settings.getReminderRingtone();

        Notification notification = new Notification();
        notification.audioStreamType = audioStreamType;
        notification.icon = R.drawable.stat_notify_time;
        notification.defaults = Notification.DEFAULT_VIBRATE;
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = LED_COLOR;
        notification.ledOffMS = LED_OFF;
        notification.ledOnMS = LED_ON;
        notification.when = when;
        notification.sound = sound;
        // Notification#setLatestEventInfo removed in Marshmallow.
        if (setLatestEventInfo == null) {
            Class<?> clazz = notification.getClass();
            try {
                setLatestEventInfo = clazz.getDeclaredMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            } catch (NoSuchMethodException nsme) {
                Log.e(TAG, "createNotificationEclair: " + nsme.getLocalizedMessage(), nsme);
            }
        }
        if (setLatestEventInfo != null) {
            try {
                setLatestEventInfo.invoke(notification, context, contentTitle, contentText, contentIntent);
            } catch (IllegalAccessException iae) {
                Log.e(TAG, "createNotificationEclair: " + iae.getLocalizedMessage(), iae);
            } catch (InvocationTargetException ite) {
                Log.e(TAG, "createNotificationEclair: " + ite.getLocalizedMessage(), ite);
            }
        }

        return notification;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Notification createNotification(Context context, ZmanimSettings settings, ZmanimReminderItem item, PendingIntent contentIntent) {
        CharSequence contentTitle = item.getTitle();
        CharSequence contentText = item.getText();
        long when = item.getTime();
        Log.i(TAG, "notify now [" + contentTitle + "] for " + formatDateTime(when));

        int audioStreamType = settings.getReminderStream();
        Uri sound = settings.getReminderRingtone();

        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setContentText(contentText);
        builder.setContentTitle(contentTitle);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
        builder.setLights(LED_COLOR, LED_ON, LED_OFF);
        builder.setSmallIcon(R.drawable.stat_notify_time);
        builder.setSound(sound, audioStreamType);
        builder.setWhen(when);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(true);
        }
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }

    @SuppressLint("Wakelock")
    private void postNotification(Notification notification, ZmanimSettings settings) {
        // Wake up the device to notify the user.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wake.acquire(5000L);// enough time to also hear an alarm tone

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(ID_NOTIFY, notification);

        // This was the last notification.
        final long now = System.currentTimeMillis();
        settings.setLatestReminder(now);
    }

    /**
     * Allow the reminder to send a notification?
     *
     * @param item
     *         the item that should be reminded.
     * @param jcal
     *         the Jewish calendar as of now.
     * @param settings
     *         the settings with reminder day flags.
     * @return can the reminder be activated?
     */
    private boolean allowReminder(ZmanimItem item, JewishCalendar jcal, ZmanimSettings settings) {
        final int timeId = item.timeId;

        int dayOfWeek = jcal.getDayOfWeek();
        int holidayIndex = jcal.getYomTovIndex();
        switch (holidayIndex) {
            case JewishCalendar.PESACH:
            case JewishCalendar.SHAVUOS:
            case JewishCalendar.ROSH_HASHANA:
            case JewishCalendar.YOM_KIPPUR:
            case JewishCalendar.SUCCOS:
            case JewishCalendar.SHEMINI_ATZERES:
            case JewishCalendar.SIMCHAS_TORAH:
                dayOfWeek = Calendar.SATURDAY;
                break;
        }

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return settings.isReminderSunday(timeId);
            case Calendar.MONDAY:
                return settings.isReminderMonday(timeId);
            case Calendar.TUESDAY:
                return settings.isReminderTuesday(timeId);
            case Calendar.WEDNESDAY:
                return settings.isReminderWednesday(timeId);
            case Calendar.THURSDAY:
                return settings.isReminderThursday(timeId);
            case Calendar.FRIDAY:
                return settings.isReminderFriday(timeId);
            case Calendar.SATURDAY:
                return settings.isReminderSaturday(timeId);
        }

        return true;
    }
}
