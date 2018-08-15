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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    // Declaring your view and variables
    private static final String TAG = "SettingsFragment";
    public static final String KEY_SOS = "sos";
    public static final String KEY_SCREEN = "screen";
    public static final String KEY_COLOR = "color";
    public static final String KEY_HIDE = "hide";
	private static SettingsActivity mActivity;
    private TorchWidgetProvider mWidgetProvider;
    private static Boolean mPrefBright;
    private CheckBoxPreference mSos;
    private CheckBoxPreference mScreen;
    private ListPreference mColor;
    private Preference mHide;
	private Context mContext;
    private SharedPreferences mPreferences;

    @SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		mActivity = this;
        mContext = this.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        addPreferencesFromResource(R.layout.activity_settings);

        mWidgetProvider = TorchWidgetProvider.getInstance();

		mSos = (CheckBoxPreference) findPreference(KEY_SOS);
        mScreen = (CheckBoxPreference) findPreference(KEY_SCREEN);

        mColor = (ListPreference) findPreference(KEY_COLOR);
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

        mHide = findPreference(KEY_HIDE);
        mHide.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(SettingsActivity.this);

                mAlertDialog.setTitle(getString(R.string.title));
                mAlertDialog.setMessage(getString(R.string.message));
                mAlertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPackageManager().setComponentEnabledSetting(new ComponentName("com.doomy.torch", "com.doomy.torch.LaunchActivity"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                        dialog.dismiss();
                    }
                });
                mAlertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getPackageManager().setComponentEnabledSetting(new ComponentName("com.doomy.torch", "com.doomy.torch.LaunchActivity"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
						dialog.dismiss();
                        MainActivity.getInstance().finish();
                        finish();
                    }
                });
                mAlertDialog.show();
                return false;
            }
        });

        enablementKeys();

        deviceHasNoFlash();

        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_SOS)) {
            enablementSOS();
        }
        if (key.equals(KEY_SCREEN)) {
            mPrefBright = mPreferences.getBoolean("mPrefBright", false);
            if(mPrefBright) {
                mPrefBright = mPreferences.edit().putBoolean("mPrefBright", false).commit();
                Toast.makeText(mContext, getString(R.string.discolored), Toast.LENGTH_SHORT).show();
            }
            enablementScreen();
            updateWidget();
        }
        if (key.equals(KEY_COLOR)) {
            mPrefBright = mPreferences.getBoolean("mPrefBright", false);
            if(mScreen.isChecked()) {
                AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(SettingsActivity.this);

                mAlertDialog.setTitle(getString(R.string.mode));
                mAlertDialog.setMessage(getString(R.string.apply));
                mAlertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mPrefBright) {
                            mPrefBright = mPreferences.edit().putBoolean("mPrefBright", false).commit();
                            Toast.makeText(mContext, getString(R.string.discolored), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                mAlertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPrefBright = mPreferences.edit().putBoolean("mPrefBright", true).commit();
                        Toast.makeText(mContext, getString(R.string.colorful), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                mAlertDialog.show();
            }
        }
        if (key.equals(KEY_HIDE)) {
        }
    }

    private void enablementSOS() {
        mScreen.setEnabled(!mPreferences.getBoolean(KEY_SOS, false));
    }

    private void enablementScreen() {
        mSos.setEnabled(!mPreferences.getBoolean(KEY_SCREEN, false));
    }

    private void enablementKeys() {
        if(mSos.isChecked()) {
            enablementSOS();
        }
        if(mScreen.isChecked()) {
            enablementScreen();
        }
    }

    private void deviceHasNoFlash() {
        Boolean mPrefDevice = mPreferences.getBoolean("mPrefDevice", false);
        if (!mPrefDevice) {
            mScreen.setEnabled(false);
            mScreen.setChecked(true);
            mSos.setEnabled(false);
            mColor.setEnabled(true);
            mHide.setEnabled(false);
        }
    }
	
	@Override
    public void onPause() {
        updateWidget();
        super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        String mPrefColor = mPreferences.getString(KEY_COLOR, getString(R.string.red));
        Utils.setPreferenceTheme(mActivity);
        Utils.colorizeBar(mActivity, mContext, mPrefColor);
    }

    public void updateWidget() {
        this.mWidgetProvider.updateAppWidget(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml.
         */
        int id = item.getItemId();

        // NoInspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            openAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	// Create AlertDialog for the about view
    private void openAboutDialog() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        View mView = mLayoutInflater.inflate(R.layout.view_about, null);

        ImageView mImageViewMrDoomy = (ImageView) mView.findViewById(R.id.imageViewMrDoomy);
        ImageView mImageViewStudio = (ImageView) mView.findViewById(R.id.imageViewStudio);
        ImageView mImageViewGitHub = (ImageView) mView.findViewById(R.id.imageViewGitHub);
        Drawable mMrDoomy = mImageViewMrDoomy.getDrawable();
        Drawable mStudio = mImageViewStudio.getDrawable();
        Drawable mGitHub = mImageViewGitHub.getDrawable();
        mMrDoomy.setColorFilter(getResources().getColor(R.color.redDark), PorterDuff.Mode.SRC_ATOP);
        mStudio.setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        mGitHub.setColorFilter(getResources().getColor(R.color.greyMaterialDark), PorterDuff.Mode.SRC_ATOP);

        mImageViewGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                mIntent.setData(Uri.parse(getString(R.string.url)));
                startActivity(mIntent);
            }
        });

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(SettingsActivity.this, setThemeDialog());

        mAlertDialog.setTitle(getString(R.string.about));
        mAlertDialog.setView(mView);
        mAlertDialog.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog.show();
    }

    private int setThemeDialog() {

        int mTheme;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTheme = R.style.MaterialDialog;
        } else {
            mTheme = R.style.HoloDialog;
        }
        return mTheme;
    }
}
