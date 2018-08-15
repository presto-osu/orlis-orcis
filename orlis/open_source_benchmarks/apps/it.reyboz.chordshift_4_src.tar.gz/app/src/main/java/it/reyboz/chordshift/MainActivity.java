/*
 * Chord Shift - Simple multiline editor for shifting your chords.
 * Copyright (C) 2015  Valerio Bozzolan and contributors
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

package it.reyboz.chordshift;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private EditText chordsEditText;
	private SharedPreferences prefs;

	private final static String FIRST_RUN = "firstRun";
	private final static String NAMING_CONVENTION = "namingConvention";
	private final static String LAST_VERSION_CODE = "lastVersionCode";

	private final static int DEFAULT_NAMING_CONVENTION = ChordShift.NAMING_CONVENTION_ENGLISH;
	private final static boolean ALWAYS_SHOW_LAST_CHANGES = false; // Debug purpuse :^)

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

		chordsEditText = (EditText) findViewById(R.id.chordsEditText);
		chordsEditText.setText(getDefaultChords(prefs.getInt(NAMING_CONVENTION, DEFAULT_NAMING_CONVENTION))); // Set a default song

		chordsEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		// Find the version code
		int versionCode = -1;
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();  // TMKH - How the f**k can be NOT FOUND?!? D:
		}

		if (prefs.getBoolean(FIRST_RUN, true)) {

			// Is the first run
			showAboutDialog();
			askNamingConvention();

			prefs.edit().putBoolean(FIRST_RUN, false).commit();
			prefs.edit().putInt(LAST_VERSION_CODE, versionCode).commit();
		} else if(ALWAYS_SHOW_LAST_CHANGES || versionCode != prefs.getInt(LAST_VERSION_CODE, -1)) {

			// The user upgraded the app
			showChangesDialog();

			prefs.edit().putInt(LAST_VERSION_CODE, versionCode).commit();
		}

		chordsEditText.selectAll();
		chordsEditText.requestFocus();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch(id) {
			case R.id.action_choose_naming_convention:
				askNamingConvention();
				break;
			case R.id.action_select_all:
				chordsEditText.selectAll();
				chordsEditText.requestFocus();
				break;
			case R.id.action_about:
				showAboutDialog();
				break;
			case R.id.action_view_source_code:
				openIceweasel("https://code.launchpad.net/android-chord-shift");
				break;
			case R.id.action_view_author_website:
				openIceweasel("http://boz.reyboz.it");
				break;
			case R.id.action_view_changelog:
				showChangesDialog();
				break;
			case R.id.action_view_license:
				openIceweasel("http://www.gnu.org/licenses/gpl-3.0.html");
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onShiftChordsLessButtonClicked(View v) {
		rotate(11); // -1
	}

	public void onShiftChordsMoreButtonClicked(View v) {
		rotate(1); // +1
	}

	/**
	 * Show app info.
	 */
	public void showAboutDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(getText(R.string.action_about));
		alertDialog.setMessage(getText(R.string.about_content));
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}

	/**
	 * Show added functionalities.
	 */
	public void showChangesDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
		alertDialog.setTitle(getText(R.string.changes_title));
		alertDialog.setMessage(getText(R.string.changes_content));
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(android.R.string.ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}

	/**
	 * Open an URL in the default browser.
	 * @param url URL
	 */
	public void openIceweasel(String url) {
		Intent browserIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent1);
	}

	/**
	 * Get the default chords.
	 * @param namingConvention A naming convention like ChordShift.ENGLISH
	 * @return Localized default chords
	 */
	public String getDefaultChords(int namingConvention) {
		switch(namingConvention) {
			case ChordShift.NAMING_CONVENTION_ENGLISH:
				return getString(R.string.default_chords_english);
			case ChordShift.NAMING_CONVENTION_NEOLATIN:
				return getString(R.string.default_chords_neolatin);
			default:
				Log.e("MainActivity", "Naming convention unknown!");
				return getDefaultChords(DEFAULT_NAMING_CONVENTION); // So.. don't make that mistake! :P
		}
	}

	/**
	 * Ask for a naming convention.
	 * It also converts the old naming convention to the other.
	 */
	public void askNamingConvention() {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int startNamingConvention = prefs.getInt(NAMING_CONVENTION, DEFAULT_NAMING_CONVENTION);
				int namingConvention;
				switch (which){
					case DialogInterface.BUTTON_POSITIVE:
						namingConvention = ChordShift.NAMING_CONVENTION_NEOLATIN;
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						namingConvention = ChordShift.NAMING_CONVENTION_ENGLISH;
						break;
					default:
						namingConvention = DEFAULT_NAMING_CONVENTION;
				}
				prefs.edit().putInt(NAMING_CONVENTION, namingConvention).commit();

				convert(startNamingConvention, namingConvention);
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getText(R.string.convention_ask)).setPositiveButton(getString(R.string.convention_neolatin), dialogClickListener)
				.setNegativeButton(getString(R.string.convention_english), dialogClickListener).show();
	}

	/**
	 * Rotate the semitones.
	 * @param rotateFactor semitones (0-12)
	 */
	public void rotate(int rotateFactor) {
		int namingConvention = prefs.getInt(NAMING_CONVENTION, DEFAULT_NAMING_CONVENTION);
		try {
			String chords = chordsEditText.getText().toString();
			ChordShift chordShift = new ChordShift(namingConvention);
			chords = chordShift.getRotated(chords, rotateFactor);
			chordsEditText.setText(chords);
		} catch (ChordShift.RotationException e) {
			Toast.makeText(getApplicationContext(),
					String.format(getString(R.string.error_shifting_chords), e.getWrongNote(), e.getWrongLine()),
					Toast.LENGTH_SHORT).show();
			chordsEditText.setSelection(e.getWrongStartChr(), e.getWrongEndChr());
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * Converts the naming convention.
	 * @param namingConventionStart Initial naming convention
	 * @param namingConventionEnd Final naming convention
	 */
	public void convert(int namingConventionStart, int namingConventionEnd) {
		try {
			String chords = chordsEditText.getText().toString();
			ChordShift chordShift = new ChordShift(namingConventionStart);
			chords = chordShift.getConverted(chords, namingConventionEnd);
			chordsEditText.setText(chords);
		} catch (ChordShift.RotationException e) {
			Toast.makeText(getApplicationContext(),
					String.format(getString(R.string.error_shifting_chords), e.getWrongNote(), e.getWrongLine()),
					Toast.LENGTH_SHORT).show();
			chordsEditText.setSelection(e.getWrongStartChr(), e.getWrongEndChr());
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
}