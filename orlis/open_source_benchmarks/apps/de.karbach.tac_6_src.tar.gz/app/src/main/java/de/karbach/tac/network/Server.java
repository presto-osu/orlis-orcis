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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import de.karbach.tac.core.BoardData;
import de.karbach.tac.core.CardManager;
import de.karbach.tac.ui.fragments.NetworkBoard;

/**
 * Implements server part of bluetooth communication.
 * Waits for clients to connect.
 * Handles each client in separate thread.
 * 
 * @author Carsten Karbach
 *
 */
public class Server extends Connector{

	/**
	 * needed to wait for new connections
	 */
	private BluetoothServerSocket mmServerSocket;

	/**
	 * If true, this server is already listening for clients
	 */
	private boolean isListening;

	/**
	 * Known cardmanager, not owned by the server
	 */
	private CardManager cardmanager;

	/**
	 * Needed to access the boarddata
	 */
	private BoardData boardData;

	/**
	 * List for all connected players.
	 * Needed to disconnect and to show a list of active players.
	 */
	private List<PlayerThread> players;

	/**
	 * Needed for disconnect
	 */
	private List<SocketAliveThread> socketAliveThreads;

	/**
	 * Number of active players expected.
	 * With only one player, the player can view all four card stacks.
	 * Two players means that each one can view two card stacks.
	 * Three means that one play can see two card stacks and the
	 * other two can view only one.
	 * Four means that every player controls a single card stack.
	 */
	private int playerCount = 1;

	/**
	 * @return the number of active players expected in the game
	 */
	public int getPlayerCount() {
		return playerCount;
	}

	/**
	 * Set the number of expected and allowed players in the game.
	 * 
	 * @param playerCount the new number of players in this game in the range of 1 to 4
	 */
	public void setPlayerCount(int playerCount) {
		if(playerCount < 1 || playerCount > 4){
			return;
		}
		this.playerCount = playerCount;
		forwardAllowedPlayerIds();
	}

	/**
	 * Teach the player threads, which Ids they are allowed to show and adapt.
	 */
	protected void forwardAllowedPlayerIds(){
		if(playerCount==1){
			if(players.size() > 0){
				Set<Integer> allowed = new HashSet<Integer>();
				for(int i=0; i< 4; i++){
					allowed.add(i);
				}
				players.get(0).setControlledPlayers(allowed);
			}
			return;
		}
		if(playerCount==2){
			if(players.size() > 0){
				Set<Integer> allowed1 = new HashSet<Integer>();
				allowed1.add(0);
				allowed1.add(2);
				players.get(0).setControlledPlayers(allowed1);
			}
			if(players.size() > 1){
				Set<Integer> allowed2 = new HashSet<Integer>();
				allowed2.add(1);
				allowed2.add(3);
				players.get(1).setControlledPlayers(allowed2);
			}
			return;
		}
		if(playerCount==3){
			if(players.size() > 0){
				Set<Integer> allowed1 = new HashSet<Integer>();
				allowed1.add(0);
				allowed1.add(2);
				players.get(0).setControlledPlayers(allowed1);
			}
			if(players.size() > 1){
				Set<Integer> allowed2 = new HashSet<Integer>();
				allowed2.add(1);
				players.get(1).setControlledPlayers(allowed2);
			}
			if(players.size() > 2){
				Set<Integer> allowed3 = new HashSet<Integer>();
				allowed3.add(3);
				players.get(2).setControlledPlayers(allowed3);
			}
			return;
		}
		if(playerCount==4){
			for(int i=0; i<4; i++){
				Set<Integer> allowed = new HashSet<Integer>();
				allowed.add(i);
				if(players.size() > i){
					players.get(i).setControlledPlayers(allowed);
				}
			}
		}

	}

	/**
	 * @param fragment the board fragment, which creates the server instance
	 */
	public Server(NetworkBoard fragment){
		super(fragment);
		isListening = false;
		players = new ArrayList<PlayerThread>();
		socketAliveThreads = new ArrayList<SocketAliveThread>();
	}

