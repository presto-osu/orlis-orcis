package com.markusborg.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.markusborg.logic.CourtPosition;
import com.markusborg.logic.GhostPlayer;
import com.markusborg.logic.LogHandler;
import com.markusborg.logic.Setting;
import com.markusborg.logic.ThreadControl;

/**
 * The GhostingActivity displays a ghosting session.
 * The actual ghosting session is implemented as an AsyncTask.
 *
 * @author  Markus Borg
 * @since   2015-07-30
 */
public class GhostingActivity extends AppCompatActivity implements GhostingFinishedListener {

    private ThreadControl mControl;
    private Setting mSetting;
    private AudioManager mAudioManager;
    private SoundPool mSoundPool;
    private int[] mSoundIDs; // six sounds, clockwise from front left
    private boolean mLoaded;
    private boolean mSessionCompleted;
    private final int SQUASH_MODE = 0;
    private final int BADMINTON_MODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghosting);
        Bundle extras = getIntent().getExtras();

        // Create a ThreadControl object to manage the ASyncTask
        mControl = new ThreadControl();
        mSessionCompleted = false;

        // Create a new setting - it gets a date
        mSetting = new Setting(extras.getBoolean(MainActivity.ISSQUASH),
                extras.getInt(MainActivity.SETS),
                extras.getInt(MainActivity.REPS),
                extras.getInt(MainActivity.INTERVAL),
                extras.getInt(MainActivity.BREAK),
                extras.getBoolean(MainActivity.IS6POINTS),
                extras.getBoolean(MainActivity.ISAUDIO));

        // If it is not squash mode, change to badminton shuttlecock and background
        if (!mSetting.isSquash()) {
            setBallIcon(BADMINTON_MODE);
            setBackgroundImage(BADMINTON_MODE);
        }

        // Load the sounds if enabled and enough time to play them (2 s)
        if (mSetting.isAudio() && mSetting.getInterval() >= 2000) {
            mSoundIDs = new int[8];
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
            mSoundIDs[0] = mSoundPool.load(this, R.raw.frontleft, 1);
            mSoundIDs[1] = mSoundPool.load(this, R.raw.frontright, 1);
            mSoundIDs[2] = mSoundPool.load(this, R.raw.volleyright, 1);
            mSoundIDs[3] = mSoundPool.load(this, R.raw.backright, 1);
            mSoundIDs[4] = mSoundPool.load(this, R.raw.backleft, 1);
            mSoundIDs[5] = mSoundPool.load(this, R.raw.volleyleft, 1);
            mSoundIDs[6] = mSoundPool.load(this, R.raw.squash, 1);
            mSoundIDs[7] = mSoundPool.load(this, R.raw.badminton, 1);
        }

        final GhostingTask gTask = new GhostingTask();
        gTask.delegate = this;
        gTask.execute(mSetting);

        // The stop button lets the user quit a session, identical to clicking back button.
        final Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (mLoaded) {
                    if (mSetting.isSquash()) {
                        mSoundPool.play(mSoundIDs[6], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                    else {
                        mSoundPool.play(mSoundIDs[7], 1.0f, 1.0f, 1, 0, 1.0f);
                    }
                }
                mControl.cancel();
            }
        });
    }

    /**
     * Called when the GhostingActivity no longer has focus: stop button, back button, or multitask.
     */
    @Override
    protected void onPause() {
        mControl.cancel();
        super.onPause();
    }

    /**
     * Switch the icon from squash ball to shuttlecock
     */
    private void setBallIcon(int ballType) {
        Drawable ballIcon = null;

        // getDrawable changed with API 22
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ballIcon = getBallIconNew(ballType);
        } else {
            ballIcon = getBallIconOld(ballType);
        }

        ImageView ball = (ImageView) findViewById(R.id.ballLeftFront);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightFront);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballLeftMid);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightMid);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballLeftBack);
        ball.setImageDrawable(ballIcon);
        ball = (ImageView) findViewById(R.id.ballRightBack);
        ball.setImageDrawable(ballIcon);
    }

    /**
     * Switch the background from squash court to badminton court
     */
    @SuppressWarnings("deprecation")
    private void setBackgroundImage(int mode) {
        Drawable d = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            d = getBackgroundImageNew(BADMINTON_MODE);
        }
        else {
            d = getBackgroundImageOld(BADMINTON_MODE);
        }

        View view = (View) findViewById(R.id.ghosting_layout);
        if (d != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackground(d);
            }
            else {
                view.setBackgroundDrawable(d);
            }
        }
    }

    /**
     * Return a ball icon from Lollipop and later Android versions
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    private Drawable getBallIconNew(int type) {
        return type == 0 ? getResources().getDrawable(R.drawable.squashball,
                getApplicationContext().getTheme()) :
                getResources().getDrawable(R.drawable.shuttlecock,
                        getApplicationContext().getTheme());
    }

    /**
     * Return a ball icon using deprecated method.
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @SuppressWarnings("deprecation")
    private Drawable getBallIconOld(int type) {
        return type == 0 ? getResources().getDrawable(R.drawable.squashball) :
                getResources().getDrawable(R.drawable.shuttlecock);
    }

    /**
     * Return a ball icon from Lollipop and later Android versions
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @TargetApi(android.os.Build.VERSION_CODES.LOLLIPOP)
    private Drawable getBackgroundImageNew(int type) {
        return type == 0 ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.squashcourt) :
                ContextCompat.getDrawable(getApplicationContext(), R.drawable.badmintoncourt);
    }

    /**
     * Return a badminton court image using deprecated method.
     *
     * @param type 0 = squash otherwise = badminton
     * @return The ball icon
     */
    @SuppressWarnings("deprecation")
    private Drawable getBackgroundImageOld(int type) {
        return type == 0 ? getResources().getDrawable(R.drawable.squashcourt) :
                getResources().getDrawable(R.drawable.badmintoncourt);
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

    /**
     * Notify that a ghosting session has been completed.
     */
    @Override
    public void notifyGhostingFinished() {
        // Only log and show ResultsActivity if the session was indeed completed
        if (mSessionCompleted) {
            printSessionToFile();
            Intent summaryIntent = new Intent(this, ResultsActivity.class);
            summaryIntent.putExtra("DATE", mSetting.getDate());
            summaryIntent.putExtra(MainActivity.ISSQUASH, mSetting.isSquash());
            summaryIntent.putExtra(MainActivity.SETS, mSetting.getSets());
            summaryIntent.putExtra(MainActivity.REPS, mSetting.getReps());
            summaryIntent.putExtra(MainActivity.INTERVAL, mSetting.getInterval());
            summaryIntent.putExtra(MainActivity.BREAK, mSetting.getBreakTime());
            summaryIntent.putExtra(MainActivity.IS6POINTS, mSetting.isSixPoints());
            summaryIntent.putExtra(MainActivity.ISAUDIO, mSetting.isAudio());
            startActivity(summaryIntent);
        }
        // Reset the finished flag
        mSessionCompleted = false;
    }

    /**
     * Print the session settings to file.
     */
    private void printSessionToFile() {
        LogHandler logger = new LogHandler(getApplicationContext());
        logger.addSessionToLog(mSetting);
    }

    /**
     * Parallel task that manages the actual ghosting session.
     */
    public class GhostingTask extends AsyncTask<Setting, String, String> {

        public GhostingFinishedListener delegate = null;
        private TextView lblProgress;

        @Override
        protected String doInBackground(Setting... params) {
            Setting theSetting = params[0];
            GhostPlayer theGhost = new GhostPlayer(theSetting.isSixPoints());

            lblProgress = (TextView) findViewById(R.id.lblProgress);

            // Loop the sets
            boolean finalSet = false;
            for (int i = 1; i <= theSetting.getSets() && !mControl.isCancelled(); i++) {

                displayCountdown();

                if (i >= theSetting.getSets())
                    finalSet = true;
                // Loop the reps
                for (int j = 1; j <= theSetting.getReps() && !mControl.isCancelled(); j++) {

                    CourtPosition pos = theGhost.serve(); // TODO: only serve the first time

                    String progress = new String(j + " / " + theSetting.getReps() +
                            " (Set " + i + ")");

                    // increase the speed if the ghost ball didn't go to a corner
                    int sleepTime = theSetting.getInterval();
                    if (!pos.isCornerPos()) {
                        sleepTime = (sleepTime * 2) / 3;
                    }

                    // Turn on corner
                    publishProgress(progress, pos.toString());
                    try {
                        Thread.sleep((sleepTime / 2));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Turn off corner
                    publishProgress(progress, pos.toString(), "OFF");
                    try {
                        Thread.sleep((sleepTime / 2) );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // end reps loop

                // Create a toast message when a set is completed
                if (!mControl.isCancelled()) {
                    publishProgress(null);
                }

                // No rest between sets if there are none left
                if (!finalSet && !mControl.isCancelled()) {
                    try {
                        Thread.sleep(theSetting.getBreakTime() * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } // end sets loop

            // check if the set was completed, or cancelled somehow
            if (!mControl.isCancelled()) {
                mSessionCompleted = true;
            }

            finish();

            return "Done";
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress == null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Set completed!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else if (progress.length == 1) {
                lblProgress.setText(progress[0]);
            }
            else if (progress.length == 2) {
                lblProgress.setText(progress[0]);
                String cornerToFlash = progress[1];
                ImageView ball;

                if (cornerToFlash.equals("L_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballLeftFront);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[0], 1.0f, 0.1f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("R_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballRightFront);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[1], 0.1f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("L_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballLeftBack);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[4], 1.0f, 0.1f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("R_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballRightBack);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[3], 0.1f, 1.0f, 1, 0, 1.0f);
                    }
                }
                else if (cornerToFlash.equals("L_MID")) {
                    ball = (ImageView) findViewById(R.id.ballLeftMid);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[5], 1.0f, 0.1f, 1, 0, 1.0f);
                    }
                }
                else {
                    ball = (ImageView) findViewById(R.id.ballRightMid);
                    if (mLoaded) {
                        mSoundPool.play(mSoundIDs[2], 0.1f, 1.0f, 1, 0, 1.0f);
                    }
                }
                ball.setVisibility(View.VISIBLE);
            }
            else if (progress.length == 3) {
                String cornerToTurnOff = progress[1];
                ImageView ball;

                // Find the current corner
                if (cornerToTurnOff.equals("L_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballLeftFront);
                }
                else if (cornerToTurnOff.equals("R_FRONT")) {
                    ball = (ImageView) findViewById(R.id.ballRightFront);
                }
                else if (cornerToTurnOff.equals("L_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballLeftBack);
                }
                else if (cornerToTurnOff.equals("R_BACK")) {
                    ball = (ImageView) findViewById(R.id.ballRightBack);
                }
                else if (cornerToTurnOff.equals("L_MID")) {
                    ball = (ImageView) findViewById(R.id.ballLeftMid);
                }
                else {
                    ball = (ImageView) findViewById(R.id.ballRightMid);
                }

                // Reset the current corner
                ball.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.notifyGhostingFinished();
        }

        /**
         * Display a countdown from 5 s.
         */
        private void displayCountdown() {
            publishProgress("5");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("4");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("3");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("2");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("1");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            publishProgress("");
        }

    }
}
