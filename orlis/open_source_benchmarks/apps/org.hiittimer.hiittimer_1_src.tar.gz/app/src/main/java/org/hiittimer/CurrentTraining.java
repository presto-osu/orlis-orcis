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

package org.hiittimer;

import org.hiittimer.database.generated.DaoSession;
import org.hiittimer.database.generated.TrainingPlan;
import org.hiittimer.enums.TrainingAction;
import org.hiittimer.hiittimer.R;
import org.hiittimer.services.CurrentTrainingService;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public final class CurrentTraining extends Activity {
	private TrainingPlan trainingPlan;
	private Long trainingPlanId;

	private DaoSession getDaoSession() {
		return ((HIITTimerApplication) getApplication()).getDaoSession();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(Constants.TRAINING_ID, trainingPlanId);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_current_training);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();

			trainingPlanId = getIntent().getExtras().getLong(Constants.TRAINING_ID);

			trainingPlan = getDaoSession().getTrainingPlanDao().load(trainingPlanId);
		} else {
			trainingPlanId = savedInstanceState.getLong(Constants.TRAINING_ID);
			trainingPlan = getDaoSession().getTrainingPlanDao().load(trainingPlanId);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.current_training, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
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

	public void terminateTraining(View view) {
		stopService(new Intent(this, CurrentTrainingService.class));

		final Intent intent = new Intent(this, TrainingPlans.class);

		startActivity(intent);
	}

	public static class PlaceholderFragment extends Fragment {
		private final static IntentFilter INTENT_FILTER = new IntentFilter(Constants.ON_TICK_BROADCAST_ACTION);
		private final static String CURRENT_ROUND_KEY = "CURRENT_ROUND_KEY",
				CURRENT_ROUND_TIME_LEFT = "CURRENT_ROUND_TIME_LEFT", TRAINING_ACTION_KEY = "TRAINING_ACTION_KEY";
		private OnTickReceiver onTickReceiver;
		private String currentRound, currentRoundTimeLeft;
		private TrainingAction trainingAction;

		private class OnTickReceiver extends BroadcastReceiver {

			@Override
			public void onReceive(Context context, Intent intent) {

				final View rootView = getView().getRootView();
				final Bundle onTickBundle = intent.getExtras();

				setRoundRemainingTimeTextViewText(rootView,
						String.valueOf(onTickBundle.getLong(Constants.ROUND_MILLISECONDS_LEFT)));

				setCurrentRoundRoundIdTextViewText(rootView,
						new StringBuilder().append(String.valueOf(onTickBundle.getLong(Constants.ROUND_ID)))
								.append("/").append(String.valueOf(onTickBundle.getLong(Constants.TOTAL_ROUNDS)))
								.toString());

				setCurrentRoundRoundActionTextViewText(rootView,
						(TrainingAction) onTickBundle.getSerializable(Constants.TRAINING_ACTION));

				if (trainingAction == TrainingAction.FINISHED || trainingAction == TrainingAction.TERMINATED_BY_CALL) {
					disableStopTrainingButton();
				}
			}

		}

		private void setCurrentRoundRoundActionTextViewText(View rootView, TrainingAction trainingAction) {
			final TextView currentRoundRoundAction = (TextView) rootView
					.findViewById(R.id.textViewCurrentRoundRoundAction);

			this.trainingAction = trainingAction;

			currentRoundRoundAction.setText(selectTrainingActionText(trainingAction));
			currentRoundRoundAction.setBackgroundResource(getBackgroundColor(trainingAction));
		}

		private String selectTrainingActionText(TrainingAction trainingAction) {
			String trainingActionText = "";

			switch (trainingAction) {
			case PREPARE_FOR_TRAINING:
				trainingActionText = getString(R.string.training_action_prepare_for_training);
				break;
			case REST:
				trainingActionText = getString(R.string.training_action_rest);
				break;
			case TRAIN:
				trainingActionText = getString(R.string.training_action_train);
				break;
			case TERMINATED_BY_CALL:
				trainingActionText = getString(R.string.training_action_terminated_by_call);
				break;
			case FINISHED:
				trainingActionText = getString(R.string.training_action_finished);
				break;
			}
			return trainingActionText;
		}

		private void setCurrentRoundRoundIdTextViewText(View rootView, String text) {
			final TextView currentRoundRoundId = (TextView) rootView.findViewById(R.id.textViewCurrentRoundRoundId);

			currentRound = text;

			currentRoundRoundId.setText(text);
		}

		private void setRoundRemainingTimeTextViewText(View rootView, String text) {
			final TextView currentRoundRemainingTime = (TextView) rootView
					.findViewById(R.id.textViewCurrentRoundRemainingTime);

			currentRoundTimeLeft = text;

			currentRoundRemainingTime.setText(text);
		}

		private int getBackgroundColor(TrainingAction trainingAction) {
			int backgroundColor = android.R.color.black;

			switch (trainingAction) {
			case FINISHED:
			case TERMINATED_BY_CALL:
			case PREPARE_FOR_TRAINING:
				backgroundColor = android.R.color.holo_orange_dark;
				break;
			case REST:
				backgroundColor = android.R.color.holo_red_dark;
				break;
			case TRAIN:
				backgroundColor = android.R.color.holo_green_dark;
				break;
			}
			return backgroundColor;
		}

		private void registerBroadcastReceiver() {
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

			onTickReceiver = new OnTickReceiver();

			localBroadcastManager.registerReceiver(onTickReceiver, INTENT_FILTER);
		}

		private void unregisterBroadcastReceiver() {
			final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

			localBroadcastManager.unregisterReceiver(onTickReceiver);

		}

		private void setTextSizes() {
			final View root = getView();
			final SharedPreferences defaultSharedPreferences = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			final float textSize = Float.valueOf(defaultSharedPreferences.getString(
					getString(R.string.preferences_training_screen_text_size_key),
					getString(R.string.preferences_training_screen_text_size_default_value)));

			setText(root.findViewById(R.id.textViewCurrentRoundRoundIdLabel), textSize);
			setText(root.findViewById(R.id.textViewCurrentRoundRoundId), textSize);
			setText(root.findViewById(R.id.textViewCurrentRoundRoundActionLabel), textSize);
			setText(root.findViewById(R.id.textViewCurrentRoundRoundAction), textSize);
			setText(root.findViewById(R.id.textViewCurrentRoundRoundRemainingtimeLabel), textSize);
			setText(root.findViewById(R.id.textViewCurrentRoundRemainingTime), textSize);

		}

		private void setText(View view, float textSize) {
			((TextView) view).setTextSize(textSize);
		}

		private void restoreSavedInstanceState(Bundle savedInstanceState, View rootView) {
			trainingAction = (TrainingAction) savedInstanceState.getSerializable(TRAINING_ACTION_KEY);
			currentRound = savedInstanceState.getString(CURRENT_ROUND_KEY);
			currentRoundTimeLeft = savedInstanceState.getString(CURRENT_ROUND_TIME_LEFT);

			setCurrentRoundRoundIdTextViewText(rootView, currentRound);
			setCurrentRoundRoundActionTextViewText(rootView, trainingAction);
			setRoundRemainingTimeTextViewText(rootView, currentRoundTimeLeft);
		}

		private void setupFragmentWhenTrainingHasEnded() {
			final int totalRounds = ((CurrentTraining) getActivity()).trainingPlan.getRounds().size();

			trainingAction = TrainingAction.FINISHED;
			currentRound = totalRounds + "/" + totalRounds;
			currentRoundTimeLeft = "0";

			setCurrentRoundRoundIdTextViewText(getView(), currentRound);
			setCurrentRoundRoundActionTextViewText(getView(), trainingAction);
			setRoundRemainingTimeTextViewText(getView(), currentRoundTimeLeft);

			disableStopTrainingButton();

		}

		private void disableStopTrainingButton() {
			Button stopTrainingButton = (Button) getView().findViewById(R.id.btn_stop_training);
			stopTrainingButton.setEnabled(false);
		}

		public PlaceholderFragment() {
		}

		@Override
		public void onResume() {
			super.onResume();

			if (!CurrentTrainingService.isRunning()) {
				setupFragmentWhenTrainingHasEnded();
			}

			registerBroadcastReceiver();

		}

		@Override
		public void onPause() {
			super.onPause();

			unregisterBroadcastReceiver();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_current_training, container, false);

			if (savedInstanceState != null) {
				restoreSavedInstanceState(savedInstanceState, rootView);
			}

			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setTextSizes();
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putSerializable(TRAINING_ACTION_KEY, trainingAction);
			outState.putString(CURRENT_ROUND_KEY, currentRound);
			outState.putString(CURRENT_ROUND_TIME_LEFT, currentRoundTimeLeft);
		}
	}

}
