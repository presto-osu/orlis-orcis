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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import de.karbach.tac.core.Card;
import de.karbach.tac.core.CardStack;

/**
 * Uses bluetooth connection to connect to a server.
 * 
 * @author Carsten Karbach
 *
 */
public class Client extends Connector{

	/**
	 * Socket for the direct server connection
	 */
	private BluetoothSocket socket = null;
	
	/**
	 * Time between two alive tics before the connection is estimated
	 * to be disconnected.
	 */
	public static final int aliveInterval = 4000;
	
	/**
	 * A timestamp holding the last time, when an alive tic was send from the server.
	 */
	private long lastAliveTic;
	
	/**
	 * Printwriter connected to outputstream to the server
	 */
	private PrintWriter pw;
	
	/**
	 * Allows to interact with the server
	 */
	private ServerCommand servercommand;
	
	/**
	 * Stack of cards for this client.
	 */
	private CardStack cards;
	
	/**
	 * ID of this player in the game
	 */
	private int playerId;
	
	/**
	 * Name of the device, to which this client connected successfully.
	 * If no connection is available, this is null.
	 */
	private String remoteDeviceName;
	
	/**
	 * hardware address of remote device
	 */
	private String remoteMAC;
	
	/**
	 * If true, this player is currently active.
	 */
	private boolean active;
	
	/**
	 * ID of the currently active player
	 */
	private int activePlayer;
	
	/**
	 * Listeners for the state of this client.
	 */
	private List<ClientStateListener> clientListeners;
	
	/**
	 * List of integer colors for the different players.
	 * The first color is the color of the first player and so forth.
	 */
	private List<Integer> colors;
	
	/**
	 * Check thread for activeness of a connected socket
	 */
	private SocketAliveThread socketAlive;
	
	/**
	 * @param fragment
	 */
	public Client(Fragment fragment){
		super(fragment);
		
		cards = new CardStack();
		clientListeners = new ArrayList<ClientStateListener>();
		colors = new ArrayList<Integer>();
		colors.add(Color.RED);
		colors.add(Color.GREEN);
		colors.add(Color.BLUE);
		colors.add(Color.BLACK);
		playerId = -1;
	}
	
	/**
	 * @return the socket, to which the client is connected
	 */
	public BluetoothSocket getSocket(){
		return socket;
	}
	
