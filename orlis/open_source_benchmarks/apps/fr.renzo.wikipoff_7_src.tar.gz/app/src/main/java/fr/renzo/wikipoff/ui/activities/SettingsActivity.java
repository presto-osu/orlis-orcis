/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.renzo.wikipoff.ui.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.renzo.wikipoff.ConfigManager;
import fr.renzo.wikipoff.R;
import fr.renzo.wikipoff.StorageUtils;
import fr.renzo.wikipoff.StorageUtils.StorageInfo;

public class SettingsActivity extends PreferenceActivity {
	private SharedPreferences config;
	private ListPreference myPref;
	private String currentStorage;
	@SuppressWarnings("unused")
	private static final String TAG = "SettingsActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		config = PreferenceManager.getDefaultSharedPreferences(this);

        String[] storage_names;
        String[] storage_paths;

		List<StorageInfo> availablestorageslist= new ArrayList<StorageInfo>();
		// Add default external storage
		ArrayList<StorageInfo> extsdlist = StorageUtils.getDefaultStorageInfo(this);
		if (extsdlist.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Warning, no external storage detected")
                    .setMessage("Unfortunately, I coudln't get access to your external Android Storage. I don't know how to work in this case =(. Please send the author some info about your device if you want him to fix it.")

                    .setPositiveButton("Okay...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            availablestorageslist.addAll(extsdlist);

            List<StorageInfo> allstorageslist = StorageUtils.getStorageList();
            for (Iterator<StorageInfo> iterator = allstorageslist.iterator(); iterator.hasNext(); ) {
                StorageInfo storageInfo = iterator.next();
                if (testWriteable(storageInfo.path)) {
                    if (!availablestorageslist.contains(storageInfo)) {
                        availablestorageslist.add(storageInfo);
                    }
                    Log.d(TAG, storageInfo.path + " is writeable");
                } else {
                    Log.d(TAG, storageInfo.path + " is not writeable");

                }
            }

            storage_names = new String[availablestorageslist.size()];
            storage_paths = new String[availablestorageslist.size()];
            for (int i = 0; i < availablestorageslist.size(); i++) {
                storage_names[i] = availablestorageslist.get(i).getDisplayName(this);
                storage_paths[i] = availablestorageslist.get(i).path;
            }


            currentStorage = config.getString(getString(R.string.config_key_storage), StorageUtils.getDefaultStorage(this));

            myPref = (ListPreference) findPreference(getString(R.string.config_key_storage));
            myPref.setEntries(storage_names);
            myPref.setEntryValues(storage_paths);
            myPref.setSummary(currentStorage);
            myPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String newStorage = (String) newValue;
                    myPref.setSummary(newStorage);
                    ConfigManager.clearSelectedDBFiles(SettingsActivity.this);
                    return true;
                }
            });
        }
	}

	private boolean testWriteable(String path) {
		boolean res=false;
		File f = new File(path,".testdir.wikipoff");
		f.mkdirs();
		if (f.exists()) {
			res=true;
			f.delete();
		}
		return res;
	}

}
