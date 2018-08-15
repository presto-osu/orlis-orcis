package net.iexos.musicalarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public final class MusicPlayer implements AudioManager.OnAudioFocusChangeListener{
    public static final String LOGGING_TAG = "MusicPlayer";
    private MediaPlayer mMediaPlayer = null;
    private AudioManager mAudioManager = null;
    private enum MusicState {PLAYING, STOPPED, PAUSED}
    private MusicState mMusicState = MusicState.STOPPED;
    private Iterator<Uri> mSongIter;
    private String mPlaylistName;
    private long mPlaylistID;
    private Context mCon;

    public MusicPlayer(Context con, long id, String name) {
        mCon = con;
        mPlaylistID = id;
        mPlaylistName = name;
    }

    private boolean initMediaPlayer() {
        mAudioManager = (AudioManager) mCon.getSystemService(Context.AUDIO_SERVICE);
        int focusGranted = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (focusGranted != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false;
        }
        PlaylistManager playlistManager = new PlaylistManager(mCon, mPlaylistID, mPlaylistName);
        mMediaPlayer = new MediaPlayer();
        List<Uri> songUris = playlistManager.getSongs();
        mSongIter = songUris.iterator();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mMusicState = MusicState.STOPPED;
                resume();
            }
        });
        return true;
    }

    public void pause() {
        if (mMusicState == MusicState.PLAYING) {
            mMediaPlayer.pause();
            mMusicState = MusicState.PAUSED;
        }
    }

    public void resume() {
        if (mMediaPlayer == null) {
            if (!initMediaPlayer()) {
                AlarmService.sendStateChangeIntent(mCon, AlarmService.StateChange.STOP_PLAYBACK);
            }
        }
        switch (mMusicState) {
            case PLAYING:
                break;
            case PAUSED:
                mMediaPlayer.start();
                mMusicState = MusicState.PLAYING;
                break;
            case STOPPED:
                if (mSongIter.hasNext()) {
                    try {
                        mMediaPlayer.reset();
                        Uri nextUri = mSongIter.next();
                        Log.v(LOGGING_TAG, "Playing: " + nextUri.toString());
                        mMediaPlayer.setDataSource(mCon, nextUri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                        mMusicState = MusicState.PLAYING;
                    }
                    catch (IOException e) {
                        Log.e(LOGGING_TAG, e.toString());
                        resume();
                    }
                }
                else {
                    AlarmService.sendStateChangeIntent(mCon, AlarmService.StateChange.STOP_PLAYBACK);
                }
                break;
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
            mAudioManager = null;
        }
        mMusicState = MusicState.STOPPED;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (mMediaPlayer == null) return;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mMediaPlayer.setVolume(1.0f, 1.0f);
                AlarmService.sendStateChangeIntent(mCon, AlarmService.StateChange.START_PLAYBACK);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                AlarmService.sendStateChangeIntent(mCon, AlarmService.StateChange.STOP_PLAYBACK);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mMediaPlayer.setVolume(0.2f, 0.2f);
                break;
        }
    }
}
