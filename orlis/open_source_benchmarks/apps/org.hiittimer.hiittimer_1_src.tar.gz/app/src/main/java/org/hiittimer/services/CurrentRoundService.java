/*
 * 
 * HIIT Timer - A simple timer for high intensity trainings
 Copyright (C) 2015 Lorenzo Chiovini

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package org.hiittimer.services;

import org.hiittimer.Constants;
import org.hiittimer.enums.TrainingAction;
import org.hiittimer.hiittimer.R;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public final class CurrentRoundService extends IntentService {
	private final static String TAG = "org.hiittimer.services.CurrentRoundService";
	private final static int SECONDS_TO_MILLIS = 1000;
	private LocalBroadcastManager localBroadcastManager;
	private SoundPool soundPool;
	private int countdownSoundId;
	private SharedPreferences sharedPreferences;
	private Boolean isAudioOn;
	private Float volume;

	private int roundDuration, roundRecoverTime, preTrainingCountdown;
	private long roundId, totalRounds;
	private volatile boolean forcefulStopRequired = false;

	private static volatile boolean isRunning = false;

	private void setupRound(Intent intent) {
		final Bundle roundData = intent.getExtras();

		roundDuration = roundData.getInt(Constants.ROUND_DURATION);
		roundRecoverTime = roundData.getInt(Constants.ROUND_RECOVER_TIME);
		roundId = roundData.getLong(Constants.ROUND_ID);
		totalRounds = roundData.getLong(Constants.TOTAL_ROUNDS);
		preTrainingCountdown = roundData.getInt(Constants.PRE_TRAINING_COUNTDOWN);
	}

	private void setupSoundPool() {
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

		countdownSoundId = soundPool.load(this, R.raw.countdown, 1);
	}

	public CurrentRoundService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		isRunning = true;

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		setupRound(intent);

		setupSoundPool();

		prepareToAction();

		performRound();

		recoverTime();

		broadcastRoundDoneMessage();
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		forcefulStopRequired = true;

		if (soundPool != null) {
			soundPool.release();
		}

		isRunning = false;
	}

	private void playCountdownSound(long remainingtime) {

		if (!isAudioEnabled() || !isTimeToReproduceSound(remainingtime)) {
			return;
		}

		soundPool.play(countdownSoundId, getVolume(), getVolume(), 1, 0, 1f);

	}

	private float getVolume() {
		if (volume == null) {
			volume = Float.valueOf(sharedPreferences.getString(getString(R.string.preferences_volume_key),
					getString(R.string.preferences_volume_default_value)));
		}

		return volume;
	}

	private boolean isTimeToReproduceSound(long remainingTime) {
		return (remainingTime <= 3L) && remainingTime > 0;
	}

	private boolean isAudioEnabled() {
		if (isAudioOn == null) {
			final String preference_audio_onoff_key = getString(R.string.preferences_audio_onoff_key);
			isAudioOn = sharedPreferences.getBoolean(preference_audio_onoff_key, true);
		}

		return isAudioOn;
	}

	private void prepareToAction() {
		if (!(roundId == 1 && preTrainingCountdown != 0)) {
			return;
		}

		performCycle(preTrainingCountdown, TrainingAction.PREPARE_FOR_TRAINING);
	}

	private void recoverTime() {
		if (roundRecoverTime == 0) {
			return;
		} else {
			performCycle(roundRecoverTime, TrainingAction.REST);
		}
	}

	private void performRound() {
		performCycle(roundDuration, TrainingAction.TRAIN);
	}

	private void performCycle(int cycleDuration, TrainingAction roundAction) {
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		sendTickBroadcastMessage(cycleDuration, roundAction);

		boolean hasCycleTimeElapsed = false;
		long previousTickTime = System.currentTimeMillis();
		int secondsLapsed = 0;

		while (!hasCycleTimeElapsed && !forcefulStopRequired) {
			long currentTime = System.currentTimeMillis();

			if (checkForValidTime(previousTickTime, currentTime)) {
				previousTickTime = currentTime;

				final int cycleSecondsLeft = cycleDuration - ++secondsLapsed;

				sendTickBroadcastMessage(cycleSecondsLeft, roundAction);

				playCountdownSound(cycleSecondsLeft);

				hasCycleTimeElapsed = hasCycleTimeElapsed(cycleSecondsLeft);
			}
		}
	}

	private boolean checkForValidTime(long previousTickTime, long currentTime) {
		return currentTime - previousTickTime >= SECONDS_TO_MILLIS;
	}

	private void broadcastRoundDoneMessage() {
		final Intent onCountDownFinishedIntent = new Intent(Constants.ON_TICK_FINISHED_BROADCAST_ACTION);

		localBroadcastManager.sendBroadcast(onCountDownFinishedIntent);
	}

	private boolean hasCycleTimeElapsed(int roundSecondsLeft) {
		if (roundSecondsLeft <= 0) {
			return true;
		} else {
			return false;
		}
	}

	private void sendTickBroadcastMessage(int roundSecondsLeft, TrainingAction roundAction) {
		final Intent onTickIntent = new Intent(Constants.ON_TICK_BROADCAST_ACTION);

		final Bundle onTickIntentData = new Bundle();
		onTickIntentData.putLong(Constants.ROUND_MILLISECONDS_LEFT, roundSecondsLeft);
		onTickIntentData.putLong(Constants.ROUND_ID, roundId);
		onTickIntentData.putLong(Constants.TOTAL_ROUNDS, totalRounds);
		onTickIntentData.putSerializable(Constants.TRAINING_ACTION, roundAction);

		onTickIntent.putExtras(onTickIntentData);

		localBroadcastManager.sendBroadcast(onTickIntent);
	}

	public static boolean isRunning() {
		return isRunning;
	}

}
