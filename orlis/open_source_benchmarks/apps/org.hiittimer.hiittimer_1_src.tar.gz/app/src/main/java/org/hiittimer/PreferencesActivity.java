/*
 * 
 * HIIT Timer - A simple timer for high intensity trainings
 Copyright (C) 2015 Lorenzo Chiovini

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.hiittimer;

import org.hiittimer.hiittimer.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new PreferencesFragment()).commit();

	}

	public static class PreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		@Override
		public void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

		}

		@Override
		public void onResume() {

			super.onResume();

			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {

			super.onPause();

			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (getString(R.string.preferences_volume_key).equals(key)) {
				final ListPreference volumeLevels = (ListPreference) findPreference(key);

				volumeLevels.setSummary(volumeLevels.getEntry());
			} else if (getString(R.string.preferences_training_screen_text_size_key).equals(key)) {
				final ListPreference trainingScreenTextSize = (ListPreference) findPreference(key);

				trainingScreenTextSize.setSummary(trainingScreenTextSize.getEntry());
			}

		}

	}
}
