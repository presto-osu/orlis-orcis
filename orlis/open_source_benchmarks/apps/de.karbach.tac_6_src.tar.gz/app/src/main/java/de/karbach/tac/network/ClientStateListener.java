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

/**
 * Listen for state changes for a client.
 * State changes are about connection state, active player state
 * or card states.
 * 
 * @author Carsten Karbach
 *
 */
public interface ClientStateListener {

	/**
	 * This function is called by the client, when an update on
	 * the active player is received.
	 * 
	 * @param newActive the id of the new active client.
	 */
	public void activePlayerUpdate(int newActive);
	
	/**
	 * Notifies, that the client is now playing with the passed id.
	 * 
	 * @param newId this is the new ID, which is now used by the client
	 */
	public void clientPlayerId(int newId);
	
	/**
	 * The state of the connection changed.
	 * This means the client either has connected to the server or
	 * disconnected.
	 * 
	 * @param connected true, if client connected, false otherwise
	 */
	public void connectionStateChanged(boolean connected);
	
	/**
	 * Notifies about a color change in the client.
	 * 
	 * @param colors new colors used for the players
	 */
	public void colorsChanged(int[] colors);
}
