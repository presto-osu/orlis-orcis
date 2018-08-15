package com.alexcruz.papuhwalls;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.IntentCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity {
    Toolbar toolbar;

    Preferences Preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.Preferences = new Preferences(getApplicationContext());

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.app_bar, root, false);
            root.addView(toolbar, 0);

            toolbar.setTitle(getResources().getString(R.string.settings));
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
            toolbar.setTitle(getResources().getString(R.string.settings));

        }


        com.alexcruz.papuhwalls.Preferences.themeMe(this, toolbar);

        View resetDataButton = toolbar.findViewById(R.id.resetData);

        resetDataButton.setVisibility(View.VISIBLE);
        resetDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.alexcruz.papuhwalls.Preferences.resetPrefs(SettingsActivity.this);
                recreate();
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });


        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String id) {
                com.alexcruz.papuhwalls.Preferences.themeMe(SettingsActivity.this, toolbar);
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    public void finishActivity() {
        finish();
        final Intent intent = IntentCompat.makeMainActivity(new ComponentName(
                SettingsActivity.this, MainActivity.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
