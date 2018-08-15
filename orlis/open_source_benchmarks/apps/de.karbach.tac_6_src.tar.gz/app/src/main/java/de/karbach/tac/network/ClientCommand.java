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
 * Implements interface to the client object.
 * Allows to update cards and send events to the client.
 * Used by the server except for the execute command function.
 * 
 * @author Carsten Karbach
 *
 */
public class ClientCommand {

	public static final String UPDATECARDS="updatecards";
	public static final String UPDATECOLORS="updatecolors";
	public static final String ACTIVEPLAYER="activeplayer";
	public static final String ALIVE="alive";
	
	/**
	 * Separates command prefix from parameters
	 */
	public static final String sep = ":";
	
	/**
	 * Separator for multiple parameters
	 */
	public static final String subSep = ",";
	
	/**
	 * writer of the client's outputstream, which is read by the server 
	 */
	private PrintWriter pw;
	
	/**
	 * @param pw the client's outputstream, which is read by the server 
	 */
	public ClientCommand(PrintWriter pw){
		this.pw = pw;
	}
	
	
	/**
	 * Parse all commands and execute them in the client thread
	 * @param command the command which has to be parsed
	 * @param clientThread thread of the client, in which the commands are executed
	 */
	public static void executeCommand(String command, ClientThread clientThread){
		if(command.equals(ALIVE)){
			clientThread.getClient().receiveLiveTic();
			return;
		}
		
		System.out.println("Client exec "+command);
		
		//Check for commands with parameters
		String[] parts = command.split(sep);
		if(parts.length == 2){
			String prefix = parts[0];
			String parameters = parts[1];

			if(prefix.equals(UPDATECARDS)){
				System.out.println("UPDATE "+command);
				
				//Cards are updated, expected format: "UPDATECARDS:23,14,11,7",
				//where two ids are assigned to one card (drawableId and unique ID)
				String[] cardIds = parameters.split(subSep);
				int[] parsedIds = new int[cardIds.length];
				if(cardIds.length % 2 != 0){//Number of cards must be of multiple of 2
					return;
				}
				
				int[] drawableIds = new int[cardIds.length/2];
				int[] cardUniqueIds = new int[cardIds.length/2];
				
				int i=0;
				for(String id: cardIds){
					try{
						parsedIds[i] = Integer.parseInt(id);
					}
					catch(NumberFormatException e){
						return;
					}
					i++;
				}
				
				for(int j=0; j<cardIds.length; j++){
					if(j % 2 == 0){
						drawableIds[j/2] = parsedIds[j];
					}
					else{
						cardUniqueIds[j/2] = parsedIds[j];
					}
				}
				
				clientThread.getClient().getCards().setCardsByIds(drawableIds, cardUniqueIds);
			}
			else if(prefix.equals(UPDATECOLORS)){
				String[] colors = parameters.split(subSep);
				int[] parsedColors = new int[colors.length];
				
				int i=0;
				for(String id: colors){
					parsedColors[i] = Integer.parseInt(id);
					i++;
				}
				
				clientThread.getClient().setColors(parsedColors);
			}
			else if(prefix.equals(ACTIVEPLAYER)){
				try{
					int activeId = Integer.parseInt(parameters);
					clientThread.getClient().setActive(activeId);
				}
				catch(NumberFormatException e){
				}
			}
		}
		else{//No parameter added
			if(parts.length == 1){
				if( parts[0].equals(UPDATECARDS) ){
					clientThread.getClient().getCards().clear();
				}
			}
		}
	}
	
	/**
	 * Send new cards to the client. The ids are the
	 * drawable ids of the cards. It sends the drawable ID and
	 * the unique ID of each card. 
	 * E.g. UPDATECARDS+sep+drawableId+subSep+cardId...
	 * 
	 * @param cardDrawabledIds drawable ids of the updated cards
	 * @param cardIds the unique ids of these cards
	 */
	public void updateCards(int[] cardDrawabledIds, int[] cardIds){
		String idstring = "";
		for(int i=0; i<cardDrawabledIds.length; i++){
			idstring += cardDrawabledIds[i];
			idstring += subSep+cardIds[i];
			if(i<cardDrawabledIds.length-1){
				idstring += subSep;
			}
		}
		synchronized(pw){
			pw.println(UPDATECARDS+sep+idstring);
			pw.flush();
		}
	}
	
	/**
	 * Send colors for all players. This indicates, that a color switch
	 * took place.
	 * 
	 * @param colors array of integer values as colors
	 */
	public void updateColors(int[] colors){
		String colorString = "";
		for(int i=0; i<colors.length; i++){
			colorString += colors[i];
			if(i<colors.length-1){
				colorString += subSep;
			}
		}
		synchronized(pw){
			pw.println(UPDATECOLORS+sep+colorString);
			pw.flush();
		}
	}
	
	/**
	 * Tell the client, which player is currently active.
	 * 
	 * @param playerId the ID of the active player
	 */
	public void setActivePlayer(int playerId){
		synchronized(pw){
			pw.println(ACTIVEPLAYER+sep+playerId);
			pw.flush();
		}
	}
	
	/**
	 * Send an alive marker to the client.
	 */
	public void receiveLiveTic(){
		synchronized(pw){
			pw.println(ALIVE);
			pw.flush();
		}
	}
	
}
