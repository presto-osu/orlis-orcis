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

import org.hiittimer.adapters.RoundArrayAdapter;
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
import android.preference.PreferenceManager;
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

public class EditTrainingPlan extends Activity {

	private TrainingPlan trainingPlan;
	private Long trainingPlanId;
	private final static String TRAINING_PLAN_ID_KEY = "TRAINING_PLAN_ID_KEY";
	private RoundArrayAdapter roundArrayAdapter;

	private DaoSession getDaoSession() {
		return ((HIITTimerApplication) getApplication()).getDaoSession();
	}

	private void updateTrainingPlan() {
		final String trainingPlanName = ((EditText) findViewById(R.id.editTextTrainingPlanName)).getText().toString();
		final String trainingPlanGetReadyTimeInSeconds = ((EditText) findViewById(R.id.editTextGetReadyTimeInSeconds))
				.getText().toString();

		trainingPlan.setName(trainingPlanName);
		trainingPlan.setGetReadyTimeInSeconds(Integer.valueOf(trainingPlanGetReadyTimeInSeconds));

		getDaoSession().update(trainingPlan);
	}

	private void roundsAdapterUpdate() {
		roundArrayAdapter.clear();
		roundArrayAdapter.addAll(trainingPlan.getRounds());
		roundArrayAdapter.notifyDataSetChanged();
	}

	private boolean areTrainingPlanFieldsCorrect() {
		final String trainingPlanName = ((EditText) findViewById(R.id.editTextTrainingPlanName)).getText().toString();
		final String trainingPlanGetReadyTimeInSeconds = ((EditText) findViewById(R.id.editTextGetReadyTimeInSeconds))
				.getText().toString();

		if (trainingPlanName == null || trainingPlanName.isEmpty() || trainingPlanGetReadyTimeInSeconds == null
				|| trainingPlanGetReadyTimeInSeconds.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private void createAndShowErrorDialog() {
		final AlertDialog.Builder errorDialogbuilder = new AlertDialog.Builder(this);

		errorDialogbuilder.setTitle(getString(R.string.error)).setMessage(getString(R.string.edit_training_error_text))
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});

		errorDialogbuilder.create().show();
	}

	private void createAndShowRoundAlertDialog(final Round round, final boolean editMode) {
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

										final DaoSession daoSession = getDaoSession();

										if (editMode) {
											daoSession.update(round);
										} else {
											daoSession.insert(round);
										}

										trainingPlan.resetRounds();

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_training_plan);

		if (savedInstanceState == null) {
			getFragmentManager()
					.beginTransaction()
					.add(R.id.container, new PlaceholderFragment(), PlaceholderFragment.EDIT_TRAINING_PLAN_FRAGMENT_TAG)
					.commit();

			trainingPlanId = getIntent().getExtras().getLong(Constants.TRAINING_ID);

			trainingPlan = getDaoSession().getTrainingPlanDao().load(trainingPlanId);

		} else {
			trainingPlanId = savedInstanceState.getLong(TRAINING_PLAN_ID_KEY);
			trainingPlan = getDaoSession().getTrainingPlanDao().load(trainingPlanId);
		}

		roundArrayAdapter = new RoundArrayAdapter(this, trainingPlan.getRounds());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.create_new_training_plan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(TRAINING_PLAN_ID_KEY, trainingPlanId);

	}

	@Override
	public void onBackPressed() {

		if (areTrainingPlanFieldsCorrect()) {
			super.onBackPressed();
			updateTrainingPlan();
		} else {
			createAndShowErrorDialog();
		}

	}

	public void addNewRound(View view) {

		Round newRound = new Round();
		newRound.setTrainingPlanId(trainingPlan.getId());
		newRound.setNumber(trainingPlan.getRounds().size() + 1);

		createAndShowRoundAlertDialog(newRound, false);

	}

	public void saveUpdateTrainingPlan(View view) {

	}

	public static class PlaceholderFragment extends Fragment {
		final static String EDIT_TRAINING_PLAN_FRAGMENT_TAG = "EDIT_TRAINING_PLAN_FRAGMENT_TAG";

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
			final RoundArrayAdapter roundArrayAdapter = ((EditTrainingPlan) getActivity()).roundArrayAdapter;

			roundsList.setAdapter(roundArrayAdapter);

			roundsList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Round clickedRound = (Round) parent.getItemAtPosition(position);

					((EditTrainingPlan) getActivity()).createAndShowRoundAlertDialog(clickedRound, true);
				}
			});

			registerForContextMenu(roundsList);

		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final ListView roundsList = (ListView) getView().getRootView().findViewById(R.id.listViewRounds);
			final Round selectedRound = (Round) roundsList.getItemAtPosition(info.position);

			switch (item.getItemId()) {
			case R.id.roundMenuEdit:
				((EditTrainingPlan) getActivity()).createAndShowRoundAlertDialog(selectedRound, true);
				return true;

			case R.id.roundMenuDelete:
				contextualMenuDeleteAction(selectedRound);
				return true;

			default:
				return super.onContextItemSelected(item);

			}

		}

		private void contextualMenuDeleteAction(Round selectedRound) {
			final EditTrainingPlan parent = ((EditTrainingPlan) getActivity());

			final DaoSession daoSession = ((HIITTimerApplication) parent.getApplication()).getDaoSession();

			if (daoSession.getRoundDao().load(selectedRound.getId()) != null) {
				final boolean roundsNumbersNeedToBeRecalculated = (selectedRound.getNumber() != 1)
						|| (selectedRound.getNumber() != parent.trainingPlan.getRounds().size());

				daoSession.delete(selectedRound);

				parent.trainingPlan.resetRounds();

				if (roundsNumbersNeedToBeRecalculated) {
					recalculateRoundsNumbers(parent);
				}

				parent.trainingPlan.resetRounds();

				parent.roundsAdapterUpdate();

				Toast.makeText(parent, getString(R.string.round_deleted), Toast.LENGTH_SHORT).show();

			}
		}

		private void recalculateRoundsNumbers(final EditTrainingPlan parent) {
			int currentRoundNumber = 0;

			for (Round round : parent.trainingPlan.getRounds()) {
				round.setNumber(++currentRoundNumber);
				parent.getDaoSession().update(round);
			}

		}

		private void setupTrainingPlanData() {
			final EditTrainingPlan parent = ((EditTrainingPlan) getActivity());
			final EditText editTextTrainingPlanName = (EditText) getView().getRootView().findViewById(
					R.id.editTextTrainingPlanName);
			final EditText editTextPreTrainingCountdown = (EditText) getView().getRootView().findViewById(
					R.id.editTextGetReadyTimeInSeconds);

			editTextTrainingPlanName.setText(parent.trainingPlan.getName());
			editTextPreTrainingCountdown.setText(String.valueOf(parent.trainingPlan.getGetReadyTimeInSeconds()));

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
			View rootView = inflater.inflate(R.layout.fragment_edit_training_plan, container, false);

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
