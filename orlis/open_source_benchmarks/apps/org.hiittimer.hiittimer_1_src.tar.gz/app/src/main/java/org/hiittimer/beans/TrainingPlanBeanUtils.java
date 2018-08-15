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

package org.hiittimer.beans;

import org.hiittimer.database.generated.TrainingPlan;

public class TrainingPlanBeanUtils {
	private TrainingPlanBeanUtils() {

	}

	public static TrainingPlan convert(TrainingPlanBean trainingPlanBean) {
		final TrainingPlan trainingPlan = new TrainingPlan();

		trainingPlan.setName(trainingPlanBean.getName());
		trainingPlan.setGetReadyTimeInSeconds(trainingPlanBean.getGetReadyTimeInSeconds());

		return trainingPlan;
	}
}
