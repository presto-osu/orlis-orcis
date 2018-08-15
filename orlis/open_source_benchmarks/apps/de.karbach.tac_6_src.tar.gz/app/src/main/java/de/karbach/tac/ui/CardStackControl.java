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

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import de.karbach.tac.core.Card;
import de.karbach.tac.network.Client;

/**
 * Is connected to the CardStackView.
 * It recognizes gestures on the cardstack view and executes the appropirate command 
 * on the activity.
 * 
 * @author Carsten Karbach
 *
 */
public class CardStackControl extends SimpleOnGestureListener{

	/**
	 * View controlled here
	 */
	private CardStackView cardStack;
	
	/**
	 * The client, which is playing with the given cardStack
	 */
	private Client client;
	
	/**
	 * Init the controller with access to the stack view and to the 
	 * client for executing the commands invoked on the view.
	 * 
	 * @param view the card stack view
	 * @param client the client playing here
	 */
	public CardStackControl(CardStackView view, Client client){
		this.cardStack = view;
		this.client = client;
	}
	
	@Override
	public boolean onDoubleTap (MotionEvent e){
		//Do nothing, if the client is not yet connected
		if(! client.isConnected()){
			return false;
		}
		
		//Play the selected card on a double tap
		int[] absPosition=new int[2];
		cardStack.getLocationOnScreen(absPosition);
		float x = e.getX()-absPosition[0];
		float y = e.getY()-absPosition[1];

		Card card = cardStack.getCardAtLocation(x, y);
		if(card != null){
			client.playCard(card);
		}
		
		return false;
	}
	
}
