/**
 *   This file is part of Ophasenkistenstapeln.
 *
 *   Ophasenkistenstapeln is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Ophasenkistenstapeln is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Ophasenkistenstapeln.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.d120.ophasekistenstapeln;

import android.app.Fragment;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * @author exploide
 * @author bhaettasch
 */
public class CountdownFragment extends Fragment {

    private Button buttonCountdownStart300;
    private Button buttonCountdownStart010;
    private Button buttonCountdownStop;

    private TextView txtCountdown;

    CountDownTimer countdown;
    boolean countdownActive = false;
    private long countdownRemaining;

    Animation blinkAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_countdown, container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

		/*
         * =========================================================== Get View
		 * references
		 * ===========================================================
		 */

        buttonCountdownStart300 = (Button) getView().findViewById(
                R.id.btnStartCountdown300);
        buttonCountdownStart010 = (Button) getView().findViewById(
                R.id.btnStartCountdown010);
        buttonCountdownStop = (Button) getView().findViewById(
                R.id.btnCountdownStop);

        txtCountdown = (TextView) getView().findViewById(R.id.txtCountdown);

		/*
		 * =========================================================== Register
		 * View callbacks
		 * ===========================================================
		 */

        buttonCountdownStart300.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startCountdown(3 * 60 * 1000);
            }
        });

        buttonCountdownStart010.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startCountdown(10 * 1000);
            }
        });

        buttonCountdownStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (countdown == null)
                    return;

                if (countdownActive)
                    stopCountdown();
            }
        });

        // Blink animation for countdown field
        blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkAnimation.setDuration(200); // You can manage the time of the blink
        // with this parameter
        blinkAnimation.setStartOffset(20);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        this.stopCountdown();
        if (countdown != null)
            this.countdown.cancel();
        this.countdown = null;
        super.onSaveInstanceState(outState);
        //Save remaining time
        outState.putBoolean("countdownActive", this.countdownActive);
        outState.putLong("countdownRemaining", this.countdownRemaining);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        //restore remaining time
        this.countdownActive = savedInstanceState.getBoolean("countdownActive");
        this.countdownRemaining = savedInstanceState
                .getLong("countdownRemaining");
    }

    /**
     * Start a countdown and register callback events
     *
     * @param milliseconds duration in milliseconds
     */
    public void startCountdown(int milliseconds) {
        Log.i("CountdownFragment", "starting countdown for " + milliseconds
                + "millis");
        // Cancel the running countdown (if any)
        if (countdown != null)
            countdown.cancel();

        // Clear animation (if any)
        txtCountdown.clearAnimation();

        // Create a new countdown with the given value
        countdown = new CountDownTimer(milliseconds, 10) {

            @Override
            public void onTick(long millisUntilFinished) {
                // Show remaining time in format mm:ss:hh
                // (minutes:seconds:hundreds)
                long minutes = TimeUnit.MILLISECONDS
                        .toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS
                        .toSeconds(millisUntilFinished)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                long hundreds = millisUntilFinished
                        - TimeUnit.SECONDS.toMillis(seconds)
                        - TimeUnit.MINUTES.toMillis(minutes);
                Log.d("countdown fragment", "txt: " + txtCountdown.getText());
                countdownRemaining = ((minutes * 60 + seconds) * 1000 + hundreds);
                txtCountdown.setText(String.format("%02d:%02d:%02d", minutes,
                        seconds, hundreds / 10));
            }

            @Override
            public void onFinish() {
                // Display 00:00:00
                // TODO throws exception "not Attached to Acitivity" after
                // screen rotate
                txtCountdown.setText(getString(R.string.countdownDefault));

                // Play sound
                try {
                    Uri notification = RingtoneManager
                            .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getActivity(),
                            notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Blink...
                txtCountdown.startAnimation(blinkAnimation);

                // ...and clear after 10 seconds
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        txtCountdown.clearAnimation();
                    }
                };

                Handler mHandler = new Handler();
                mHandler.postDelayed(r, 10000);

                // Disable stop button
                buttonCountdownStop.setEnabled(false);
            }
        };

        // Start and activate Stop button
        countdownActive = true;
        countdown.start();
        buttonCountdownStop.setEnabled(true);
    }

    /**
     * Stop running coutdown and disable button
     */
    public void stopCountdown() {
        Log.d("Countdown", "cancle countdown");
        countdownActive = false;
        if (this.countdown != null) {
            countdown.cancel();
        }
        buttonCountdownStop.setEnabled(false);
        txtCountdown.clearAnimation();
    }
}