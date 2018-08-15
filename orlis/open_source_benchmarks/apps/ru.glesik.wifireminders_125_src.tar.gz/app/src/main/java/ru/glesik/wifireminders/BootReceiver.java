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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			SharedPreferences sharedPrefSettings = PreferenceManager.getDefaultSharedPreferences(context);
			String intervalString = sharedPrefSettings.getString("prefInterval", "0");
			int interval = Integer.parseInt(intervalString);
			if (interval != 0) {  // Polling enabled - start AlarmManager.
				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				Intent i = new Intent(context, AlarmReceiver.class);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
				am.cancel(pi);
				am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, pi);
			}
        }
	}


}
