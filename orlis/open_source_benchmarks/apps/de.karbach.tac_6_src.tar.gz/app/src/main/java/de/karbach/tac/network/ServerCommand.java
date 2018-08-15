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

import java.io.PrintWriter;

/**
 * Defines strings for all commands, which can be send from client to server.
 * Commands are prefixed with the commands defined in this class.
 * Instances of this class are created in the client.
 * The server uses the static executeCommand function.
 * The normal functions are used by the client.
 * 
 * @author Carsten Karbach
 *
 */
public class ServerCommand {

	public static final String SENDID="sendid";
	public static final String REQUESTCARDS="getcards";
	public static final String ACTIVEPLAYER="getactive";
	public static final String ALIVE = "alive";
	public static final String REQUESTCOLORS = "getcolors";
	public static final String PLAYCARD = "playcard";
	
	/**
	 * Separates command prefix from parameters
	 */
	public static final String sep = ":";
	
	/**
	 * writer of the client's outputstream, which is read by the server 
	 */
	private PrintWriter pw;
	
	/**
	 * @param pw the client's outputstream, which is read by the server 
	 */
	public ServerCommand(PrintWriter pw){
		this.pw = pw;
	}
	
	
	/**
	 * Parse all commands and execute them in the player thread
	 * @param command the command which has to be parsed
	 * @param player the corresponding thread of the player
	 */
	public static void executeCommand(String command, PlayerThread player){
		//Check for simple commands without parameters
		if(command.equals(REQUESTCARDS)){
			player.sendUpdatedCards();
			return;
		}
		
		if(command.equals(REQUESTCOLORS)){
			player.sendColorUpdate();
			return;
		}
		
		if(command.equals(ALIVE)){
			player.receiveLiveTic();
			return;
		}
		
		//Check for commands with parameters
		String[] parts = command.split(sep);
		if(parts.length == 2){
			String prefix = parts[0];
			String parameters = parts[1];

			if(prefix.equals(SENDID)){//Is only a player's ID send?
				int id = Integer.parseInt(parameters);
				player.setPlayerId(id);
			}
			
			if(prefix.equals(PLAYCARD)){
				try{
					int cardId = Integer.parseInt(parameters);
					player.playCard(cardId);
				}
				catch(NumberFormatException e){
					return;
				}
			}
		}
	}
	
	/**
	 * @param myId the id, which is used by the player
	 */
	public void sendId(int myId){
		synchronized(pw){
			pw.println(SENDID+sep+myId);
			pw.flush();
		}
	}
	
	/**
	 * Asynchronous request for the player's cards
	 */
	public void requestCards(){
		synchronized(pw){
			pw.println(REQUESTCARDS);
			pw.flush();
		}
	}
	
	/**
	 * The currently active player is requested by a client
	 */
	public void requestActivePlayer(){
		synchronized(pw){
			pw.println(ACTIVEPLAYER);
			pw.flush();
		}
	}
	
	/**
	 * Send an live marker to the server.
	 */
	public void receiveLiveTic(){
		synchronized(pw){
			pw.println(ALIVE);
			pw.flush();
		}
	}
	
	/**
	 * Request for an update of the colors used by the players.
	 */
	public void requestColors(){
		synchronized(pw){
			pw.println(REQUESTCOLORS);
			pw.flush();
		} 
	}
	
	/**
	 * Allows the client to play a card with the given unique ID.
	 * 
	 * @param id the unique ID of the card
	 */
	public void playCard(int id){
		synchronized(pw){
			pw.println(PLAYCARD+sep+id);
			pw.flush();
		} 
	}
	
}
