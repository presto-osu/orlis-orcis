package io.github.otakuchiyan.dnsman;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class ControlNotification implements ValueConstants{
    private static final String NOTIFICATION_TAG = "Control";

    public static void notify(final Context context, String dns1, String dns2) {
        final Resources res = context.getResources();

        final String title = res.getString(
                R.string.control_notification_title);
        final String text = res.getString(
                R.string.control_notification_placeholder_text, dns1, dns2);

        final Context appContext = context.getApplicationContext();

        PendingIntent applyIntent = PendingIntent.getService(appContext, 0,
                ExecuteIntentService.setWithLastDnsIntent(context), 0);
        PendingIntent restoreIntent = PendingIntent.getService(context, 0,
                ExecuteIntentService.restoreIntent(context), 0);
        PendingIntent changeAutoSettingIntent = PendingIntent.getService(context, 0,
                new Intent(context, ChangeAutoSettingService.class), 0);

        final Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.ic_menu_preferences)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class),
                                PendingIntent.FLAG_ONE_SHOT))
                .addAction(android.R.drawable.ic_menu_set_as,
                        context.getText(R.string.action_set_with_last_dns), applyIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        context.getText(R.string.action_restore), restoreIntent)
                .addAction(android.R.drawable.ic_menu_manage,
                        context.getString(R.string.action_change_auto_setting), changeAutoSettingIntent)
                .setOngoing(true);

        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_TAG, 0, builder.build());
        }


    public static class ChangeAutoSettingService extends IntentService{
        public ChangeAutoSettingService() {
            super("ChangeAutoSettingService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();

                boolean value = preferences.getBoolean(KEY_PREF_AUTO_SETTING, true);
                editor.putBoolean(KEY_PREF_AUTO_SETTING, !value);
                editor.apply();
                String toastString = getString(R.string.toast_auto_setting_enabled);
                if(value){ //enabled -> disabled
                    toastString = getString(R.string.toast_auto_setting_disabled);
                }
                Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_SHORT).show();
        }
    }
}
