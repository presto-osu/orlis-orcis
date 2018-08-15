/**
	MoTAC - digital board for TAC board game
    Copyright (C) 2013-2014  Carsten Karbach
    
    Contact by mail carstenkarbach@gmx.de

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package de.karbach.tac.core;

/**
 * Event instance handed to every notify message via the DataChangeListener.
 * Provides access to the data model, which was changed.
 * 
 * @author Carsten Karbach
 *
 */
public class DataChangeEvent {

	public static enum ChangeType{
		moveBalls, moveMode, colorInit, colorSwitch, colorSwitchMode, snapshotSaved, ballsSwitched,
		stepBack, stepForward, dataSaved, dataLoaded, ballStarted, tricksMode, markersCleared, bordersChecked,
		ballMoving, scaled, moved, toastdimension, setActionPoint
	}
	
	/**
	 * description of the change notified with this event
	 */
	private ChangeType type;
	
	/**
	 *  the data object, on which this change happened.
	 *  Usually it will be of type BoardData or BoardViewData
	 */
	private Object data;
	
	/**
	 * Create a new event with type and data
	 * @param type description of the change notified with this event
	 * @param data the data object, on which this change happened
	 */
	public DataChangeEvent(ChangeType type, Object data){
		this.type = type;
		this.data = data;
	}
	
	/**
	 * @return string description of the change notified with this event
	 */
	public ChangeType getEventType(){
		return type;
	}
	
	/**
	 * @return the data object, on which this change happened
	 */
	public Object getData(){
		return data;
	}
}