	/**
	 * Register the current board data in order to forward
	 * it to the player threads.
	 * 
	 * @param boardData new board data.
	 */
	public void setBoardData(BoardData boardData){
		if(this.boardData != null){
			for(PlayerThread player: players){
				boardData.removeListener(player);
			}
		}
		
		this.boardData = boardData;
		
		List<Integer> cols = boardData.getColors();
		
		for(PlayerThread player: players){
			boardData.addListener(player);
			player.setColors(cols);
			player.sendColorUpdate();
		}
	}

	/**
	 * Make the card manager known to the server.
	 * @param cardmanager the new cardmanager for this server
	 */
	public void setCardManager(CardManager cardmanager){
		this.cardmanager = cardmanager;
	}

	/**
	 * Traverse the list of players registered and
	 * return a list of player device names.
	 * @return list of device names of the connected players
	 */
	public List<String> getActivePlayers(){
		List<String> res = new ArrayList<String>();
		for(PlayerThread player: players){
			if(player.isConnected()){
				String playerName = player.getBTDeviceName(); 
				if(playerName != null){
					res.add(playerName);
				}
			}
		}
		return res;
	}
	
	/**
	 * Remove all playerthreads using the given BT device.
	 * 
	 * @param device the remote bluetooth device whose player threads need to be cleaned
	 */
	protected void disconnectConnectionsOfDevice(BluetoothDevice device){
		for(int i=0; i< players.size(); i++){
			PlayerThread player = players.get(i);
			if(player.getBTDevice().equals(device) ){
				player.disconnect();
				players.remove(player);
				forwardAllowedPlayerIds();
				i--;
			}
		}
	}

	/**
	 * Start to listen for new client connections.
	 */
	public void listenForClients(){

		synchronized(this){
			if(isListening){
				return;
			}
			isListening = true;
		}

		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		BluetoothServerSocket tmp = null;
		try {
			// MY_UUID is the app's UUID string, also used by the client code
			tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(COMMNAME, UUID.fromString(MYUUID));
		} catch (IOException e) { }
		mmServerSocket = tmp;

		//Start thread listening for new client connections
		Runnable run = new Runnable() {
			@Override
			public void run() {

				isListening = true;
				BluetoothSocket socket;
				// Keep listening until exception occurs
				while (isListening) {
					try {
						socket = mmServerSocket.accept();
					} catch (IOException e) {
						socket = null;
					}
					// If a connection was accepted
					if (socket != null) {
						// Do work to manage the connection (in a separate thread)
						try {
							disconnectConnectionsOfDevice(socket.getRemoteDevice());
							
							final PlayerThread player = new PlayerThread(socket, cardmanager);
							players.add(player);
							player.setColors(boardData.getColors());
							player.start();
							forwardAllowedPlayerIds();
							if(boardData != null){
								boardData.addListener(player);
							}

							SocketAliveThread socketAlive= new SocketAliveThread();
							socketAliveThreads.add(socketAlive);
							socketAlive.setServerThread(player);
							socketAlive.start();
							socketAlive.addListener(new SocketStateListener() {

								@Override
								public void stateChanged(boolean newConnectedState) {
									if(newConnectedState == false){
										player.disconnect();
										players.remove(player);
										forwardAllowedPlayerIds();
									}
								}
							});
						} catch (IOException e) {
						}
					}
				}
				isListening = false;
			}
		};

		new Thread(run).start();
	}

	/**
	 * Disconnect not only the server from operation but also all running 
	 * player threads.
	 */
	public void disconnect(){
		for(PlayerThread player : players){
			player.disconnect();
		}

		for(SocketAliveThread sat: socketAliveThreads){
			sat.setActive(false);
		}

		isListening = false;
		try {
			if(mmServerSocket != null){
				mmServerSocket.close();
			}
		} catch (IOException e) {
		}
	}

}
