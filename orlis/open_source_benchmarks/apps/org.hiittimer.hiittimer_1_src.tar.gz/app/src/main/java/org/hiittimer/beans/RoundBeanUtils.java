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

import java.util.List;

import org.hiittimer.database.generated.Round;

public class RoundBeanUtils {
	private RoundBeanUtils() {

	}

	public static Round convert(RoundBean roundBean) {
		final Round round = new Round();

		round.setNumber(roundBean.getNumber());
		round.setWorkInSeconds(roundBean.getWorkInSeconds());
		round.setRestInSeconds(roundBean.getRestInSeconds());

		return round;
	}

	public static int calculateRoundNumber(List<RoundBean> rounds) {
		return rounds.size() + 1;
	}

	public static void recalculateRoundsNumbers(List<RoundBean> rounds) {
		int currentRoundNumber = 0;
	
		for (RoundBean roundBean : rounds) {
			roundBean.setNumber(++currentRoundNumber);
		}
	}
}
