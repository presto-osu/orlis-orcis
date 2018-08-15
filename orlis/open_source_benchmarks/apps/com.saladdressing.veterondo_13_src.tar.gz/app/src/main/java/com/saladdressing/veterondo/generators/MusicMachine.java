package com.saladdressing.veterondo.generators;

import android.content.Context;
import android.util.Log;

import com.saladdressing.veterondo.R;
import com.saladdressing.veterondo.enums.WeatherKind;
import com.saladdressing.veterondo.interfaces.PlaybackListener;
import com.saladdressing.veterondo.utils.SamplePlayer;

import java.util.ArrayList;
import java.util.Random;

public class MusicMachine {

    public static final int[] MAJOR_SEVENTH_PATTERN = {4, 7, 11};
    public static final int[] MINOR_SEVENTH_PATTERN = {3, 7, 10};
    public static final int[] AUGMENTED_PATTERN = {4, 8, 12};
    public static final int[] DIMINISHED_PATTERN = {3, 6, 9};

    Context context;
    WeatherKind weatherKind;
    int scale[] = MAJOR_SEVENTH_PATTERN;


    public MusicMachine(Context context, WeatherKind weatherKind) {

        this.context = context;
        this.weatherKind = weatherKind;

    }

    /*
    Returns an array of guitar tones. More sounds will be added soon, so expect vibraphoneTones(), rhodesTones() and
    maybe even kazooTones() in the near future!
     */
    public static Integer[] guitarNotes() {

        return new Integer[]{R.raw.c4, R.raw.cs4, R.raw.d4, R.raw.ds4, R.raw.e4, R.raw.f4, R.raw.fs4, R.raw.g4, R.raw.gs4, R.raw.a4,
                R.raw.as4, R.raw.b4, R.raw.c5, R.raw.cs5, R.raw.d5, R.raw.ds5, R.raw.e5, R.raw.f5, R.raw.fs5, R.raw.g5, R.raw.gs5,
                R.raw.a5, R.raw.as5, R.raw.b5};


    }


    /**
     *
     * @param interval Time to pause between playback of notes in the arpeggio
     * @param playbackCompletedListener In case you need to apply events at the start and end of playback
     */
    public void playPattern(final long interval, final PlaybackListener playbackCompletedListener) {

        final SamplePlayer samplePlayer = new SamplePlayer(context);
        final ArrayList<Integer> pattern = generateArpeggio();

        playbackCompletedListener.onPlaybackStarted();


        Runnable runnable = new Runnable() {

            public void run() {
                for (int tone : pattern) {

                    Log.i("PATTERN NOTE = ", tone + "");


                    samplePlayer.playSound(tone);

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                playbackCompletedListener.onPlaybackCompleted();
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();


    }

    @SuppressWarnings("unused")
    /*
    Overloaded playPattern method without the PlaybackListener callback.
     */
    public void playPattern(final long interval) {

        final SamplePlayer samplePlayer = new SamplePlayer(context);
        final ArrayList<Integer> pattern = generateArpeggio();


        Runnable runnable = new Runnable() {

            public void run() {
                for (int tone : pattern) {

                    Log.i("PATTERN NOTE = ", tone + "");


                    samplePlayer.playSound(tone);

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        };

        Thread thread = new Thread(runnable);
        thread.start();


    }

    public ArrayList<Integer> generateArpeggio() {

        ArrayList<Integer> tonesToReturn = new ArrayList<>();
        Integer[] tones = guitarNotes();


        // select random starting note

        Random random = new Random();
        int randomBaseNote = random.nextInt(guitarNotes().length - 12);


        tonesToReturn.add(tones[randomBaseNote]);
        setArpFromWeather();


        for (int i = 0; i < 3; i++) {

            if ((randomBaseNote + scale[i]) < tones.length) {

                tonesToReturn.add(tones[randomBaseNote + scale[i]]);
            } else {
                Log.e("NOTE ", "not added");
            }

        }


        return tonesToReturn;

    }

    public void setArpFromWeather() {

        if (weatherKind == WeatherKind.SUNNY || weatherKind == WeatherKind.NIGHTLY) {
            scale = MAJOR_SEVENTH_PATTERN;
        }

        if (weatherKind == WeatherKind.CLOUDY) {
            scale = MINOR_SEVENTH_PATTERN;
        }

        if (weatherKind == WeatherKind.SNOWY) {
            scale = AUGMENTED_PATTERN;
        }

        if (weatherKind == WeatherKind.RAINY) {
            scale = DIMINISHED_PATTERN;
        }

    }


}
