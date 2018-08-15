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

public final class Constants {
	public static final String TRAINING_ID = "org.hiittimer.constants.TRAINING_ID";

	public final static String ON_TICK_BROADCAST_ACTION = "org.hiittimer.constants.ON_TICK_BROADCAST_ACTION",
			ON_TICK_FINISHED_BROADCAST_ACTION = "org.hiittimer.constants.ON_TICK_FINISHED_BROADCAST_ACTION";

	public final static String STOP_TRAINING_BROADCAST_ACTION = "org.hiittimer.constants.STOP_TRAINING_BROADCAST_ACTION";

	// onTick bundle data keys
	public final static String ON_TICK_ROUND_NUMBER = "org.hiittimer.constants.ON_TICK_ROUND_NUMBER",
			ON_TICK_ROUNDS_LEFT = "org.hiittimer.constants.ON_TICK_ROUNDS_LEFT",
			ROUND_MILLISECONDS_LEFT = "org.hiittimer.constants.ON_TICK_ROUND_SECONDS_LEFT",
			ROUND_DURATION = "org.hiittimer.constants.ROUND_DURATION",
			ROUND_RECOVER_TIME = "org.hiittimer.constants.ROUND_RECOVER_TIME",
			ROUND_ID = "org.hiittimer.constants.ROUND_NUMBER",
			TRAINING_ACTION = "org.hiittimer.constants.ROUND_ACTION",
			TOTAL_ROUNDS = "org.hiittimer.constants.TOTAL_ROUNDS",
			PRE_TRAINING_COUNTDOWN = "org.hiittimer.constants.PRE_TRAINING_COUNTDOWN";

	private Constants() {

	}

}
