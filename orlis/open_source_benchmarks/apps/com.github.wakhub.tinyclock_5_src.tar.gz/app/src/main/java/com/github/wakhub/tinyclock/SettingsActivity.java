package com.github.wakhub.tinyclock;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

/**
 * Activity for changing settings
 *
 * Created by wak on 11/8/14.
 */
public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    // http://stackoverflow.com/questions/3590955/intent-to-launch-the-clock-application-on-android
    private static final String[][] CLOCK_APPS = {
            {"com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl"},
            {"com.android.deskclock", "com.android.deskclock.AlarmClock"},
            {"com.google.android.deskclock", "com.android.deskclock.DeskClock"},
            {"com.motorola.blur.alarmclock", "com.motorola.blur.alarmclock.AlarmClock"},
            {"com.sec.android.app.clockpackage", "com.sec.android.app.clockpackage.ClockPackage"}
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static final class SettingsFragment extends PreferenceFragment {

        private ComponentName clockAppComponentName = null;

        private Settings settings;

        private Preference.OnPreferenceChangeListener onColorChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String value = ((String) newValue).trim();
                Context context = getActivity();
                try {
                    Color.parseColor(value);
                    preference.setSummary(value);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.message_invalid_color, value),
                            Toast.LENGTH_LONG)
                            .show();
                    return false;
                }
                return true;
            }
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            settings = new Settings(getActivity().getApplicationContext());

            PackageManager packageManager = getActivity().getPackageManager();

            for (String[] item : CLOCK_APPS) {
                String packageName = item[0];
                String className = item[1];
                try {
                    ComponentName cn = new ComponentName(packageName, className);
                    packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                    clockAppComponentName = cn;
                    break;
                } catch (PackageManager.NameNotFoundException e) {
                    onNameNotFound(e);
                }
            }

            addPreferencesFromResource(R.xml.preferences);

            final Resources res = getResources();
            Preference pref;

            pref = findPreference(res.getString(R.string.pref_text_color_key));
            pref.setSummary(settings.getTextColorString());
            pref.setOnPreferenceChangeListener(onColorChangeListener);

            pref = findPreference(res.getString(R.string.pref_background_color_key));
            pref.setSummary(settings.getBackgroundColorString());
            pref.setOnPreferenceChangeListener(onColorChangeListener);

            pref = findPreference(res.getString(R.string.pref_reset_style_key));
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showResetStyleDialog();
                    return false;
                }
            });

            pref = findPreference(res.getString(R.string.pref_open_clock_app_key));
            if (clockAppComponentName != null) {
                try {
                    Drawable icon = packageManager.getApplicationIcon(clockAppComponentName.getPackageName());
                    if (icon != null) {
                        pref.setIcon(icon);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    onNameNotFound(e);
                }
            }
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    onClickOpenClockApp();
                    return true;
                }
            });

            pref = findPreference(res.getString(R.string.pref_open_datetime_settings_key));
            try {
                Drawable icon = packageManager.getApplicationIcon("com.android.settings");
                if (icon != null) {
                    pref.setIcon(icon);
                }
            } catch (PackageManager.NameNotFoundException e) {
                onNameNotFound(e);
            }
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                    return false;
                }
            });

            pref = findPreference(res.getString(R.string.pref_app_info_key));
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        getActivity().getApplication().getPackageName(), 0);
                long apkSize = new File(packageInfo.applicationInfo.publicSourceDir).length();
                pref.setSummary(String.format(
                        "Name: %1$s\nVersion: %2$s\nSize: %3$dKB",
                        packageInfo.packageName,
                        packageInfo.versionName,
                        apkSize / 1024L));
            } catch (PackageManager.NameNotFoundException e) {
                onNameNotFound(e);
            }
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.github.wakhub.tinyclock");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getActivity().startActivity(intent);
                    return false;
                }
            });
        }

        @Override
        public void onPause() {
            super.onPause();
            updateWidget();
        }

        private void showResetStyleDialog() {
            final Resources res = getResources();
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.pref_reset_style_title)
                    .setMessage(R.string.message_confirm)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @SuppressLint("CommitPrefEdits")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            prefs.edit()
                                    .putString(
                                            res.getString(R.string.pref_text_color_key),
                                            res.getString(R.string.text_color_default))
                                    .putString(
                                            res.getString(R.string.pref_background_color_key),
                                            res.getString(R.string.background_color_default))
                                    .commit();
                            Toast.makeText(getActivity(), R.string.message_success_reset_style, Toast.LENGTH_SHORT).show();
                            Resources res = getResources();
                            findPreference(res.getString(R.string.pref_text_color_key))
                                    .setSummary(settings.getTextColorString());
                            findPreference(res.getString(R.string.pref_background_color_key))
                                    .setSummary(settings.getBackgroundColorString());
                        }
                    })
                    .show();
        }

        private void updateWidget() {
            Intent intent = new Intent(getActivity(), TinyClockAppWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            Application app = getActivity().getApplication();
            int[] ids = AppWidgetManager.getInstance(app)
                    .getAppWidgetIds(new ComponentName(app, TinyClockAppWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            getActivity().sendBroadcast(intent);
        }

        private void onClickOpenClockApp() {
            Intent clockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            clockIntent.setComponent(clockAppComponentName);

            startActivity(clockIntent);
        }

        private void onNameNotFound(PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Package name not found", e);
        }
    }
}
