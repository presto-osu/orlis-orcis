package net.iexos.musicalarm;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.util.Calendar;

public final class AlarmViewActivity extends Activity {
    private int mHour;
    private int mMin;
    private int mDelayMin;

    private SharedPreferences mSettings;
    public PlaylistManager mPlaylistManager;
    private boolean mAlarmSet = false;

    public final static String PREFERENCES = "AlarmPreferences";
    public final static String PREF_HOUR = "hour";
    public final static String PREF_MIN = "minute";
    public final static String PREF_DELAY = "delayMinute";
    public final static String PREF_SNOOZE = "snoozeMinute";
    public final static String PREF_PLAYLIST_ID = "playlistID";
    public final static String PREF_PLAYLIST_NAME = "playlistName";
    public final static String PREF_RINGTONE_URI = "ringtoneUri";
    public final static String PREF_RINGTONE_NAME = "ringtoneName";
    public final static String PREF_ALARM_SET = "alarmSet";
    public final static String PREF_TRIGGER_TIME = "triggerTime";

    public final static int DEFAULT_HOUR = 8;
    public final static int DEFAULT_MINUTE = 30;
    public final static int DEFAULT_DELAY = 10;
    public final static int DEFAULT_SNOOZE = 10;

    private final static int RINGTONE_PICKER_REQUEST_CODE = 1234;
    private final static String ACTION_UPDATE_STATUS = "net.iexos.musicalarm.update_status";
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE_STATUS)) {
                checkAlarmStatus();
                updateAlarmStatus();
            }
        }
    };

    public static void sendUpdateStatus(Context con) {
        con.sendBroadcast(new Intent(ACTION_UPDATE_STATUS));
    }

    public void onDelayChosen(int delay) {
        mDelayMin = delay;
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(PREF_DELAY, mDelayMin);
        editor.apply();
        updateTimeViews();
        if (mAlarmSet) {
            AlarmUtils.dismissAlarm(this);
            setAlarm();
        }
    }

    public void onTimeChosen(int hour, int minute) {
        mHour = hour;
        mMin = minute;
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(PREF_HOUR, mHour);
        editor.putInt(PREF_MIN, mMin);
        editor.putInt(PREF_DELAY, mDelayMin);
        editor.apply();
        updateTimeViews();
        if (mAlarmSet) {
            AlarmUtils.dismissAlarm(this);
            setAlarm();
        }
    }

    public void onPlaylistChosen() {
        updatePlaylistName();
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putLong(PREF_PLAYLIST_ID, mPlaylistManager.mID);
        editor.putString(PREF_PLAYLIST_NAME, mPlaylistManager.mName);
        editor.apply();
        updateAlarmStatus();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                String name = RingtoneManager.getRingtone(this, uri).getTitle(this);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(PREF_RINGTONE_URI, uri.toString());
                editor.putString(PREF_RINGTONE_NAME, name);
                editor.apply();
                updateRingtoneName();
            }
        }
    }

    public void onSetAlarmButton(View view) {
        if (!mAlarmSet) {
            setAlarm();
        }
        else {
            AlarmUtils.dismissAlarm(this);
            mAlarmSet = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_view);
        mSettings = getSharedPreferences(PREFERENCES, 0);
        mHour = mSettings.getInt(PREF_HOUR, DEFAULT_HOUR);
        mMin = mSettings.getInt(PREF_MIN, DEFAULT_MINUTE);
        mDelayMin = mSettings.getInt(PREF_DELAY, DEFAULT_DELAY);
        long playlistID = mSettings.getLong(PREF_PLAYLIST_ID, 0);
        String playlistName = mSettings.getString(PREF_PLAYLIST_NAME,
                this.getString(R.string.choose_playlist));
        updateTimeViews();
        mPlaylistManager = new PlaylistManager(this, playlistID, playlistName);
        updatePlaylistName();
        updateRingtoneName();
        checkAlarmStatus();
        updateAlarmStatus();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_STATUS);
        registerReceiver(receiver, filter);
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void showTimePickerDialog(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(PREF_HOUR, mHour);
        bundle.putInt(PREF_MIN, mMin);
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public void showDelayPickerDialog(View view) {
        Bundle bundle = new Bundle();
        bundle.putInt(PREF_DELAY, mDelayMin);
        DialogFragment newFragment = new DelayPickerFragment();
        newFragment.setArguments(bundle);
        newFragment.show(getFragmentManager(), "delayPicker");
    }

    public void showPlaylistDialog(View view) {
        new PlaylistDialog().show(getFragmentManager(), "playlistDialog");
    }

    public void showRingtonePicker(View view) {
        Uri uri = Uri.parse(mSettings.getString(PREF_RINGTONE_URI, ""));
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }

    private void setAlarm() {
        if (mPlaylistManager.mID == 0) {
            String errorText = this.getString(R.string.error_playlist);
            Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
            return;
        }
        AlarmUtils.setAlarm(this, mHour, mMin);
        mAlarmSet = true;
    }

    private void checkAlarmStatus() {
        mAlarmSet = AlarmUtils.isAlarmSet(getApplication()) || AlarmService.isRinging();
    }

    public void updateAlarmStatus() {
        ToggleButton button = (ToggleButton) findViewById(R.id.set_alarm);
        if (mPlaylistManager.mID == 0) {
            //TODO also check for valid ringtone
            button.setEnabled(false);
            button.setTextOff(this.getString(R.string.no_valid_playlist));
        }
        else {
            button.setEnabled(true);
            button.setTextOff(this.getString(R.string.alarm_off));
        }
        if (mAlarmSet) button.setChecked(true);
        else button.setChecked(false);
    }

    private void updateTimeViews() {
        TextView musicTimeView = (TextView) findViewById(R.id.music_time);
        TextView alarmTimeView = (TextView) findViewById(R.id.alarm_time);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, mHour);
        cal.set(Calendar.MINUTE, mMin);
        String timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        musicTimeView.setText(timeString);
        cal.add(Calendar.MINUTE, mDelayMin);
        timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(cal.getTime());
        alarmTimeView.setText(timeString);
    }

    protected void updatePlaylistName() {
        TextView view = (TextView) findViewById(R.id.playlist);
        view.setText(mPlaylistManager.mName);
    }

    protected void updateRingtoneName() {
        TextView view = (TextView) findViewById(R.id.ringtone);
        view.setText(mSettings.getString(PREF_RINGTONE_NAME, getString(R.string.choose_ringtone)));
    }

    // debug methods, uncomment buttons in .xml to use
    public void testMusicPlayerButton(View view) {
        AlarmService.sendStateChangeIntent(this, AlarmService.StateChange.START_PLAYBACK);
        mAlarmSet = true;
        updateAlarmStatus();
    }

    public void startRinging(View view) {
        AlarmService.sendStateChangeIntent(this, AlarmService.StateChange.START_RINGING);
    }

    public void stopRinging(View view) {
        AlarmService.sendStateChangeIntent(this, AlarmService.StateChange.STOP_RINGING);
    }

}
