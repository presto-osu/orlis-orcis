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

import org.hiittimer.adapters.TrainingPlanArrayAdapter;
import org.hiittimer.database.generated.DaoSession;
import org.hiittimer.database.generated.Round;
import org.hiittimer.database.generated.TrainingPlan;
import org.hiittimer.hiittimer.R;
import org.hiittimer.services.CurrentTrainingService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Toast;

public final class TrainingPlans extends Activity {
	private TrainingPlanArrayAdapter trainingPlanArrayAdapter;

	private DaoSession getDaoSession() {
		return ((HIITTimerApplication) getApplication()).getDaoSession();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_plans);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

		trainingPlanArrayAdapter = new TrainingPlanArrayAdapter(this, getDaoSession().getTrainingPlanDao().loadAll());

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupTrainingPlanArray();

	}

	private void setupTrainingPlanArray() {
		trainingPlanArrayAdapter.clear();
		trainingPlanArrayAdapter.addAll(getDaoSession().getTrainingPlanDao().loadAll());
		trainingPlanArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();

		final Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.training_plans, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	public void createNewTraining(View view) {
		final Intent intent = new Intent(this, CreateNewTrainingPlan.class);

		startActivity(intent);
	}

	public static class PlaceholderFragment extends Fragment {

		private void contextualMenuDeleteAction(final TrainingPlan selectedTrainingPlan,
				final TrainingPlanArrayAdapter trainingPlanArrayAdapter) {
			final TrainingPlans parent = ((TrainingPlans) getActivity());
			final DaoSession daoSession = ((HIITTimerApplication) parent.getApplication()).getDaoSession();

			daoSession.runInTx(new Runnable() {

				@Override
				public void run() {
					selectedTrainingPlan.resetRounds();
					for (Round round : selectedTrainingPlan.getRounds()) {
						daoSession.delete(round);
					}

					daoSession.delete(selectedTrainingPlan);

					trainingPlanArrayAdapter.remove(selectedTrainingPlan);
					trainingPlanArrayAdapter.notifyDataSetChanged();

					Toast.makeText(parent, getString(R.string.training_plan_deleted), Toast.LENGTH_SHORT).show();

				}
			});

		}

		private void contextualMenuEditAction(TrainingPlan selectedTrainingPlan) {

			final Intent intent = new Intent(getActivity(), EditTrainingPlan.class);

			intent.putExtra(Constants.TRAINING_ID, selectedTrainingPlan.getId());

			startActivity(intent);
		}

		private void contextualMenuStartAction(TrainingPlan selectedTrainingPlan) {
			if (selectedTrainingPlan.getRounds().isEmpty()) {
				return;
			}

			startCurrentTraining(selectedTrainingPlan);

		}

		private void startCurrentTraining(TrainingPlan selectedTrainingPlan) {
			if (CurrentTrainingService.isRunning()) {
				showTrainingAlreadyRunningAlertDialog();

			} else {
				startCurrentTrainingActivity(selectedTrainingPlan);

				startCurrentTrainingService(selectedTrainingPlan);
			}
		}

		private void showTrainingAlreadyRunningAlertDialog() {
			final AlertDialog.Builder serviceAlreadyRunningAlertDialog = new AlertDialog.Builder(getActivity());

			serviceAlreadyRunningAlertDialog.setMessage(getString(R.string.training_already_running))
					.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

						}
					});

			serviceAlreadyRunningAlertDialog.create().show();
		}

		private void startCurrentTrainingService(TrainingPlan selectedTrainingPlan) {
			final Intent intentService = new Intent(getActivity(), CurrentTrainingService.class);
			intentService.putExtra(Constants.TRAINING_ID, selectedTrainingPlan.getId());
			getActivity().startService(intentService);
		}

		private void startCurrentTrainingActivity(TrainingPlan selectedTrainingPlan) {
			final Intent intent = new Intent(getActivity(), CurrentTraining.class);
			intent.putExtra(Constants.TRAINING_ID, selectedTrainingPlan.getId());

			startActivity(intent);
		}

		private void setupTrainingPlanList() {
			final ListView trainingPlansList = (ListView) getView().getRootView().findViewById(
					R.id.listViewTrainingPlans);
			final TrainingPlanArrayAdapter trainingPlanAdapter = ((TrainingPlans) getActivity()).trainingPlanArrayAdapter;

			trainingPlansList.setAdapter(trainingPlanAdapter);

			trainingPlansList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final TrainingPlan clickedTrainingPlan = (TrainingPlan) parent.getItemAtPosition(position);

					final Intent intent = new Intent(getActivity(), EditTrainingPlan.class);

					intent.putExtra(Constants.TRAINING_ID, clickedTrainingPlan.getId());

					startActivity(intent);
				}
			});

			registerForContextMenu(trainingPlansList);

		}

		private void showHelpDialog() {
			if (!isHelpDialogToBeShown()) {
				return;
			}

			final AlertDialog.Builder helpDialogBuilder = new AlertDialog.Builder(getActivity());

			helpDialogBuilder.setTitle(getString(R.string.help))
					.setMessage(getString(R.string.training_plans_help_dialog_text))
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
									.putString(getString(R.string.training_plans_show_help_dialog_key),
											getString(R.string.false_string)).apply();

						}
					});

			helpDialogBuilder.create().show();

		}

		private boolean isHelpDialogToBeShown() {
			final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

			return Boolean.valueOf(sharedPreferences.getString(getString(R.string.training_plans_show_help_dialog_key),
					getString(R.string.true_string)));
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
			inflater.inflate(R.menu.training_plan_contextual_menu, menu);

		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final ListView trainingPlansList = (ListView) getView().getRootView().findViewById(
					R.id.listViewTrainingPlans);
			final TrainingPlan selectedTrainingPlan = (TrainingPlan) trainingPlansList.getItemAtPosition(info.position);

			switch (item.getItemId()) {
			case R.id.trainingMenuEdit:
				contextualMenuEditAction(selectedTrainingPlan);
				return true;

			case R.id.trainingMenuDelete:
				contextualMenuDeleteAction(selectedTrainingPlan,
						((TrainingPlans) getActivity()).trainingPlanArrayAdapter);
				return true;

			case R.id.trainingMenuStart:
				contextualMenuStartAction(selectedTrainingPlan);
				return true;

			default:
				return super.onContextItemSelected(item);

			}

		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.training_plans, container, false);
			return rootView;
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {

			super.onActivityCreated(savedInstanceState);

			setupTrainingPlanList();
		}
	}

}
