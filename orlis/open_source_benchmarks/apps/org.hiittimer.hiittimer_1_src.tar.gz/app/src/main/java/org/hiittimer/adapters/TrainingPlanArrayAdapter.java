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

package org.hiittimer.adapters;

import java.util.List;

import org.hiittimer.database.generated.TrainingPlan;
import org.hiittimer.hiittimer.R;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public final class TrainingPlanArrayAdapter extends ArrayAdapter<TrainingPlan> {

	public TrainingPlanArrayAdapter(Context context, List<TrainingPlan> trainingPlans) {
		super(context, R.layout.round, trainingPlans);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final TrainingPlan trainingPlan = getItem(position);
		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			final LayoutInflater inflater = LayoutInflater.from(getContext());

			convertView = inflater.inflate(R.layout.training_plan, parent, false);

			viewHolder.trainingPlanName = (TextView) convertView.findViewById(R.id.textViewTrainingPlanName);
			viewHolder.trainingPlanPreTrainingCountdown = (TextView) convertView
					.findViewById(R.id.textViewTrainingPlanPreTrainingCountdown);
			viewHolder.trainingPlanRoundsNumber = (TextView) convertView
					.findViewById(R.id.textViewTrainingPlanRoundsNumber);

			convertView.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Resources resources = convertView.getResources();

		viewHolder.trainingPlanName.setText(new StringBuilder()
				.append(resources.getString(R.string.training_plan_name)).append(":").append(trainingPlan.getName()));
		viewHolder.trainingPlanPreTrainingCountdown.setText(new StringBuilder()
				.append(resources.getString(R.string.training_plan_get_ready_time_in_seconds)).append(":")
				.append(trainingPlan.getGetReadyTimeInSeconds()));
		viewHolder.trainingPlanRoundsNumber.setText(new StringBuilder()
				.append(resources.getString(R.string.training_plan_rounds_number)).append(":")
				.append(trainingPlan.getRounds().size()));

		return convertView;

	}

	private static class ViewHolder {
		TextView trainingPlanName, trainingPlanPreTrainingCountdown, trainingPlanRoundsNumber;

	}
}
