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

package de.karbach.tac.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardActionListener;
import de.karbach.tac.core.CardManager;
import de.karbach.tac.core.CardStack;
import de.karbach.tac.core.DataChangeEvent;
import de.karbach.tac.core.CardStack.CardEventType;
import de.karbach.tac.core.DataChangeEvent.ChangeType;
import de.karbach.tac.core.DataChangeListener;

/**
 * Thread handling exactly one connection to a client
 * This thread is run by the server.
 * Implements request - response protocol.
 * 
 * @author Carsten Karbach
 */
public class PlayerThread extends Thread implements DataChangeListener{

	/**
	 * The client socket handled by this thread
	 */
	private BluetoothSocket socket;
	
	/**
	 * A timestamp holding the last time, when an alive tic was send from the client.
	 */
	private long lastAliveTic;
	
	/**
	 * Id of the player connected to this thread and working over this thread.
	 */
	private int playerId = -1;
	
	/**
	 * Stores the id, which was requested the last time the client has sent
	 * a player id request.
	 */
	private int requestedPlayerId = -1;
	
	/**
	 * Reference to cardmanager
	 */
	private CardManager manager;
	
	/**
	 * Printwriter connected to the socket.
	 */
	private PrintWriter pw;
	
	/**
	 * Interface to any actions with the client
	 */
	private ClientCommand clientCommand;
	
	/**
	 * Array with colors after the last data update.
	 */
	private int[] lastKnownColors;
	
	/**
	 * Stores the Ids of the players which can be controlled via this 
	 * thread. Clients can only set their player Ids to one of the Ids
	 * contained in this set. 
	 */
	private Set<Integer> controlledPlayers;
	
	/**
	 * Start listening for client requests.
	 * @param socket connected bluetooth socket
	 * @param manager card manager
	 * @throws IOException if outputstream cannot be attached
	 */
	public PlayerThread( BluetoothSocket socket, CardManager manager) throws IOException{
		this.socket = socket;
		this.manager = manager;
		
		pw = new PrintWriter(socket.getOutputStream());
		clientCommand = new ClientCommand(pw);
		
		manager.addCardListener(new CardActionListener() {
			@Override
			public void onCardAction(CardEventType type) {
				sendUpdatedCards();
				sendActivePlayer();
			}
		});
		
		receiveLiveTic();
		setColors(null);//Init empty color list
		sendActivePlayer();
		
		controlledPlayers = new HashSet<Integer>();
	}
	
	/**
	 * Allow this thread to control the player cards with the Ids contained
	 * in the passed set.
	 * 
	 * @param allowed set of allowed player Ids for this thread
	 */
	public void setControlledPlayers(Set<Integer> allowed){
		controlledPlayers.clear();
		controlledPlayers.addAll(allowed);
		//Send update to the client
		setPlayerId(requestedPlayerId);
	}
	
	/**
	 * Set the player colors on the startup of the client thread.
	 * 
	 * @param colors list of player colors, each color represented by an integer value
	 */
	public void setColors(List<Integer> colors){
		if(colors == null){
			lastKnownColors = new int[0];
			return;
		}
		int[] colarray = new int[colors.size()];
		for(int i=0; i< colors.size(); i++){
			colarray[i] = colors.get(i);
		}
		lastKnownColors = colarray;
	}
	
	
	
	/**
	 * Set id for this player.
	 * The id is in the range of 0 to 3
	 * @param id the player's id
	 */
	public void setPlayerId(int id){
		requestedPlayerId = id;
		if(this.controlledPlayers.contains(id)){
			playerId = id;
		}
		else{
			playerId = -1;
		}
		
		sendUpdatedCards();
		sendActivePlayer();
	}
	
	/**
	 * Send current cards to the connected client
	 */
	public void sendUpdatedCards(){
		CardStack stack = manager.getPlayerCards(playerId);
		
		if(stack == null){
			stack = new CardStack();
		}
		
		//Send updated cards to the client
		clientCommand.updateCards(stack.getCardDrawableIds(), stack.getUniqueIds());
	}
	
	/**
	 * Send the latest version of colors to the attached client
	 */
	public void sendColorUpdate(){
		clientCommand.updateColors(lastKnownColors);
	}
	
	/**
	 * Send the currently active player to the client.
	 */
	public void sendActivePlayer(){
		clientCommand.setActivePlayer(manager.getActivePlayer());
	}
	
	/**
	 * @return the card manager connected to this thread
	 */
	public CardManager getCardManager(){
		return manager;
	}
	
	@Override
	public void run() {
		Scanner input;
		try {
			input = new Scanner(socket.getInputStream());
		} catch (IOException e) {
			return;
		}
		while(input.hasNext()){
			//Parse all available commands.
			String command = input.nextLine();
			ServerCommand.executeCommand(command, this);
		}
		
		input.close();
	}
	
	/**
	 * A live tic was send from the client.
	 */
	public void receiveLiveTic(){
		lastAliveTic = System.currentTimeMillis();
	}
	
	/**
	 * @return true, if the connection is still active, false otherwise
	 */
	protected boolean isConnectionAlive(){
		return System.currentTimeMillis()-lastAliveTic <= Client.aliveInterval; 
	}
	
	/**
	 * @return true, if this connection is still active
	 */
	public boolean isConnected(){
		return socket != null && isConnectionAlive();
	}
	
	/**
	 * @return access to the client's interface
	 */
	public ClientCommand getClientCommand(){
		return clientCommand;
	}
	
	/**
	 * Disconnect this thread from the client.
	 */
	public void disconnect(){
		if(socket != null){
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void onDataChanged(DataChangeEvent event) {
		Object data = event.getData();
		if(event.getEventType() == ChangeType.colorSwitch || event.getEventType() == ChangeType.colorInit ){
			//Tell clients about new colors
			if(data instanceof BoardData){
				BoardData boardData = (BoardData) data;
				setColors(boardData.getColors());
				clientCommand.updateColors(lastKnownColors);
			}
		}
	}
	
	/**
	 * Play the card with the given unique ID.
	 * @param id the unique ID of the card to play
	 */
	public void playCard(int id){
		CardStack cards = manager.getPlayerCards(playerId);
		if(cards == null){
			return;
		}
		int cardRelId = 0;
		for(Card card: cards.getCards()){
			if(card.getId() == id){
				manager.playCard(playerId, cardRelId);
				return;
			}
			cardRelId++;
		}
	}
	
	/**
	 * Retrieve the name of the client BT device connected with this thread.
	 * Does not work, if the connection is broken. Then null is returned.
	 * @return the name of the bluetooth device used for this thread
	 */
	public String getBTDeviceName(){
		if(socket == null || socket.getRemoteDevice() == null){
			return null;
		}
		return socket.getRemoteDevice().getName();
	}
	
	/**
	 * If this thread is not connected, null is returned.
	 * @return the remote BT device
	 */
	public BluetoothDevice getBTDevice(){
		if(socket == null){
			return null;
		}
		return socket.getRemoteDevice();
	}
	
}
