package com.saladdressing.veterondo.utils;


import android.content.Context;
import android.media.MediaPlayer;


public class SamplePlayer {

    Context context;
    MediaPlayer mediaPlayer;

    public SamplePlayer(Context context) {
        this.context = context;
    }


    public void playSound(int rawSound) {

        mediaPlayer = MediaPlayer.create(context, rawSound);
        mediaPlayer.start();


    }

}
