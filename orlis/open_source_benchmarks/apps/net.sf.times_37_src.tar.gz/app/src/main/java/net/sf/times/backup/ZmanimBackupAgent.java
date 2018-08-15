package net.sf.times.backup;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.Build;

/**
 * Backup agent helper for the application.
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class ZmanimBackupAgent extends BackupAgentHelper {

    /** A key to uniquely identify the set of backup data. */
    private static final String PREFS_BACKUP_KEY = "prefs";

    @Override
    public void onCreate() {
        super.onCreate();

        String prefsName = getDefaultSharedPreferencesName(this);
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, prefsName);
        addHelper(PREFS_BACKUP_KEY, helper);
    }

    /** Copied from android.preference.PreferenceManager */
    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }
}