	/**
	 * Connect the client to a bluetooth server.
	 * @param server the name of the device as discovered via bluetooth, or its MAC adress
	 */
	public void connect(String remoteDeviceName){
		
		BluetoothDevice server = getDeviceByName(remoteDeviceName);
		if(server == null){
			server = getPairedDeviceByName(remoteDeviceName);
		}
		if(server == null){
			server = getDeviceByMAC(remoteDeviceName);
		}
		
		//Close existing connection
		if(socket != null){
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		socket = null;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			stopDiscovering();
			// MY_UUID is the app's UUID string, also used by the server code
			socket = server.createRfcommSocketToServiceRecord(UUID.fromString(MYUUID));
			socket.connect();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}

		//Connect input and output streams
		try {
			pw = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		//Here input stream is connected
		ClientThread clientThread = new ClientThread(this);
		clientThread.start();
		
		socketAlive= new SocketAliveThread();
		socketAlive.addListener(new SocketStateListener() {
			
			@Override
			public void stateChanged(boolean newConnectedState) {
				if(newConnectedState == false){
					//Disconnect the client, if the socket is not alive anymore
					disconnect();
				}
				for(ClientStateListener listener: clientListeners){
					listener.connectionStateChanged(newConnectedState);
				}
			}
		});
		socketAlive.setClientThread(clientThread);
		socketAlive.setInterval(aliveInterval/2);
		socketAlive.start();
		
		
		servercommand = new ServerCommand(pw);
		this.remoteDeviceName = server.getName();
		this.remoteMAC = server.getAddress();
		
		receiveLiveTic();
		for(ClientStateListener listener: clientListeners){
			listener.connectionStateChanged(true);
		}
		
		servercommand.sendId(getID());
		servercommand.requestColors();
	}
	
	/**
	 * Disconnects from the socket.
	 */
	public void disconnect(){
		if(socketAlive != null){
			socketAlive.setActive(false);
		}
		if(! isConnected()){
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * @return the card stack for this client
	 */
	public CardStack getCards(){
		return cards;
	}
	
	/**
	 * @return true, if this client is connected to the server
	 */
	public boolean isConnected(){
		return socket != null && isConnectionAlive();
	}
	
	/**
	 * @return get interface to the connected server
	 */
	public ServerCommand getServerCommand(){
		return servercommand;
	}
	
	/**
	 * Store the current ID of this player client
	 * and send the ID to the server.
	 * 
	 * @param id the new ID of the client, between 0 and 3
	 */
	public void setID(int id){
		this.playerId = id;
		if(isConnected()){
			servercommand.sendId(id);
			servercommand.requestColors();
		}
		
		for(ClientStateListener listener: clientListeners){
			listener.clientPlayerId(id);
		}
	}
	
	/**
	 * @return current ID of this player, or -1 if none is set so far
	 */
	public int getID(){
		return this.playerId;
	}
	
	/**
	 * @return a name for the server game
	 */
	public String getGameName(){
		if(! isConnected() ){
			return null;
		}
		
		return remoteDeviceName;
	}
	
	/**
	 * @return the hardware adress of the last device, to which this client connected successfully, 
	 * or null, if there was no connection so far
	 */
	public String getRemoteMAC(){
		return remoteMAC;
	}
	
	/**
	 * @return true, if the client is the active player, false otherwise
	 */
	public boolean isActive(){
		return active;
	}
	
	/**
	 * @return the ID of the active player
	 */
	public int getActivePlayer(){
		return activePlayer;
	}
	
	/**
	 * @param playerId the ID of the active player
	 */
	public void setActive(int playerId){
		this.activePlayer = playerId;
		active = playerId == getID();
		//Tell the listeners about the new player id
		for(ClientStateListener listener: clientListeners){
			listener.activePlayerUpdate(playerId);
		}
	}

	/**
	 * Add a new listener listening for the state changes of this client.
	 * 
	 * @param listener the new listener.
	 */
	public void addListener(ClientStateListener listener){
		clientListeners.add(listener);
	}
	
	/**
	 * Stop listening for the passed listener.
	 * 
	 * @param listener the listener, which has to be removed
	 */
	public void removeListener(ClientStateListener listener){
		clientListeners.remove(listener);
	}
	
	/**
	 * A live tic was send from the server.
	 */
	public void receiveLiveTic(){
		lastAliveTic = System.currentTimeMillis();
	}
	
	/**
	 * @return true, if the connection is still active, false otherwise
	 */
	protected boolean isConnectionAlive(){
		return System.currentTimeMillis()-lastAliveTic <= aliveInterval; 
	}
	
	/**
	 * @param playId the ID of the player, whose color is requested
	 * @return color value for the player with the passed ID
	 */
	public int getColor(int playId){
		if(playId < 0 || playId>= colors.size()){
			return 0;
		}
		
		return colors.get(playId);
	}
	
	/**
	 * Set new colors for the players.
	 * E.g. a color switch was done.
	 * 
	 * @param colors list of 4 colors
	 */
	public void setColors(int[] colors){
		if(colors != null && colors.length == 4){
			this.colors.clear();
			for(int i=0; i< colors.length; i++){
				this.colors.add(colors[i]);
			}
		}
		
		for(ClientStateListener listener: clientListeners){
			listener.colorsChanged(colors);
		}
	}
	
	/**
	 * Let this client play the passed card.
	 * It is expected that the passed card is part of the client's current
	 * card stack.
	 * 
	 * @param card the card, which should be played
	 */
	public void playCard(Card card){
		servercommand.playCard(card.getId());
	}
}
