package org.toulibre.cdl;

import org.toulibre.cdl.alarms.FosdemAlarmManager;
import org.toulibre.cdl.db.DatabaseManager;

import android.app.Application;
import android.preference.PreferenceManager;

public class CdlApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		DatabaseManager.init(this);
		// Initialize settings
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		// Alarms (requires settings)
		FosdemAlarmManager.init(this);
	}
}
