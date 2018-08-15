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

package de.karbach.tac.ui;

import android.view.ScaleGestureDetector;

/**
 * Scale control of map
 * @author Carsten Karbach
 *
 */
public class BoardScale extends ScaleGestureDetector.SimpleOnScaleGestureListener{

	/**
	 * board controller.
	 */
	private BoardControl boardControl;
	
	/**
	 * Saves the scale factor at the beginning of the scale event
	 */
	private float beginScaleFactor;
	
	public BoardScale(BoardControl boardControl){
		this.boardControl = boardControl;
	}
	
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector){
		beginScaleFactor = boardControl.getScaleFactor();
		return true;
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		boardControl.setScale((float)detector.getScaleFactor()*beginScaleFactor);
		return false;
	}

}
