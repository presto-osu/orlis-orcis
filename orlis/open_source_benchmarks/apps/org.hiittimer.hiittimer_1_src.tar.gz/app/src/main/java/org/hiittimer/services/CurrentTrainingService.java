package org.hiittimer.services;

import org.hiittimer.Constants;
import org.hiittimer.CurrentTraining;
import org.hiittimer.HIITTimerApplication;
import org.hiittimer.database.generated.DaoSession;
import org.hiittimer.database.generated.Round;
import org.hiittimer.database.generated.TrainingPlan;
import org.hiittimer.enums.TrainingAction;
import org.hiittimer.hiittimer.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public final class CurrentTrainingService extends Service {

	private final static IntentFilter INTENT_FILTER = new IntentFilter(Constants.ON_TICK_FINISHED_BROADCAST_ACTION);
	private TrainingPlan trainingPlan;
	private Long trainingPlanId;
	private long currentRoundId;
	private long totalRounds;
	private OnTickFinishedReceiver onTickFinishedReceiver;
	private Intent currentRoundServiceIntent;
	private static volatile boolean isRunning = false;
	private PhoneStateListener phoneStateListener;

	private class OnTickFinishedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			performRound();
		}

	}

	private DaoSession getDaoSession() {
		return ((HIITTimerApplication) getApplication()).getDaoSession();
	}

	private void stopNotification() {
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.cancel(R.id.notification_id);
	}

	private Notification trainingNotification() {
		final Intent intent = new Intent(this, CurrentTraining.class);
		intent.putExtra(Constants.TRAINING_ID, trainingPlanId);

		final Notification notification = new NotificationCompat.Builder(this)
				.setContentTitle(getString(R.string.notification_title))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentText(
						new StringBuilder().append(trainingPlan.getName()).append(" - Round:").append(currentRoundId)
								.append("/").append(totalRounds).toString())
				.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0)).build();

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		return notification;
	}

	private void showNotification() {
		final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		notificationManager.notify(R.id.notification_id, trainingNotification());

	}

	private void performRound() {
		if (++currentRoundId <= totalRounds) {
			currentRoundServiceIntent = new Intent(this, CurrentRoundService.class);
			final Bundle roundData = setupRoundDataBundle();

			currentRoundServiceIntent.putExtras(roundData);

			startService(currentRoundServiceIntent);

			showNotification();
		} else {
			updateCurrentRoundActivityWhenTrainingIsFinished();
			stopSelf();
		}

	}

	private Bundle setupRoundDataBundle() {
		final Bundle roundData = new Bundle();
		final Round currentRound = trainingPlan.getRounds().get((int) currentRoundId - 1);

		roundData.putInt(Constants.ROUND_DURATION, currentRound.getWorkInSeconds());
		roundData.putInt(Constants.ROUND_RECOVER_TIME, currentRound.getRestInSeconds());
		roundData.putLong(Constants.ROUND_ID, currentRoundId);
		roundData.putLong(Constants.TOTAL_ROUNDS, totalRounds);
		roundData.putInt(Constants.PRE_TRAINING_COUNTDOWN, trainingPlan.getGetReadyTimeInSeconds());
		return roundData;
	}

	private void stopCurrentRoundService() {
		if (currentRoundServiceIntent != null) {
			stopService(currentRoundServiceIntent);
		}

	}

	private void updateCurrentRoundActivityWhenTrainingIsTerminatedByACall() {

		final Bundle onTickIntentData = new Bundle();
		onTickIntentData.putLong(Constants.ROUND_MILLISECONDS_LEFT, 0);
		onTickIntentData.putLong(Constants.ROUND_ID, currentRoundId);
		onTickIntentData.putLong(Constants.TOTAL_ROUNDS, totalRounds);
		onTickIntentData.putSerializable(Constants.TRAINING_ACTION, TrainingAction.TERMINATED_BY_CALL);

		updateCurrentRoundActivity(onTickIntentData);
	}

	private void updateCurrentRoundActivityWhenTrainingIsFinished() {

		final Bundle onTickIntentData = new Bundle();
		onTickIntentData.putLong(Constants.ROUND_MILLISECONDS_LEFT, 0);
		onTickIntentData.putLong(Constants.ROUND_ID, totalRounds);
		onTickIntentData.putLong(Constants.TOTAL_ROUNDS, totalRounds);
		onTickIntentData.putSerializable(Constants.TRAINING_ACTION, TrainingAction.FINISHED);

		updateCurrentRoundActivity(onTickIntentData);
	}

	private void updateCurrentRoundActivity(Bundle onTickIntentData) {
		final Intent onTickIntent = new Intent(Constants.ON_TICK_BROADCAST_ACTION);

		onTickIntent.putExtras(onTickIntentData);

		final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);

		localBroadcastManager.sendBroadcast(onTickIntent);
	}

	private void registerPhoneStateListener() {
		phoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING:
				case TelephonyManager.CALL_STATE_OFFHOOK:
					stopCurrentRoundService();
					updateCurrentRoundActivityWhenTrainingIsTerminatedByACall();
					CurrentTrainingService.this.stopSelf();
					break;

				default:
					break;
				}

			}

		};

		listenToPhoneState(PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void listenToPhoneState(int phoneState) {
		final TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(
				Context.TELEPHONY_SERVICE);

		telephonyManager.listen(phoneStateListener, phoneState);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		isRunning = true;

		registerPhoneStateListener();

		trainingPlanId = intent.getExtras().getLong(Constants.TRAINING_ID);

		trainingPlan = getDaoSession().getTrainingPlanDao().load(trainingPlanId);

		currentRoundId = 0;

		onTickFinishedReceiver = new OnTickFinishedReceiver();

		LocalBroadcastManager.getInstance(this).registerReceiver(onTickFinishedReceiver, INTENT_FILTER);

		totalRounds = trainingPlan.getRounds().size();

		showNotification();

		performRound();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		listenToPhoneState(PhoneStateListener.LISTEN_NONE);

		final TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(
				Context.TELEPHONY_SERVICE);

		telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		stopCurrentRoundService();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(onTickFinishedReceiver);

		stopNotification();

		isRunning = false;
	}

	public TrainingPlan getTrainingPlan() {
		return trainingPlan;
	}

	public void setTrainingPlan(TrainingPlan trainingPlan) {
		this.trainingPlan = trainingPlan;
	}

	public Long getTrainingPlanId() {
		return trainingPlanId;
	}

	public void setTrainingPlanId(Long trainingPlanId) {
		this.trainingPlanId = trainingPlanId;
	}

	public long getCurrentRoundId() {
		return currentRoundId;
	}

	public void setCurrentRoundId(long currentRoundId) {
		this.currentRoundId = currentRoundId;
	}

	public static boolean isRunning() {
		return isRunning;
	}

}
