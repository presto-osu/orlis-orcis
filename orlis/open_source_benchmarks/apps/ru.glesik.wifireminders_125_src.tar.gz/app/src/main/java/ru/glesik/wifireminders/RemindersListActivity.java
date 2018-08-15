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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class RemindersListActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reminders_list);
		// Create adapter for remindersListView.
		ListView listView = (ListView) findViewById(R.id.remindersListView);
		ArrayList<String> listItems = new ArrayList<>();
		ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_multiple_choice, listItems);
		listView.setAdapter(listAdapter);
		// Create context menu.
		registerForContextMenu(listView);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Check box was clicked.
				ListView listView = (ListView) findViewById(R.id.remindersListView);
				Boolean chk = listView.isItemChecked(position);
				SharedPreferences sharedPrefRules = getSharedPreferences(
						"rules", MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPrefRules.edit();
				editor.putBoolean("Enabled" + Integer.toString(position + 1),
						chk);
				editor.commit();
				// Recounting reminders and restarting alarms.
				refreshList(false);
			}
		});
		// Read default settings in case user hasn't set any.
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshList(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.reminders_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			// Show Add Reminder activity.
			Intent ari = new Intent(this, AddReminderActivity.class);
			startActivity(ari);
			return true;
		case R.id.action_settings:
			// Show Add Reminder activity.
			Intent si = new Intent(this, SettingsActivity.class);
			startActivity(si);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// Inflate the menu.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reminders_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.action_delete:
			// Delete selected.
			SharedPreferences sharedPrefRulesD = getSharedPreferences("rules",
					MODE_PRIVATE);
			int rulesCount = sharedPrefRulesD.getInt("RulesCount", 0);
			// Removing item: shifting all up starting from selected.
			SharedPreferences.Editor editor = sharedPrefRulesD.edit();
			for (int i = info.position + 1; i < rulesCount; i++) {
				editor.putString(
						"Title" + Integer.toString(i),
						sharedPrefRulesD.getString(
								"Title" + Integer.toString(i + 1), "error"));
				editor.putString(
						"Text" + Integer.toString(i),
						sharedPrefRulesD.getString(
								"Text" + Integer.toString(i + 1), "error"));
				editor.putString(
						"SSID" + Integer.toString(i),
						sharedPrefRulesD.getString(
								"SSID" + Integer.toString(i + 1), "error"));
				editor.putBoolean(
						"Enabled" + Integer.toString(i),
						sharedPrefRulesD.getBoolean(
								"Enabled" + Integer.toString(i + 1), false));
			}
			// Removing last item.
			editor.remove("Title" + Integer.toString(rulesCount));
			editor.remove("Text" + Integer.toString(rulesCount));
			editor.remove("SSID" + Integer.toString(rulesCount));
			editor.remove("Enabled" + Integer.toString(rulesCount));
			editor.putInt("RulesCount", rulesCount - 1);
			editor.commit();
			refreshList(true);
			return true;
		case R.id.action_edit:
			// Edit Text selected.
			final SharedPreferences sharedPrefRulesE = getSharedPreferences(
					"rules", MODE_PRIVATE);
			// Showing dialog with selected item's reminder text to edit.
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.action_edit);
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
					| InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
			input.setSingleLine(false);
			input.setText(sharedPrefRulesE.getString(
					"Text" + Integer.toString(info.position + 1), "error"));
			alert.setView(input);
			alert.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// OK pressed: storing new value and refreshing
							// list.
							String value = input.getText().toString();
							SharedPreferences.Editor editor = sharedPrefRulesE
									.edit();
							editor.putString(
									"Text"
											+ Integer
													.toString(info.position + 1),
									value);
                            editor.putBoolean("Enabled" + Integer.toString(info.position + 1),
                                    true);
							editor.commit();
                            refreshList(true);
						}
					});
			alert.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Cancel pressed.
						}
					});
			alert.show();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void refreshList(boolean redraw) {
        // Create adapter for remindersListView.
        ListView listView = (ListView) findViewById(R.id.remindersListView);
        ArrayList<String> listItems = new ArrayList<>();
        if (redraw) {
            ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this,
					android.R.layout.simple_list_item_multiple_choice, listItems);
            listView.setAdapter(listAdapter);
        }
		Bundle bundle = new Bundle();
		SharedPreferences sharedPreferences = getSharedPreferences("rules",
				MODE_PRIVATE);
		int rulesCount = sharedPreferences.getInt("RulesCount", 0);
		int activeCount = 0;
		for (int i = 1; i <= rulesCount; i++) {
            if (redraw) {
                listItems.add(sharedPreferences.getString(
                        "Title" + Integer.toString(i), "error")
                        + " ("
                        + sharedPreferences.getString("SSID" + Integer.toString(i),
                        "error") + ")");
            }
			if (sharedPreferences.getBoolean("Enabled" + Integer.toString(i),
					false)) {
                if (redraw) {
                    listView.setItemChecked(i - 1, true);
                }
				activeCount++;
				// Add SSIDs of active reminders to bundle.
				bundle.putString(
						"SSID" + Integer.toString(activeCount),
						sharedPreferences.getString(
								"SSID" + Integer.toString(i), "error"));
			} else {
                if (redraw) {
                    listView.setItemChecked(i - 1, false);
                }
			}
		}
		// Add active reminders count to bundle.
		bundle.putInt("ActiveRulesCount", activeCount);
		SharedPreferences sharedPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
		String intervalString = sharedPrefSettings.getString("prefInterval", "0");
		if (intervalString == null)
			intervalString = "0";
		// Enable alarm if polling is enabled and there are active rules.
		if ((activeCount > 0) && (!intervalString.equals("0"))) {
			startAlarm();
		} else {
			stopAlarm();
		}
	}


	public void startAlarm() {
		SharedPreferences sharedPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
		String intervalString = sharedPrefSettings.getString("prefInterval", "0");
		int interval = Integer.parseInt(intervalString);
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		am.cancel(pi);
		am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, pi);
		// Enable boot receiver
		ComponentName receiver = new ComponentName(this, BootReceiver.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		        PackageManager.DONT_KILL_APP);
	}

	public void stopAlarm() {
		AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		am.cancel(pi);
		// Disable boot receiver
		ComponentName receiver = new ComponentName(this, BootReceiver.class);
		PackageManager pm = this.getPackageManager();

		pm.setComponentEnabledSetting(receiver,
		        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		        PackageManager.DONT_KILL_APP);
	}

}
