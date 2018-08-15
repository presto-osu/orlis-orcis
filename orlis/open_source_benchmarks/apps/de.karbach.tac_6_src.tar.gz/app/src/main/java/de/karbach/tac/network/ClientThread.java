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
import java.util.Scanner;

/**
 * Thread listeing for commands from the server.
 * 
 * @author Carsten Karbach
 *
 */
public class ClientThread extends Thread{

	/**
	 * Client reference. Is expected to be connected to the server.
	 */
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	/**
	 * @return the client, which started this thread
	 */
	public Client getClient(){
		return client;
	}
	
	@Override
	public void run(){
		Scanner input;
		try {
			input = new Scanner(client.getSocket().getInputStream());
		} catch (IOException e) {
			return;
		}
		
		while(input.hasNext()){
			String command = input.nextLine();
			//parse and execute this command
			ClientCommand.executeCommand(command, this);
		}
		
		input.close();
	}
	
}
