package gq.nulldev.animeopenings.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Project: AnimeOpenings
 * Created: 11/10/15
 * Author: nulldev
 */
public class VersionPreference extends Preference {
    public VersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        String versionName;
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                versionName = null;
            }
            setSummary(versionName);
        }
    }
}
