package com.gk.simpleworkoutjournal;

import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by George on 06.04.2015.
 */
public class WorkoutTimer {

    public static final  String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;

    enum timerState  { DISABLED, STOPPED, TICKING, PAUSED };
    Thread clockThread;

    MenuItem clockView;
    Activity parentActivity;

    private static timerState state;

    WorkoutTimer( Activity parentAct, MenuItem tv) {
        state = timerState.STOPPED;
        clockView = tv;
        parentActivity = parentAct;

        if ( state != timerState.DISABLED )
        {
            drawClock(0,0);
        }
    }

    void drawClock(int minutes, int seconds) {
        clockView.setTitle( String.format("%02d:%02d", minutes, seconds) );
    }


    public static void enable() {
        state = timerState.STOPPED;
    }

    public static void disable() {
        state = timerState.DISABLED;
    }

    void start( int min, int sec) {

        if ( state != timerState.DISABLED )
        {
            stop( true );
            clockThread = new Thread(new TimeRunner( parentActivity, min, sec ));
            clockThread.start();
        }
    }

    void stop( boolean nullClock ) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutTimer :: stopping  timer");
        if ( clockThread != null ) clockThread.interrupt();
        if ( nullClock ) drawClock(0,0);
    }

    void reset() {

    }

    void pause() {

    }

    void nextStep() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "WorkoutTimer :: setting next timer step");

        switch ( state ) {
            case DISABLED:
                break;

            case PAUSED:
            case STOPPED:
                state = timerState.TICKING;
                start( 0,0 );
                break;

            case TICKING:
                state = timerState.PAUSED;
                stop( false );
                break;

        }

    }


    class TimeRunner implements Runnable {
        int minutes, seconds;

        class uiTimerUpdater implements Runnable {
            int m;
            int s;

            uiTimerUpdater( int m, int s) {
                this.m = m;
                this.s = s;
            }

            @Override
            public void run() {
                WorkoutTimer.this.drawClock(m, s);
            }
        }

        Activity uiActivity;

        TimeRunner( Activity parAct, int min, int sec ) {
            this.uiActivity = parAct;
            this.minutes = min;
            this.seconds = sec;

        }

        public void run() {
            if (DEBUG_FLAG) Log.v(APP_NAME, "WorkoutTimer :: starting timer");

            while (true) {

                if ( state == timerState.DISABLED ) {
                    break;
                }

                uiActivity.runOnUiThread(new uiTimerUpdater( minutes, seconds));

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    if (DEBUG_FLAG) Log.v(APP_NAME, "WorkoutTimer :: timer interrupted");
                    return;
                }

                seconds++;

                if (seconds == 60) {
                    minutes++;
                    seconds = 0;
                }
            }
        }
    }

}
