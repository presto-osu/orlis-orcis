package net.iexos.musicalarm;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

public final class AlarmService extends Service {

    public static final String WAKE_LOCK_TAG = "net.iexos.musicalarm.WAKE_LOCK";
    public static final String LOGGING_TAG = "AlarmService";
    public static final String EXTRA_STATE_CHANGE = "net.iexos.musicalarm.EXTRA_STATE_CHANGE";
    public static final int NOTIFICATION_ID = 31415;

    private PowerManager.WakeLock mWakeLock;
    private MediaPlayer mAlarmPlayer;
    private MusicPlayer mMusicPlayer;
    private Uri mRingtoneUri;
    private int mDelay;
    private int mSnooze;
    private boolean mIsPlaying = false;
    private static boolean mAlarmWaiting = false;
    public enum State {INIT, IDLE, RINGING, IN_CALL}
    private static State mState = State.INIT;
    public enum StateChange {
        START_PLAYBACK, STOP_PLAYBACK, START_RINGING, STOP_RINGING, STOP_ALL, SNOOZE,
        START_CALL, STOP_CALL
    }
    public AlarmService() {
    }

    public static boolean isRinging() {
        return (mAlarmWaiting || mState == State.RINGING);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                sendStateChangeIntent(context, StateChange.STOP_PLAYBACK);
            }
            if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    sendStateChangeIntent(context, StateChange.STOP_CALL);
                }
                else {
                    sendStateChangeIntent(context, StateChange.START_CALL);
                }
            }
        }
    };

    private static Intent getStateChangeIntent(Context con, StateChange change) {
        Intent intent = new Intent(con, AlarmService.class);
        intent.putExtra(EXTRA_STATE_CHANGE, change);
        return intent;
    }

    public static PendingIntent getPendingStateChangeIntent(Context con, StateChange change) {
        return PendingIntent.getService(con, change.hashCode(),
                getStateChangeIntent(con, change), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void sendStateChangeIntent(Context con, StateChange change) {
        con.startService(getStateChangeIntent(con, change));
    }

    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire();

        SharedPreferences settings = getSharedPreferences(AlarmViewActivity.PREFERENCES, 0);
        long playlistID = settings.getLong(AlarmViewActivity.PREF_PLAYLIST_ID, 0);
        String playlistName = settings.getString(AlarmViewActivity.PREF_PLAYLIST_NAME,
                this.getString(R.string.choose_playlist));
        mRingtoneUri = Uri.parse(settings.getString(AlarmViewActivity.PREF_RINGTONE_URI, ""));
        mDelay = settings.getInt(AlarmViewActivity.PREF_DELAY,
                AlarmViewActivity.DEFAULT_DELAY);
        mSnooze = settings.getInt(AlarmViewActivity.PREF_SNOOZE,
                AlarmViewActivity.DEFAULT_SNOOZE);

        mMusicPlayer = new MusicPlayer(this, playlistID, playlistName);

        mAlarmPlayer = new MediaPlayer();

        TelephonyManager tMan = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (tMan.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
            mState = State.IN_CALL;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(receiver, filter);

        Log.v(LOGGING_TAG, "AlarmService started");
    }

    @Override
    public void onDestroy() {
        Log.v(LOGGING_TAG, "AlarmService stopped");
        mAlarmWaiting = false;
        mState = State.INIT;
        mMusicPlayer.stop();
        mAlarmPlayer.release();
        mWakeLock.release();
        unregisterReceiver(receiver);
    }

    private void startPlayback() {
        switch (mState) {
            case INIT:
                mState = State.IDLE;
                mMusicPlayer.resume();
                break;
            case IDLE:
                mMusicPlayer.resume();
                break;
            case RINGING: case IN_CALL:
                break;
        }
        mIsPlaying = true;
    }

    private void stopPlayback() {
        mMusicPlayer.stop();
        mIsPlaying = false;
    }

    private void startRinging() {
        switch (mState) {
            case IN_CALL:
                mAlarmWaiting = true;
            case RINGING:
                return;
        }
        mState = State.RINGING;
        if (mIsPlaying) mMusicPlayer.pause();
        mAlarmPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mAlarmPlayer.setLooping(true);
        try {
            mAlarmPlayer.setDataSource(this, mRingtoneUri);
            mAlarmPlayer.prepare();
            mAlarmPlayer.start();
        }
        catch (IOException e1) {
            Log.e(LOGGING_TAG, e1.toString());
            Log.i(LOGGING_TAG, "Using fallback ringtone");
            try {
                mAlarmPlayer.reset();
                mAlarmPlayer.setDataSource(this,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                mAlarmPlayer.prepare();
                mAlarmPlayer.start();
            }
            catch (IOException e2) {
                Log.e(LOGGING_TAG, "fallback failed: " + e2.toString());
            }
        }
    }

    private void stopRinging() {
        mAlarmWaiting = false;
        AlarmUtils.dismissRingAlarm(this);
        AlarmViewActivity.sendUpdateStatus(this);
        if (mState != State.RINGING) return;
        if (mIsPlaying) mMusicPlayer.resume();
        mState = State.IDLE;
        mAlarmPlayer.reset();
    }

    private void callStarted() {
        if (mIsPlaying) mMusicPlayer.pause();
        if (mState == State.RINGING) {
            mAlarmPlayer.reset();
            mAlarmWaiting = true;
        }
        mState = State.IN_CALL;
    }

    private void callStopped() {
        if (mState != State.IN_CALL) return;
        mState = State.IDLE;
        if (mAlarmWaiting) {
            mAlarmWaiting = false;
            startRinging();
        }
        else if (mIsPlaying) {
            mMusicPlayer.resume();
        }
    }

    private void checkErrorStates(StateChange change) {
        if ((!mIsPlaying && mState == State.IDLE) || (mIsPlaying && mState == State.INIT)) {
            Log.e(LOGGING_TAG, "This should not happen: State: " + mState.toString()
                    + " IsPlaying: " + String.valueOf(mIsPlaying)
                    + " StateChange: " + change.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        StateChange change = (StateChange) intent.getSerializableExtra(EXTRA_STATE_CHANGE);
        if (change != null) {
            checkErrorStates(change);
            switch (change) {
                case START_PLAYBACK:
                    if (mState == State.INIT) AlarmUtils.setRingAlarm(this, mDelay);
                    startPlayback();
                    break;
                case STOP_PLAYBACK:
                    stopPlayback();
                    break;
                case START_RINGING:
                    startRinging();
                    break;
                case STOP_RINGING:
                    stopRinging();
                    break;
                case START_CALL:
                    callStarted();
                    break;
                case STOP_CALL:
                    callStopped();
                    break;
                case STOP_ALL:
                    stopPlayback();
                    stopRinging();
                    break;
                case SNOOZE:
                    stopRinging();
                    AlarmUtils.setRingAlarm(this, mSnooze);
                    break;
            }

            if (mState == State.INIT
                    || (mState == State.IDLE && !mIsPlaying)
                    || (mState == State.IN_CALL && !mAlarmWaiting && !mIsPlaying)) {
                stopForeground(true);
                NotificationUtils.displayStoppedNotification(this);
                stopSelf();
            }
            else {
                NotificationUtils.displayForegroundNotification(this, mState);
            }
        }
        return Service.START_NOT_STICKY;
    }
}
