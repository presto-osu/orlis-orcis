/**
 * Copyright 2015 Willian Gustavo Veiga
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

package com.willianveiga.countdowntimer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.willianveiga.countdowntimer.Time.CountdownTimer;
import com.willianveiga.countdowntimer.Time.OnFinishListener;
import com.willianveiga.countdowntimer.Time.OnTickListener;
import com.willianveiga.countdowntimer.Utils.AlertDialogUtils;
import com.willianveiga.countdowntimer.Utils.StringUtils;
import com.willianveiga.countdowntimer.Utils.TimeUtils;

public class MainActivity extends Activity implements View.OnFocusChangeListener, View.OnClickListener, OnTickListener, OnFinishListener {
    private CountdownTimer countdownTimer;

    private EditText hoursEditText;
    private EditText minutesEditText;
    private EditText secondsEditText;
    private Button startPauseResumeButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        setUpCountdownTimer();
        setViewFields();
        addEventListeners();
    }

    private void setUpCountdownTimer() {
        countdownTimer = new CountdownTimer();
        countdownTimer.setOnTickListener(this);
        countdownTimer.setOnFinishListener(this);
    }

    private void setViewFields() {
        hoursEditText = (EditText) findViewById(R.id.hoursEditText);
        minutesEditText = (EditText) findViewById(R.id.minutesEditText);
        secondsEditText = (EditText) findViewById(R.id.secondsEditText);
        startPauseResumeButton = (Button) findViewById(R.id.startPauseResumeButton);
        stopButton = (Button) findViewById(R.id.stopButton);
    }

    private void addEventListeners() {
        hoursEditText.setOnFocusChangeListener(this);
        minutesEditText.setOnFocusChangeListener(this);
        secondsEditText.setOnFocusChangeListener(this);
        startPauseResumeButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        EditText timeUnitEditText = ((EditText) view);
        if (hasFocus) {
            timeUnitEditText.setText(null);
        }

        if (!hasFocus && timeUnitEditText.getText().toString().isEmpty()) {
            timeUnitEditText.setText(getInitialTimeUnit());
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;

        if (button.getId() == R.id.startPauseResumeButton) {
            startPauseOrResumeCountdownTimer(button);
        } else {
            stopCountdownTimer();
        }
    }

    private void startPauseOrResumeCountdownTimer(Button button) {
        String startLabel = getResources().getString(R.string.start);
        String resumeLabel = getResources().getString(R.string.resume);
        String pauseLabel = getResources().getString(R.string.pause);

        String buttonText = button.getText().toString();

        if (buttonText.equals(startLabel) || buttonText.equals(resumeLabel)) {
            startResumeCountdownTimer();
        } else if (buttonText.equals(pauseLabel)) {
            pauseCountdownTimer();
        }
    }

    private void pauseCountdownTimer() {
        countdownTimer.stop();
        startPauseResumeButton.setText(R.string.resume);
    }

    private void startResumeCountdownTimer() {
        String hoursString = hoursEditText.getText().toString();
        String minutesString = minutesEditText.getText().toString();
        String secondsString = secondsEditText.getText().toString();

        if (hoursString.isEmpty() || minutesString.isEmpty() || secondsString.isEmpty()) {
            showFieldsEmptyAlertDialog();
            return;
        }

        int hours = Integer.parseInt(hoursEditText.getText().toString());
        int minutes = Integer.parseInt(minutesEditText.getText().toString());
        int seconds = Integer.parseInt(secondsEditText.getText().toString());
        long milliseconds = TimeUtils.toMilliseconds(hours, minutes, seconds);

        if (milliseconds == 0) {
            showMinimumTimeAlertDialog();
            return;
        }

        countdownTimer.start(milliseconds);

        startPauseResumeButton.setText(R.string.pause);

        findViewById(R.id.mainActivityRelativeLayout).requestFocus();

        hoursEditText.setEnabled(false);
        minutesEditText.setEnabled(false);
        secondsEditText.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void showFieldsEmptyAlertDialog() {
        AlertDialogUtils.create(this, R.string.fields_empty_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    private void showMinimumTimeAlertDialog() {
        AlertDialogUtils.create(this, R.string.minimum_time_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }

    private void stopCountdownTimer() {
        countdownTimer.stop();
        resetUserInterface();
    }

    private void resetUserInterface() {
        String initialTimeUnit = getInitialTimeUnit();
        hoursEditText.setText(initialTimeUnit);
        minutesEditText.setText(initialTimeUnit);
        secondsEditText.setText(initialTimeUnit);

        hoursEditText.setEnabled(true);
        minutesEditText.setEnabled(true);
        secondsEditText.setEnabled(true);

        stopButton.setEnabled(false);

        startPauseResumeButton.setText(R.string.start);
    }

    private String getInitialTimeUnit() {
        return getResources().getString(R.string.initial_time_unit);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        updateTimeFields(millisUntilFinished);
    }

    @Override
    public void onFinish() {
        timeFinished();
    }

    private void updateTimeFields(long milliseconds) {
        hoursEditText.setText(StringUtils.padTimeUnit(TimeUtils.millisecondsToHours(milliseconds)));
        minutesEditText.setText(StringUtils.padTimeUnit(TimeUtils.millisecondsToMinutes(milliseconds)));
        secondsEditText.setText(StringUtils.padTimeUnit(TimeUtils.millisecondsToSeconds(milliseconds)));
    }

    private void timeFinished() {
        showTimeFinishedAlertDialog();
        showNotification();
    }

    private void showTimeFinishedAlertDialog() {
        AlertDialogUtils.create(this, R.string.countdown_over_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resetUserInterface();
                dialogInterface.cancel();
            }
        }).show();
    }

    private void showNotification() {
        Notification notification = buildNotification();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, notification);
    }

    private Notification buildNotification() {
        Resources resources = getResources();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, getIntent(), 0);

        return new Notification.Builder(this)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.countdown_over_message))
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();
    }
}