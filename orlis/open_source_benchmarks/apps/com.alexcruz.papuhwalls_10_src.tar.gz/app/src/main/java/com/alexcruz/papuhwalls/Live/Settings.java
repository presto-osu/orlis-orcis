package com.alexcruz.papuhwalls.Live;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alexcruz.papuhwalls.MainActivity;
import com.alexcruz.papuhwalls.Preferences;
import com.alexcruz.papuhwalls.R;
import com.jenzz.materialpreference.SwitchPreferenceThemeable;

import java.io.File;
import java.util.Arrays;

/**
 * Created by Daniel Huber on 16.12.2015.
 */
public class Settings extends PreferenceActivity {

    private static final String keyUpdateInterval = "LWinterval";
    private static final String keyMyWalls = "MyWalls";
    private static final String keyTripleTapToJump = "tripleTapToJump";
    private static final String keyApplyWall = "ApplyWall";
    private static final String keyHowTo = "HowTo";

    private com.jenzz.materialpreference.Preference mywalls;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.live_wallpaper_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final Preferences preferences = new Preferences(Settings.this);

        Toolbar toolbar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
            root.addView(toolbar, 0);

            toolbar.setTitle(getResources().getString(R.string.live_wallpaper_settings));
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            View content = root.getChildAt(0);

            root.removeAllViews();

            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);

            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            } else {
                height = toolbar.getHeight();
            }

            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(toolbar);
            toolbar.setTitle(getResources().getString(R.string.live_wallpaper_settings));
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });

        com.alexcruz.papuhwalls.Preferences.themeMe(this, toolbar);

        final ListPreference updateInterval = (ListPreference)findPreference(keyUpdateInterval);
        mywalls = (com.jenzz.materialpreference.Preference)findPreference(keyMyWalls);
        final SwitchPreference triple_tap_to_jump = (SwitchPreference)findPreference(keyTripleTapToJump);
        final com.jenzz.materialpreference.Preference applyWall = (com.jenzz.materialpreference.Preference)findPreference(keyApplyWall);
        final com.jenzz.materialpreference.Preference howTo = (com.jenzz.materialpreference.Preference)findPreference(keyHowTo);

        if(updateInterval != null){
            String minutes = getResources().getString(R.string.interval_minutes);
            String minute = getResources().getString(R.string.interval_minute);
            String hours = getResources().getString(R.string.interval_hours);
            String hour = getResources().getString(R.string.interval_hour);

            final CharSequence[] entries = new CharSequence[7];
            entries[0] = "1 " + minute;
            entries[1] = "5 " + minutes;
            entries[2] = "30 " + minutes;
            entries[3] = "1 " + hour;
            entries[4] = "2 " + hours;
            entries[5] = "12 " + hours;
            entries[6] = "24 " + hours;

            final CharSequence[] entryValues = new CharSequence[7];
            entryValues[0] = "60";
            entryValues[1] = "300";
            entryValues[2] = "1800";
            entryValues[3] = "3600";
            entryValues[4] = "7200";
            entryValues[5] = "43200";
            entryValues[6] = "86400";

            updateInterval.setEntries(entries);
            updateInterval.setEntryValues(entryValues);

            String entryValue = String.valueOf(preferences.LWinterval());
            CharSequence entry = entries[Arrays.asList(entryValues).indexOf(entryValue)];
            updateInterval.setSummary(entry);
            updateInterval.setValue(entryValue);

            updateInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    String selectedValue = newValue.toString();
                    CharSequence displayValue = entries[Arrays.asList(entryValues).indexOf(selectedValue)];

                    preferences.setLWUpdateInterval(Integer.parseInt(selectedValue));

                    updateInterval.setSummary(displayValue);

                    Intent updateLWService = new Intent(LiveWallpaperService.settingsChangedAction);
                    sendBroadcast(updateLWService);

                    return true;
                }
            });
        }

        if(mywalls!= null){
            mywalls.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent toMyWallsActivity = new Intent(Settings.this, MyWallsManager.class);
                    startActivity(toMyWallsActivity);
                    return true;
                }
            });

            updateLWcount();
        }

        if(triple_tap_to_jump != null){

            boolean tripleTap = preferences.isTripleTapToJump();
            triple_tap_to_jump.setChecked(tripleTap);

            triple_tap_to_jump.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean activated = Boolean.parseBoolean(String.valueOf(newValue));
                        preferences.setLWtripleTapToJumpActivated(activated);
                    return true;
                }
            });
        }

        if(applyWall != null)
            applyWall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try{
                        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                new ComponentName(Settings.this, com.alexcruz.papuhwalls.Live.LiveWallpaperService.class));
                        startActivity(intent);
                    }
                    catch(Exception e){
                        Intent intent = new Intent();
                        intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
                        startActivity(intent);
                    }
                    return true;
                }
            });

        if(howTo != null){
            howTo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    MaterialDialog dialog = new MaterialDialog.Builder(Settings.this)
                            .content(getString(R.string.how_to_lw) + " \"" + getString(R.string.addToLiveWallList) + "\"")
                            .title(R.string.how_to_lw_title)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    Intent toPapuhMain = new Intent(Settings.this, MainActivity.class);
                                    startActivity(toPapuhMain);
                                }
                            })
                            .neutralText(R.string.ok).build();
                    dialog.setActionButton(DialogAction.POSITIVE, R.string.how_to_try);
                    dialog.setActionButton(DialogAction.NEUTRAL, R.string.ok);
                    dialog.show();

                    return true;
                }
            });

        }
    }

    private void updateLWcount(){
        File saveWallLoc = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.walls_save_location));
        int count = 0;
        File file[] = saveWallLoc.listFiles();
        if(file!= null) {
            for (File wall : file) {
                if (wall.getName().startsWith("PapuhLive")) {
                    count++;
                }
            }
        }

        mywalls.setSummary(String.valueOf(count) + " " + getString(R.string.walls_count));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLWcount();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    public void finishActivity() {
        finish();
        final Intent intent = IntentCompat.makeMainActivity(new ComponentName(
                Settings.this, MainActivity.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
