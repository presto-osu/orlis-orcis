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

import org.hiittimer.database.generated.DaoMaster;
import org.hiittimer.database.generated.DaoMaster.DevOpenHelper;
import org.hiittimer.database.generated.DaoSession;
import org.hiittimer.hiittimer.R;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class HIITTimerApplication extends Application {
	private DaoMaster daoMaster;
	private DaoSession daoSession;
	private final static String DATABASENAME = "org.hiittimer.db";

	private void setupPreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		setupCreateNewTrainingHelpDialog(sharedPreferences);

		setupTrainingPlansHelpDialog(sharedPreferences);
	}

	private void setupCreateNewTrainingHelpDialog(final SharedPreferences sharedPreferences) {
		if (sharedPreferences.getString(getString(R.string.create_new_training_show_help_dialog_key), null) == null) {
			sharedPreferences
					.edit()
					.putString(getString(R.string.create_new_training_show_help_dialog_key),
							getString(R.string.true_string)).apply();
		}
	}

	private void setupTrainingPlansHelpDialog(final SharedPreferences sharedPreferences) {
		if (sharedPreferences.getString(getString(R.string.training_plans_show_help_dialog_key), null) == null) {
			sharedPreferences
					.edit()
					.putString(getString(R.string.training_plans_show_help_dialog_key), getString(R.string.true_string))
					.apply();
		}
	}

	@Override
	public void onCreate() {
		setupPreferences();

		DevOpenHelper devOpenerHelper = new DaoMaster.DevOpenHelper(this, DATABASENAME, null);
		daoMaster = new DaoMaster(devOpenerHelper.getWritableDatabase());
		daoSession = daoMaster.newSession();
	}

	public DaoSession getDaoSession() {
		return daoSession;
	}

}
