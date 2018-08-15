package net.iexos.musicalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;

import java.text.DateFormat;
import java.util.Date;

public class NotificationUtils {

    //class is never instantiated
    private NotificationUtils() {
    }

    private static Notification.Builder addSnoozeButton(Context con, Notification.Builder builder) {
        return builder.addAction(R.drawable.ic_snooze,
                con.getString(R.string.notification_snooze),
                AlarmService.getPendingStateChangeIntent(con,
                        AlarmService.StateChange.SNOOZE));
    }

    private static Notification.Builder addDismissAlarmButton(Context con, Notification.Builder builder) {
        return builder.addAction(R.drawable.ic_alarm_off,
                con.getString(R.string.notification_dismiss_alarm),
                AlarmService.getPendingStateChangeIntent(con,
                        AlarmService.StateChange.STOP_RINGING));
    }

    private static Notification.Builder addDismissAllButton(Context con, Notification.Builder builder) {
        return builder.addAction(R.drawable.ic_action_cancel,
                con.getString(R.string.notification_dismiss_all),
                AlarmService.getPendingStateChangeIntent(con,
                        AlarmService.StateChange.STOP_ALL));
    }

    private static Notification.Builder getNotificationBuilder(Context con) {
        return new Notification.Builder(con)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_ALARM)
                .setLargeIcon(BitmapFactory.decodeResource(con.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_alarm_note);
    }

    public static void displayStoppedNotification(Context con) {
        NotificationManager mNotifyMgr =
                (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        if (AlarmUtils.isAlarmSet(con)) {
            long time = AlarmUtils.getTriggerTime(con);
            String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
            String title = con.getString(R.string.notification_title_waiting);
            String text = con.getString(R.string.notification_waiting, timeString);

            Notification.Builder builder = getNotificationBuilder(con);
            builder.setWhen(time);
            builder.setContentTitle(title);
            builder.setContentText(text);

            builder = addDismissAlarmButton(con, builder);

            mNotifyMgr.notify(AlarmService.NOTIFICATION_ID, builder.build());
        }
        else {
            mNotifyMgr.cancel(AlarmService.NOTIFICATION_ID);
        }
    }

    public static void displayForegroundNotification(Service con, AlarmService.State state) {
        String title;
        String text;
        Notification.Builder builder = getNotificationBuilder(con);

        if (state == AlarmService.State.RINGING) {
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            builder.setVibrate(new long[0]);

            title = con.getString(R.string.notification_title_ringing);
            text = con.getString(R.string.notification_ringing);
            builder = addSnoozeButton(con, builder);
            builder = addDismissAlarmButton(con, builder);
        }
        else {
            title = con.getString(R.string.notification_title_playing);
            text = con.getString(R.string.notification_playing);

            if (AlarmUtils.isAlarmSet(con)) {
                long time = AlarmUtils.getTriggerTime(con);
                String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
                text = con.getString(R.string.notification_playing_upcoming, timeString);
                builder.setWhen(time);
                builder = addDismissAlarmButton(con, builder);
            }

            builder = addDismissAllButton(con, builder);
        }

        builder.setContentTitle(title);
        builder.setContentText(text);

        con.startForeground(AlarmService.NOTIFICATION_ID, builder.build());
    }
}
