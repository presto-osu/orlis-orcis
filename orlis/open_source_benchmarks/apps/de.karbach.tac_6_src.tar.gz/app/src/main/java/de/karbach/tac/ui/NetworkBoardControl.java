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

import android.support.v4.app.Fragment;
import de.karbach.tac.core.CardManager;
import de.karbach.tac.network.Server;

/**
 * Extends the board controller for the network functionality of
 * the NetworkBoard. E.g. mixes cards on restart of the game.
 * 
 * @author Carsten Karbach
 *
 */
public class NetworkBoardControl extends BoardControl{

	/**
	 * Manages all cards in the game.
	 * Is instantiated elsewhere.
	 */
	protected CardManager cardmanager;
	
	/**
	 * Reference to the network server.
	 */
	protected Server serverRef;
	
	/**
	 * Init the controller with the board and surrounding fragment.
	 * 
	 * @param board the controlled board
	 * @param fragment the surrounding fragment
	 * @param manager the cardmanager created somewhere else, only managed here
	 */
	public NetworkBoardControl(Board board, Fragment fragment, CardManager manager) {
		super(board, fragment);
		
		cardmanager = manager;
		
		if(board instanceof BoardWithCards){
			BoardWithCards boardView = (BoardWithCards) board;
			boardView.setCardStack(cardmanager.getPlayedCards());
		}
	}
	
	/**
	 * Make reference to the server available to this controller.
	 * 
	 * @param server the server instance used here, especially on restart
	 */
	public void setServer(Server server){
		serverRef = server;
	}
	
	/**
	 * @return the manager of the game's cards
	 */
	public CardManager getCardManager(){
		return cardmanager;
	}
	
	/**
	 * Restart the game. Delete history.
	 */
	@Override
	protected void realRestart(){
		super.realRestart();
		cardmanager.restart();
		if(board instanceof BoardWithCards){
			BoardWithCards boardView = (BoardWithCards) board;
			boardView.setCardStack(cardmanager.getPlayedCards());
		}
		
		if(serverRef != null){
			serverRef.setBoardData(this.data);
		}
	}
	
	/**
	 * Play the Narr on the card manager.
	 */
	public void playNarr(){
		if(cardmanager == null){
			return;
		}
		cardmanager.playNarr();
	}

}
