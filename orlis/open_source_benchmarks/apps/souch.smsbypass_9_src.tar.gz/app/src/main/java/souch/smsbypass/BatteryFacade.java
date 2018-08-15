/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package souch.smsbypass;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;


public class BatteryFacade extends Activity {
    public static final boolean DEBUG_MODE = false;

    private TextView mBatteryLevel;
    private long downTime;
    private int msBeforeStartUI = 1500;
    private boolean cancelHaptic;
    private Settings mSettings;
    private boolean mHowtoStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG_MODE)
            msBeforeStartUI = 100;

        setContentView(R.layout.battery_facade);

        mBatteryLevel = (TextView) findViewById(R.id.batteryLevelText);

        mSettings = new Settings(this);
        mSettings.tryUpgradeDB();
        if(mSettings.getFirstStart()) {
            mHowtoStarted = true;

            TextView howto = (TextView) findViewById(R.id.howtoStart);
            howto.setText(getText(R.string.howtoStart));

            mBatteryLevel.setTextColor(Color.argb(255, 255, 0, 0));
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500); //You can manage the blinking time with this parameter
            anim.setStartOffset(20);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            mBatteryLevel.startAnimation(anim);
        }
        else {
            mHowtoStarted = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        downTime = 0;

        mBatteryLevel.setText(GetBatteryLevel(getApplicationContext()) + "%");
        mBatteryLevel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    downTime = motionEvent.getEventTime();
                    cancelHaptic = false;
                    mBatteryLevel.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!cancelHaptic) {
                                mBatteryLevel.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                startSMSbypass();
                            }
                        }
                    }, msBeforeStartUI);

                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (downTime > 0 && (motionEvent.getEventTime() - downTime) > msBeforeStartUI) {
                        downTime = 0;
                    } else {
                        cancelHaptic = true;
                    }
                    return true;
                }

                return false;
            }
        });
    }

    static public float GetBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            // Error checking that probably isn't needed but I added just in case.
            if (level < 0 || scale <= 0) {
                return 50.0f;
            }

            return ((float) level / (float) scale) * 100.0f;
        }
        else {
            return 50.0f;
        }
    }

    public void closeEverything(View view) {
        finish();
    }

    private void stopHowto() {
        if (mHowtoStarted) {
            mHowtoStarted = false;
            mSettings.setFirstStart(false);

            mBatteryLevel.clearAnimation();
            mBatteryLevel.setTextColor(Color.argb(255, 255, 255, 255));

            TextView howto = (TextView) findViewById(R.id.howtoStart);
            howto.setText("");
        }
    }

    private void startSMSbypass() {
        stopHowto();

        Intent intent = new Intent(this, UI.class);
        startActivity(intent);
    }
}


