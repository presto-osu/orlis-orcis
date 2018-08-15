/**
 * Copyright (C) 2014 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.torch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RemoteViews;

public class WidgetActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // Declaring your view and variables
	private static final String TAG = "WidgetFragment";
    public static final String KEY_WIDGET_SOS = "widget_sos";
    public static final String KEY_WIDGET_COLOR = "widget_color";
	private static WidgetActivity mActivity;
	private TorchWidgetProvider mWidgetProvider;
    private ListPreference mColor;
    private int mAppWidgetId;
	private Context mContext;
    private SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    // No need to go to fragments right now
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mActivity = this;
        mContext = this.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.layout.activity_widget);

        mWidgetProvider = TorchWidgetProvider.getInstance();

        mColor = (ListPreference) findPreference(KEY_WIDGET_COLOR);
        if(mColor.getValue()==null){
            mColor.setValueIndex(0);
        }
        mColor.setSummary(mColor.getValue().toString());
        mColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());
                String mPrefColor = newValue.toString();
                Utils.colorizeBar(mActivity, mContext, mPrefColor);
                return true;
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_WIDGET_SOS)) {
        }
        if (key.equals(KEY_WIDGET_COLOR)) {
        }
    }

	@Override
    public void onPause() {
        updateWidget();
        super.onPause();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

	@Override
    public void onResume() {
		Log.d(TAG, "onResume");
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String mPrefColor = mPreferences.getString(WidgetActivity.KEY_WIDGET_COLOR, getString(R.string.red));
        Utils.setPreferenceTheme(mActivity);
        Utils.colorizeBar(mActivity, mContext, mPrefColor);
    }

    public void updateWidget() {
        this.mWidgetProvider.updateAppWidget(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_widget, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_widget: // Changes are accepted
                addWidget();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addWidget() {
        Editor mEditor = mPreferences.edit();
        mEditor.putBoolean("widget_sos" + mAppWidgetId, mPreferences.getBoolean(KEY_WIDGET_SOS, false));
        mEditor.commit();

        // Initialize widget view for first update
        Context mContext = getApplicationContext();
        RemoteViews mViews = new RemoteViews(mContext.getPackageName(), R.layout.widget);
        mViews.setImageViewResource(R.id.widget, R.drawable.widget_off);
        Intent mLaunchIntent = new Intent();
        mLaunchIntent.setClass(mContext, MainActivity.class);
        mLaunchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        mLaunchIntent.setData(Uri.parse("custom:" + mAppWidgetId + "/0"));
        PendingIntent mPendingIntent = PendingIntent.getBroadcast(mContext, 0, mLaunchIntent, 0);
        mViews.setOnClickPendingIntent(R.id.button, mPendingIntent);

        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        appWidgetManager.updateAppWidget(mAppWidgetId, mViews);

        Intent mResultValue = new Intent();
        mResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, mResultValue);

        // Close the activity
        finish();
    }
}
