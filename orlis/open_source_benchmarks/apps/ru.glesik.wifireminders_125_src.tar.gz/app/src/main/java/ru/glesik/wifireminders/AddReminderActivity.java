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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class AddReminderActivity extends Activity {

	private
		BroadcastReceiver scanReceiver;
		ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_reminder);
		// Show the Up button in the action bar.
		setupActionBar();
		// Prepare receiver for AP scan results.
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		scanReceiver = new BroadcastReceiver() {
			public void onReceive(Context c, Intent i) {
				// Scan results are available.
				WifiManager w = (WifiManager) c
						.getSystemService(Context.WIFI_SERVICE);
				// Handle scan results.
				scanResultHandler(w.getScanResults());
			}
		};
		registerReceiver(scanReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(scanReceiver);
	}

	protected void onResume() {
		super.onResume();
		// Creating adapter to populate spinnerSSID items.
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner Items = (Spinner) findViewById(R.id.spinnerSSID);
		Items.setAdapter(adapter);
		WifiManager mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// Start scanning for visible networks.
		if (mainWifi.isWifiEnabled()) {
			mainWifi.startScan();
			// Getting list of stored networks.
			List<WifiConfiguration> wifiList = mainWifi.getConfiguredNetworks();
			for (WifiConfiguration result : wifiList) {
				// Removing quotes.
				adapter.add(result.SSID.toString().replaceAll(
						"^\"|\"$", ""));
			}
			adapter.notifyDataSetChanged();
		}
		else {
			new AlertDialog.Builder(this)
		    .setTitle(R.string.error_wifi_off_title)
		    .setMessage(R.string.error_wifi_off_text)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
		        	finish();
		        }
		     })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		        	finish();
		        }
		     })
		    .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_reminder, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_save:
			// Save Rule selected, saving data to SharedPreferences.
			Spinner spinnerSSID = (Spinner) findViewById(R.id.spinnerSSID);
			String selectedSSID = spinnerSSID.getSelectedItem().toString();
			SharedPreferences sharedPrefRules = getSharedPreferences("rules",
					MODE_PRIVATE);
			// Getting number of rules.
			int rulesCount = sharedPrefRules.getInt("RulesCount", 0);
			// Saving new rule to SharedPreferences.
			SharedPreferences.Editor editor = sharedPrefRules.edit();
			EditText editRuleTitle = (EditText) findViewById(R.id.editTitle);
			EditText editReminderText = (EditText) findViewById(R.id.editReminderText);
			editor.putString("Title" + Integer.toString(rulesCount + 1),
					editRuleTitle.getText().toString());
			editor.putString("Text" + Integer.toString(rulesCount + 1),
					editReminderText.getText().toString());
			editor.putString("SSID" + Integer.toString(rulesCount + 1),
					selectedSSID);
			editor.putBoolean("Enabled" + Integer.toString(rulesCount + 1),
					true);
			editor.putInt("RulesCount", rulesCount + 1);
			editor.commit();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void scanResultHandler(List<ScanResult> wifiList) {
		SharedPreferences sharedPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
		String intervalString = sharedPrefSettings.getString("prefInterval", "0");
		int interval = Integer.parseInt(intervalString);
		if (interval != 0) { // Polling enabled.
			for (ScanResult result : wifiList) {
				Boolean dupe = false;
				// Checking for duplicate entries (saved and visible).
				for (int i = 0; i < adapter.getCount(); i++) {
					if (adapter.getItem(i).equals(result.SSID)) {
						dupe = true;
					}
				}
				// Appending SSID to the list.
				if (!dupe)
					adapter.add(result.SSID);
			}
			adapter.notifyDataSetChanged();
		}
	}

}
