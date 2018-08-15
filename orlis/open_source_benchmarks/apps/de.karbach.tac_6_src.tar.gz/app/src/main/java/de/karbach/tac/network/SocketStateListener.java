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
 * Interface for state change notifications on a socket.
 * 
 * @author Carsten Karbach
 *
 */
public interface SocketStateListener {
	
	/**
	 * Notifies about a connection state change.
	 * 
	 * @param newConnectedState if true, the state of the socket is now connected, otherwise disconnected
	 */
	public void stateChanged(boolean newConnectedState);

}
