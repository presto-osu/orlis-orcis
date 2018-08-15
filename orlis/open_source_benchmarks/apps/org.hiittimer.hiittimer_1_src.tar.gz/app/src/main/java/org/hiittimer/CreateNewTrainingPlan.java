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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hiittimer.adapters.RoundBeanArrayAdapter;
import org.hiittimer.beans.RoundBean;
import org.hiittimer.beans.RoundBeanUtils;
import org.hiittimer.beans.TrainingPlanBean;
import org.hiittimer.beans.TrainingPlanBeanUtils;
import org.hiittimer.database.generated.DaoSession;
import org.hiittimer.database.generated.Round;
import org.hiittimer.database.generated.TrainingPlan;
import org.hiittimer.hiittimer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CreateNewTrainingPlan extends Activity {

	private final static String TRAINING_PLAN_BEAN_KEY = "TRAINING_PLAN_BEAN_KEY", ROUND_BEANS_KEY = "ROUND_BEANS_KEY";

	private TrainingPlanBean trainingPlanBean;
	private List<RoundBean> roundBeans;
	private RoundBeanArrayAdapter roundBeanArrayAdapter;

	private void roundsAdapterUpdate() {
		final List<RoundBean> roundBeansTemp = new ArrayList<RoundBean>(roundBeans);
		Collections.copy(roundBeansTemp, roundBeans);

		roundBeanArrayAdapter.clear();

		roundBeans = roundBeansTemp;

		roundBeanArrayAdapter.addAll(roundBeans);
		roundBeanArrayAdapter.notifyDataSetChanged();
	}

	private void createAndShowRoundAlertDialog(final RoundBean round, final boolean editMode) {
		final AlertDialog.Builder roundAlertDialogBuilder = new AlertDialog.Builder(this);
		final LayoutInflater layoutInflater = getLayoutInflater();
		final View roundAlertDialogView = layoutInflater.inflate(R.layout.round_alert_dialog, null);

		if (editMode) {
			final EditText editTextRoundWorkInSeconds = (EditText) roundAlertDialogView
					.findViewById(R.id.editTextRoundWorkInSeconds);
			final EditText editTextRoundRestInSeconds = (EditText) roundAlertDialogView
					.findViewById(R.id.editTextRoundRestInSeconds);

			editTextRoundWorkInSeconds.setText(String.valueOf(round.getWorkInSeconds()));
			editTextRoundRestInSeconds.setText(String.valueOf(round.getRestInSeconds()));

		}

		roundAlertDialogBuilder
				.setView(roundAlertDialogView)
				.setTitle(
						new StringBuilder().append(getString(R.string.round_number)).append(" ")
								.append(round.getNumber()).toString())
				.setPositiveButton(editMode ? R.string.edit_action : R.string.add_action,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								final String roundWorkInSeconds = ((EditText) roundAlertDialogView
										.findViewById(R.id.editTextRoundWorkInSeconds)).getText().toString();
								final String roundRestInSeconds = ((EditText) roundAlertDialogView
										.findViewById(R.id.editTextRoundRestInSeconds)).getText().toString();

								if (roundWorkInSeconds == null || roundWorkInSeconds.isEmpty()
										|| roundRestInSeconds == null || roundRestInSeconds.isEmpty()) {
									Toast.makeText(roundAlertDialogView.getContext(),
											getString(R.string.round_creation_error), Toast.LENGTH_LONG).show();
								} else {

									try {
										round.setWorkInSeconds(Integer.parseInt(roundWorkInSeconds));
										round.setRestInSeconds(Integer.parseInt(roundRestInSeconds));

										if (!editMode) {
											CreateNewTrainingPlan.this.roundBeans.add(round);
										}

										roundsAdapterUpdate();

									} catch (NumberFormatException nfe) {
										Toast.makeText(getParent(), getString(R.string.round_creation_error),
												Toast.LENGTH_LONG).show();
									}

								}

							}
						}).setNeutralButton(R.string.cancel_action, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

		roundAlertDialogBuilder.create().show();
	}

	private boolean isTrainingPlanValid() {
		if (trainingPlanBean.getName() == null || trainingPlanBean.getName().isEmpty()) {
			return false;
		} else if (trainingPlanBean.getGetReadyTimeInSeconds() == null) {
			return false;
		} else if (roundBeans.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private void updateTrainingPlan() {

		trainingPlanBean.setName(getTrainingPlanName());
		trainingPlanBean.setGetReadyTimeInSeconds(getGetReadyTimeInSecondsString());
	}

	private String getTrainingPlanName() {
		final EditText editTextTrainingPlanName = (EditText) findViewById(R.id.editTextTrainingPlanName);

		final Editable trainingPlanNameText = editTextTrainingPlanName.getText();

		if (trainingPlanNameText == null || trainingPlanNameText.toString() == null
				|| trainingPlanNameText.toString().isEmpty()) {
			return null;
		} else {
			return trainingPlanNameText.toString();
		}

	}

	private Integer getGetReadyTimeInSecondsString() {
		final EditText editTextTrainingPlanGetReadyTimeInSeconds = (EditText) findViewById(R.id.editTextGetReadyTimeInSeconds);
		final Editable getReadyTimeInSecondsText = editTextTrainingPlanGetReadyTimeInSeconds.getText();
		String getReadyTimeInSecondsString;

		if (getReadyTimeInSecondsText != null) {
			final String getReadyTimeInSecondsStringCandidate = getReadyTimeInSecondsText.toString();

			if (getReadyTimeInSecondsStringCandidate == null || getReadyTimeInSecondsStringCandidate.isEmpty()) {
				getReadyTimeInSecondsString = null;
			} else {
				getReadyTimeInSecondsString = getReadyTimeInSecondsStringCandidate;
			}

		} else {
			getReadyTimeInSecondsString = null;
		}

		return getReadyTimeInSecondsString == null ? null : Integer.valueOf(getReadyTimeInSecondsString);
	}

	private DaoSession getDaoSession() {
		return ((HIITTimerApplication) getApplication()).getDaoSession();
	}

	private void saveTrainingPlan() {
		final DaoSession daoSession = getDaoSession();

		final TrainingPlan trainingPlan = TrainingPlanBeanUtils.convert(trainingPlanBean);

		daoSession.insert(trainingPlan);

		daoSession.update(trainingPlan);

		for (RoundBean roundBean : roundBeans) {
			final Round round = RoundBeanUtils.convert(roundBean);
			round.setTrainingPlanId(trainingPlan.getId());

			daoSession.insert(round);
		}

	}

	private void showQuitOrFixTrainingPlanAlertDialog() {
		final AlertDialog.Builder quitOrFixTrainingPlanAlertDialog = new AlertDialog.Builder(this);

		quitOrFixTrainingPlanAlertDialog.setTitle(getString(R.string.training_plan_contains_errorrs))
				.setPositiveButton(R.string.fix_incorrect_training_plan, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setNeutralButton(R.string.quit, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						CreateNewTrainingPlan.this.finish();

					}
				});

		quitOrFixTrainingPlanAlertDialog.create().show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_new_training_plan);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), PlaceholderFragment.NEW_TRAINING_PLAN_FRAGMENT_TAG)
					.commit();

			trainingPlanBean = new TrainingPlanBean();
			roundBeans = new ArrayList<RoundBean>();

		} else {
			trainingPlanBean = savedInstanceState.getParcelable(TRAINING_PLAN_BEAN_KEY);
			roundBeans = savedInstanceState.getParcelableArrayList(ROUND_BEANS_KEY);

		}

		roundBeanArrayAdapter = new RoundBeanArrayAdapter(this, roundBeans);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_new_training_plan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(TRAINING_PLAN_BEAN_KEY, trainingPlanBean);
		outState.putParcelableArrayList(ROUND_BEANS_KEY, (ArrayList<? extends Parcelable>) roundBeans);

	}

	@Override
	public void onBackPressed() {

		updateTrainingPlan();

		if (isTrainingPlanValid()) {
			saveTrainingPlan();
			super.onBackPressed();
		} else {

			showQuitOrFixTrainingPlanAlertDialog();

		}

	}

	public void addNewRound(View view) {

		RoundBean newRound = new RoundBean();

		newRound.setNumber(RoundBeanUtils.calculateRoundNumber(roundBeans));

		createAndShowRoundAlertDialog(newRound, false);

	}

	public void saveUpdateTrainingPlan(View view) {

	}

	public static class PlaceholderFragment extends Fragment {
		final static String NEW_TRAINING_PLAN_FRAGMENT_TAG = "NEW_TRAINING_PLAN_FRAGMENT_TAG";

		private void showHelpDialog() {
			if (!isHelpDialogToBeShown()) {
				return;
			}

			final AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(getActivity());

			helpDialogBuilder.setTitle(getString(R.string.help))
					.setMessage(getString(R.string.create_new_training__help_dialog_text))
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

						}
					}).setNegativeButton(getString(R.string.dontShow), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							final SharedPreferences sharedPreferences = PreferenceManager
									.getDefaultSharedPreferences(getActivity());

							sharedPreferences
									.edit()
									.putString(getString(R.string.create_new_training_show_help_dialog_key),
											getString(R.string.false_string)).apply();

						}
					});

			helpDialogBuilder.create().show();

		}

		private boolean isHelpDialogToBeShown() {
			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

			return Boolean.valueOf(sharedPreferences.getString(
					getString(R.string.create_new_training_show_help_dialog_key), getString(R.string.true_string)));
		}

		private void setupRoundsList() {

			final ListView roundsList = (ListView) getView().getRootView().findViewById(R.id.listViewRounds);
			final RoundBeanArrayAdapter roundBeanArrayAdapter = ((CreateNewTrainingPlan) getActivity()).roundBeanArrayAdapter;

			roundsList.setAdapter(roundBeanArrayAdapter);

			roundsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final RoundBean clickedRound = (RoundBean) parent.getItemAtPosition(position);

					((CreateNewTrainingPlan) getActivity()).createAndShowRoundAlertDialog(clickedRound, true);
				}
			});

			registerForContextMenu(roundsList);

		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final ListView roundsList = (ListView) getView().getRootView().findViewById(R.id.listViewRounds);
			final RoundBean selectedRound = (RoundBean) roundsList.getItemAtPosition(info.position);

			switch (item.getItemId()) {
			case R.id.roundMenuEdit:
				((CreateNewTrainingPlan) getActivity()).createAndShowRoundAlertDialog(selectedRound, true);
				return true;

			case R.id.roundMenuDelete:
				contextualMenuDeleteAction(selectedRound);
				return true;

			default:
				return super.onContextItemSelected(item);

			}

		}

		private void contextualMenuDeleteAction(RoundBean selectedRound) {
			final CreateNewTrainingPlan parent = ((CreateNewTrainingPlan) getActivity());

			final boolean roundsNumbersNeedToBeRecalculated = (selectedRound.getNumber() != 1)
					|| (selectedRound.getNumber() != parent.roundBeans.size());

			parent.roundBeans.remove(selectedRound);

			if (roundsNumbersNeedToBeRecalculated) {
				RoundBeanUtils.recalculateRoundsNumbers(parent.roundBeans);
			}

			parent.roundsAdapterUpdate();

			Toast.makeText(parent, getString(R.string.round_deleted), Toast.LENGTH_SHORT).show();

		}

		private void setupTrainingPlanData() {
			final CreateNewTrainingPlan parent = ((CreateNewTrainingPlan) getActivity());
			final EditText editTextTrainingPlanName = (EditText) getView().getRootView().findViewById(
					R.id.editTextTrainingPlanName);
			final EditText editTextTrainingPlanGetReadyTimeInSeconds = (EditText) getView().getRootView().findViewById(
					R.id.editTextGetReadyTimeInSeconds);

			if (!(parent.trainingPlanBean.getName() == null || parent.trainingPlanBean.getName().isEmpty())) {
				editTextTrainingPlanName.setText(parent.trainingPlanBean.getName());
			}
			if (parent.trainingPlanBean.getGetReadyTimeInSeconds() != null) {

				editTextTrainingPlanGetReadyTimeInSeconds.setText(String.valueOf(parent.trainingPlanBean
						.getGetReadyTimeInSeconds()));
			}

		}

		@Override
		public void onResume() {

			super.onResume();

			showHelpDialog();
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);

			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.round_contextual_menu, menu);

		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_create_new_training_plan, container, false);

			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);

			setupTrainingPlanData();

			setupRoundsList();
		}
	}

}
