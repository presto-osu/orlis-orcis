package com.alexkang.loopboard;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;

public class Sample {

    private static final int SAMPLE_RATE = 44100;

    private String name;

    private AudioTrack audioTrack;
    private int loopPoint;

    private Uri uri;
    private Context context;
    private MediaPlayer currentPlayer;
    private boolean isImported;

    private boolean isLooping = false;

    public Sample(String name, byte[] soundBytes) {
        this.name = name;
        loopPoint = soundBytes.length / 2;
        isImported = false;

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                soundBytes.length,
                AudioTrack.MODE_STATIC
        );

        audioTrack.write(soundBytes, 0, soundBytes.length);
    }

    public Sample(String name, File file, Context context) {
        this.name = name;
        this.context = context;
        isImported = true;

        uri = Uri.parse(file.getAbsolutePath());
    }

    public String getName() {
        return name;
    }

    public void updateSample(byte[] soundBytes) {
        if (!isImported) {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    soundBytes.length,
                    AudioTrack.MODE_STATIC
            );

            audioTrack.write(soundBytes, 0, soundBytes.length);
        }
    }

    public void play(boolean isLooped) {
        isLooping = isLooped;

        if (isImported) {
            if (currentPlayer != null) {
                currentPlayer.seekTo(0);
            } else {
                currentPlayer = MediaPlayer.create(context, uri);
            }

            currentPlayer.setLooping(isLooped);
            currentPlayer.start();
        } else {
            audioTrack.stop();
            audioTrack.reloadStaticData();

            if (isLooped) {
                audioTrack.setLoopPoints(0, loopPoint, -1);
            } else {
                audioTrack.setLoopPoints(0, 0, 0);
            }

            audioTrack.play();
        }
    }

    public void stop() {
        try {
            if (isImported && currentPlayer != null) {
                currentPlayer.stop();
                currentPlayer.release();
                currentPlayer = null;
            } else if (!isImported && audioTrack != null) {
                audioTrack.pause();
                audioTrack.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        isLooping = false;
    }

    public boolean isImported() {
        return isImported;
    }

    public boolean isLooping() {
        return isLooping;
    }

}
