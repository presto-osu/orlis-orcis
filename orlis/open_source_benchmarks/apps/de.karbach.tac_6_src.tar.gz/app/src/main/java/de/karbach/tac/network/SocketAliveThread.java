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

import java.util.ArrayList;
import java.util.List;

/**
 * This thread checks in a given interval, if the socket connection
 * is still connected. It notifies its listeners, if the connection
 * state changes. It either checks for a client or for a server thread.
 * It sends alive markers for the monitored client.
 * 
 * @author Carsten Karbach
 *
 */
public class SocketAliveThread extends Thread{

	/**
	 * The check interval
	 */
	private int interval = Client.aliveInterval/2;

	/**
	 * If true, the thread keeps checking the socket state,
	 * otherwise it will stop to check.
	 */
	private boolean active = true;

	/**
	 * @return true, if the thread can still be used for monitoring
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set, if the thread should monitor the delegate (client or server).
	 * 
	 * @param active true to activate monitoring, false to stop monitoring
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Listeners for state changes of the socket.
	 */
	private List<SocketStateListener> listeners;

	/**
	 * Client thread for checking alive state of the client.
	 * If clientThread is set, check if the client receives live tokens from server
	 * and send live tokens to the server
	 */
	private ClientThread clientThread;

	/**
	 * The server thread for checking alive state of server.
	 * If the serverThread is set, check if the server receives live tokens from client
	 * And send tokens to the client 
	 */
	private PlayerThread serverThread;

	/**
	 * @return The check interval in ms
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Set a new check interval in ms
	 * @param interval the new interval
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	/**
	 * Activate monitoring on client thread.
	 * 
	 * @param clientThread the client thread, which has to be checked for being alive
	 */
	public void setClientThread(ClientThread clientThread) {
		this.clientThread = clientThread;
	}

	/**
	 * Activate monitoring on server thread.
	 * 
	 * @param serverThread the server thread, which has to be checked for being alive
	 */
	public void setServerThread(PlayerThread serverThread) {
		this.serverThread = serverThread;
	}

	/**
	 * Initialization.
	 */
	public SocketAliveThread(){
		listeners = new ArrayList<SocketStateListener>();
	}

	/**
	 * Add a state listener to this thread.
	 * @param listener the new listener for state changes
	 */
	public void addListener(SocketStateListener listener){
		listeners.add(listener);
	}

	/**
	 * Remove a listener, stop it from listening.
	 * @param listener the previously added listener, which is removed now
	 */
	public void removeListener(SocketStateListener listener){
		listeners.remove(listener);
	}

	/**
	 * @return true, if the socket is still connected, false otherwise
	 */
	public boolean isConnectionAlive(){
		if(clientThread != null){
			return clientThread.getClient().isConnected();
		}
		else if(serverThread != null){
			return serverThread.isConnected();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run(){
		boolean lastState = isConnectionAlive();
		while(active){
			try{
				if(clientThread != null){
					//If clientThread is set, check if the client receives live tokens from server
					//and send live tokens to the server
					clientThread.getClient().getServerCommand().receiveLiveTic();
				}
				else if(serverThread != null){
					//If the serverThread is set, check if the server receives live tokens from client
					//And send tokens to the client 
					serverThread.getClientCommand().receiveLiveTic();
				}

				boolean newState = isConnectionAlive();
				if(newState != lastState){
					lastState = newState;
					for(SocketStateListener listener: listeners){
						listener.stateChanged(newState);
					}
				}

				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
				}

			}
			catch(Exception e){//Stop this thread, if an exception occurred, probably connection closed
				e.printStackTrace();
				break;
			}
		}

		boolean connectionState = isConnectionAlive();
		for(SocketStateListener listener: listeners){
			listener.stateChanged(connectionState);
		}

	}

}
