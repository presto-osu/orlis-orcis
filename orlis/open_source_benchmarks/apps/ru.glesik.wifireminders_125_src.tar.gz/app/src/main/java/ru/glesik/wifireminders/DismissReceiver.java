/*
 * This file is part of Wi-Fi Reminders.
 *
 * Wi-Fi Reminders is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wi-Fi Reminders is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wi-Fi Reminders.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.glesik.wifireminders;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class DismissReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "rules", Context.MODE_PRIVATE);
        // Get SSID and notification id from extras set in AlarmService.
        String SSID = intent.getStringExtra("SSID");
        int id = intent.getIntExtra("id", -1);
        int rulesCount = sharedPreferences.getInt("RulesCount", 0);
        // Disable all rules with given SSID.
        for (int k = 1; k <= rulesCount; k++) {
            String currentSSID = sharedPreferences.getString("SSID"
                    + Integer.toString(k), "error");
            if (currentSSID != null) {
                if (currentSSID.equals(SSID)) {
                    SharedPreferences.Editor editor = sharedPreferences
                            .edit();
                    editor.putBoolean("Enabled" + Integer.toString(k),
                            false);
                    editor.commit();
                }
            }
        }
        // Hide notification.
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }

}
