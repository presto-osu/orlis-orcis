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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {
	public WifiReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
	    if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
	    	NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	    	NetworkInfo.State state = netInfo.getState();
	    	if (state == NetworkInfo.State.CONNECTED) {
				// CONNECTED is received twice, so compare to stored value to avoid dupes.
				SharedPreferences sharedPreferences = context.getSharedPreferences("runtime", Context.MODE_PRIVATE);
				boolean isConnected = sharedPreferences.getBoolean("IsConnected", false);
				if (!isConnected) {  //
					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					String SSID = wifiInfo.getSSID().replaceAll("^\"|\"$", "");
					// Starting service, to be sure we're not killed too soon.
					Intent serviceIntent = new Intent(context, AlarmService.class);
					serviceIntent.putExtra("SSID", SSID);
					context.startService(serviceIntent);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putBoolean("IsConnected", true);
					editor.commit();
				}
	    	}
			if (state == NetworkInfo.State.DISCONNECTED) {
				SharedPreferences sharedPreferences = context.getSharedPreferences("runtime", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean("IsConnected", false);
				editor.commit();
			}
	    }
	}
}
