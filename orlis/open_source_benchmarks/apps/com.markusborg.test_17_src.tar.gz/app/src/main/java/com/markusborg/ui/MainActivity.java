package com.markusborg.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.markusborg.logic.LogHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity that displays the initial settings dialog.
 *
 * @author  Markus Borg
 * @since   2015-07-29
 */
public class MainActivity extends AppCompatActivity {

    private Context mAppContext;
    private Spinner mSpinner;
    private SeekBar mSeekBarSets, mSeekBarReps, mSeekBarInterval, mSeekBarBreak;
    private EditText mTxtSets, mTxtReps, mTxtInterval, mTxtBreak, mTxtHistory;
    private CheckBox mChk6Points, mChkAudio;
    private LogHandler mLogger;
    private SharedPreferences mSharedPrefs;
    private AudioManager mAudioManager;
    private SoundPool mSoundPool;
    private int[] mSoundIDs; // two sounds, squash and badminton
    private boolean mLoaded;

    private static final String PREFERENCES = "GhostingPrefs";
    public static final String ISSQUASH = "IS_SQUASH";
    public static final String SETS = "NBR_SETS";
    public static final String REPS = "NBR_REPS";
    public static final String INTERVAL = "TIME_INTERVAL";
    public static final String BREAK = "TIME_BREAK";
    public static final String IS6POINTS = "IS_6POINTS";
    public static final String ISAUDIO = "IS_AUDIO";
    public final static String FILENAME = "history.log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppContext = getApplicationContext();
        setGUIComponents();
        initSounds();

        // Load previous setting
        loadSharedPrefs();

        mLogger = new LogHandler(mAppContext);
        displayHistory();


        final Button btnGo = (Button) findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent ghostingIntent = new Intent(mAppContext, GhostingActivity.class);

                boolean squashMode = mSpinner.getSelectedItemPosition() == 0; // squash is the first
                int sets = mSeekBarSets.getProgress() + 1; // add one, due to slider starts at 1
                int reps = mSeekBarReps.getProgress() + 1; // add one, due to slider starts at 1
                int interval = (mSeekBarInterval.getProgress() + 10) * 100; // from ds to ms
                int breakTime = mSeekBarBreak.getProgress();
                boolean is6Points = mChk6Points.isChecked();
                boolean isAudio = mChkAudio.isChecked();

                ghostingIntent.putExtra(ISSQUASH, squashMode);
                ghostingIntent.putExtra(SETS, sets);
                ghostingIntent.putExtra(REPS, reps);
                ghostingIntent.putExtra(INTERVAL, interval);
                ghostingIntent.putExtra(BREAK, breakTime);
                ghostingIntent.putExtra(IS6POINTS, is6Points);
                ghostingIntent.putExtra(ISAUDIO, isAudio);

                // Save the settings as SharedPreferences
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putBoolean(ISSQUASH, squashMode);
                editor.putInt(SETS, sets);
                editor.putInt(REPS, reps);
                editor.putInt(INTERVAL, interval);
                editor.putInt(BREAK, breakTime);
                editor.putBoolean(IS6POINTS, is6Points);
                editor.putBoolean(ISAUDIO, isAudio);
                editor.apply();

