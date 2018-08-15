package at.linuxtage.companion;

import android.app.Application;
import android.preference.PreferenceManager;
import at.linuxtage.companion.alarms.FosdemAlarmManager;
import at.linuxtage.companion.db.DatabaseManager;

public class GLTApplication extends Application {

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
