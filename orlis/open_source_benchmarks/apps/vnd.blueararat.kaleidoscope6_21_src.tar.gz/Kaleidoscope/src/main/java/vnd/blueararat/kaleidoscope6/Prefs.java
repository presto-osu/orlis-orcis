package vnd.blueararat.kaleidoscope6;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

public class Prefs extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	static final String KEY_FOLDER = "save_location";
	static final int SELECT_FOLDER = 4;
	private SeekbarPref mSeekbarPrefM;
	private SeekbarPref mSeekbarPrefJ;
	private ListPreference mSaveFormat;
	private SeekbarPref mSeekbarPrefB;
	private CheckBoxPreference mCheckBoxBlur;
	private CheckBoxPreference mCheckBoxCameraInMenu;
	private CheckBoxPreference mCheckBoxHardwareAccel;
	private FolderPref mPrefSaveLocation;
	private String mDefaultSaveLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		mSeekbarPrefM = (SeekbarPref) getPreferenceScreen().findPreference(
				"number_of_mirrors");
		mSeekbarPrefJ = (SeekbarPref) getPreferenceScreen().findPreference(
				"jpeg_quality");
		mSeekbarPrefB = (SeekbarPref) getPreferenceScreen().findPreference(
				"blur_value");
		mCheckBoxBlur = (CheckBoxPreference) getPreferenceScreen()
				.findPreference("blur");
		mCheckBoxCameraInMenu = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(Kaleidoscope.KEY_CAMERA_IN_MENU);
		mCheckBoxHardwareAccel = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(Kaleidoscope.KEY_HARDWARE_ACCEL);
		mSaveFormat = (ListPreference) getPreferenceScreen().findPreference(
				"format");
		mSaveFormat.setSummary(getString(R.string.pictures_will_be_saved) + " "
				+ mSaveFormat.getValue());
		mSeekbarPrefJ.setEnabled(mSaveFormat.getValue().equals("JPEG"));
		mPrefSaveLocation = (FolderPref) getPreferenceScreen().findPreference(
				"save_location");
		mDefaultSaveLocation = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		mPrefSaveLocation.setDefaultValue(mDefaultSaveLocation);
		mPrefSaveLocation
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(getBaseContext(),
								FileDialog.class);
						intent.putExtra(FileDialog.START_PATH, "/sdcard");
						intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
						startActivityForResult(intent, SELECT_FOLDER);
						return true;
					}
				});

		mCheckBoxHardwareAccel.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Toast.makeText(getApplicationContext(),
						R.string.hardware_accel_toast,
						Toast.LENGTH_LONG).show();
				return true;
			}
		});


	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SELECT_FOLDER) {
			if (resultCode == RESULT_OK) {
				String sFolder = data.getStringExtra(FileDialog.RESULT_PATH);
				mPrefSaveLocation.setString(sFolder);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// if (arg1.equals("reset_settings")) {
		//
		// } else if (arg1.equals(KView.KEY_NUMBER_OF_MIRRORS)) {
		//
		// } else if (arg1.equals(Kaleidoscope.KEY_IMAGE_URI)) {
		//
		// } else
		if (arg1.equals("format")) {
			mSeekbarPrefJ.setEnabled(mSaveFormat.getValue().equals("JPEG"));
			mSaveFormat.setSummary(getString(R.string.pictures_will_be_saved)
					+ " " + mSaveFormat.getValue());
		}
	}

	// Save all preferences before exiting.
	// Since the hardware accel switch prompts for restart, it is better UX
	// to have it save automaticalli since they may close the app after toggling
	@Override
	public void onDestroy() {
		super.onDestroy();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		preferences.edit().commit();
	}

	public void onButtonClicked(View v) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		// edit.putInt(Kaleidoscope.KEY_NUMBER_OF_MIRRORS, 4);
		edit.putString(Kaleidoscope.KEY_IMAGE_URI, "");
		edit.commit();
		mSeekbarPrefM.setProgressValue(4);
		mSeekbarPrefJ.setProgressValue(40);
		mSaveFormat.setValue(getString(R.string.default_save_format));
		mSeekbarPrefB.setProgressValue(49);
		mCheckBoxBlur.setChecked(true);
		mCheckBoxCameraInMenu.setChecked(true);
		mCheckBoxHardwareAccel.setChecked(true);
		mPrefSaveLocation.reset();
	}

	// @Override
	// public void onBackPressed() {
	// finish();
	// }

	// @Override
	// public void onProgressChanged(SeekBar seekBar, int progress,
	// boolean fromUser) {
	// // TODO Auto-generated method stub
	// mTextView.setText(progress);
	// //Toast.makeText(this, "reset_settings", Toast.LENGTH_LONG).show();
	//
	// }
	//
	// @Override
	// public void onStartTrackingTouch(SeekBar seekBar) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onStopTrackingTouch(SeekBar seekBar) {
	// // TODO Auto-generated method stub
	// }
}