                // Play sound corresponding to selected sport
                if (mLoaded) {
                    if (squashMode) {
                        mSoundPool.play(mSoundIDs[0], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                    else {
                        mSoundPool.play(mSoundIDs[1], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }

                startActivity(ghostingIntent);
            }
        });
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

        switch (id) {
            case R.id.action_help:
                HelpBox.Show(MainActivity.this);
                break;
            case R.id.action_about:
                AboutBox.Show(MainActivity.this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayHistory();
    }

    /**
     * Set all GUI components.
     */
    private void setGUIComponents() {
        mTxtSets = (EditText) findViewById(R.id.txtSets);
        mTxtReps = (EditText) findViewById(R.id.txtReps);
        mTxtInterval = (EditText) findViewById(R.id.txtInterval);
        mTxtBreak = (EditText) findViewById(R.id.txtBreak);
        mChk6Points = (CheckBox) findViewById(R.id.chk6Point);
        mChkAudio = (CheckBox) findViewById(R.id.chkAudio);
        mTxtHistory = (EditText) findViewById(R.id.txtHistory);

        // Squash/Badminton spinner. No action listener needed.
        mSpinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add("Squash");
        list.add("Badminton");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(dataAdapter);

        // SeekBars
        mSeekBarSets = (SeekBar) findViewById(R.id.seekBarSets);
        mSeekBarSets.setOnSeekBarChangeListener(new SeekBarListener());
        mSeekBarReps = (SeekBar) findViewById(R.id.seekBarReps);
        mSeekBarReps.setOnSeekBarChangeListener(new SeekBarListener());
        mSeekBarInterval = (SeekBar) findViewById(R.id.seekBarInterval);
        mSeekBarInterval.setOnSeekBarChangeListener(new SeekBarListener());
        mSeekBarBreak = (SeekBar) findViewById(R.id.seekBarBreak);
        mSeekBarBreak.setOnSeekBarChangeListener(new SeekBarListener());
    }

    /**
     * Load shared preferences containing the previous setting. If none are available, return false.
     *u
     * @return True if successful, otherwise false.
     */
    private boolean loadSharedPrefs() {
        mSharedPrefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        int prevSets = mSharedPrefs.getInt(SETS, -1);
        if (prevSets != -1) {
            if (mSharedPrefs.getBoolean(ISSQUASH, true)) {
                mSpinner.setSelection(0);
            } else {
                mSpinner.setSelection(1);
            }
            // set all SeekBars, adjusted to take care of non-zero min values
            mTxtSets.setText("Sets: " + prevSets);
            mSeekBarSets.setProgress(prevSets-1);
            int prevReps = mSharedPrefs.getInt(REPS, 15);
            mTxtReps.setText("Reps: " + prevReps);
            mSeekBarReps.setProgress(prevReps-1);
            int prevInt = mSharedPrefs.getInt(INTERVAL, 50);
            mTxtInterval.setText("Interval (s): " + prevInt);
            mSeekBarInterval.setProgress((prevInt / 100) - 10); // from ms to ds
            int prevBreak = mSharedPrefs.getInt(BREAK, 15);
            mTxtBreak.setText("Break btw. sets (s): " + prevBreak);
            mSeekBarBreak.setProgress(prevBreak);
            mChk6Points.setChecked(mSharedPrefs.getBoolean(IS6POINTS, true));
            mChkAudio.setChecked(mSharedPrefs.getBoolean(ISAUDIO, true));
        }
        return prevSets != -1;
    }

    /**
     * Display the three latest ghosting sessions.
     */
    private void displayHistory() {
        mTxtHistory.setText("Recent history:\n" + mLogger.getFromLog(3));
    }

    private void initSounds() {
        mSoundIDs = new int[6];
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

        // Take care of the deprecated SoundPool constructor
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }

        // Load the actual sounds
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });
        mSoundIDs[0] = mSoundPool.load(this, R.raw.squash, 1);
        mSoundIDs[1] = mSoundPool.load(this, R.raw.badminton, 1);
    }

    /**
     * Create a new SoundPool from Lollipop and later Android versions
     */
    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    /**
     * Create a new SoundPool using the deprecated constructor for Android versions before Lollipop.
     */
    @SuppressWarnings("deprecation")
    protected void createOldSoundPool(){
        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            TextView tmp = null;
            switch (seekBar.getId()) {
                case R.id.seekBarSets:
                    tmp = (TextView) findViewById(R.id.txtSets);
                    tmp.setText("Sets: " + (seekBar.getProgress() + 1));
                    break;
                case R.id.seekBarReps:
                    tmp = (TextView) findViewById(R.id.txtReps);
                    tmp.setText("Reps: " + (seekBar.getProgress() + 1));
                    break;
                case R.id.seekBarInterval:
                    tmp = (TextView) findViewById(R.id.txtInterval);
                    tmp.setText("Interval (s): " + (((float) seekBar.getProgress() / 10.0) + 1));
                    break;
                case R.id.seekBarBreak:
                    tmp = (TextView) findViewById(R.id.txtBreak);
                    tmp.setText("Break btw. sets (s): " + seekBar.getProgress());
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
