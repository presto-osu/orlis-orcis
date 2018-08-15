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

package de.karbach.tac.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import de.karbach.tac.R;
import de.karbach.tac.network.Server;

/**
 * UI part showing settings for a network TAC server.
 * Allows to adjust the settings.
 */
public class ServerSettings extends DialogFragment{

	/**
	 * Network server reference for adjusting its setting.
	 */
	private Server server;

	/**
	 * The view managed by this fragment.
	 */
	private View myView;
 
	/**
	 * Names of player devices
	 */
	private List<String> players = new ArrayList<String>();

	/**
	 * Model for the listview used to display the player names
	 */
	private ArrayAdapter<String> playerData;

	/**
	 * Init the server managed by this fragment.
	 * This needs to happen before the fragment is actually used.
	 * @param server the managed server
	 */
	public void setServer(Server server){
		this.server = server;
		setPlayers(server.getActivePlayers());
	}

	/**
	 * Set the list of currently active players.
	 * @param players list of players' devices
	 */
	public void setPlayers(List<String> players){
		this.players = players;
		forwardPlayersToAdapter();
	}
	
	/**
	 * Forward the list of players to the player adapter.
	 * If the playerData adapter is not yet created, this
	 * function does not have any effect.
	 */
	protected void forwardPlayersToAdapter(){
		if(playerData == null){
			return;
		}
		playerData.clear();
		for(String player: players){
			playerData.add(player);
		}
		playerData.notifyDataSetChanged();
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();

		forwardPlayersToAdapter();
		
		//Show player number expected in the game
		RadioButton toSelect = null;

		switch(server.getPlayerCount()){
		case 1: toSelect = (RadioButton)myView.findViewById(R.id.player1); break;
		case 2: toSelect = (RadioButton)myView.findViewById(R.id.player2); break;
		case 3: toSelect = (RadioButton)myView.findViewById(R.id.player3); break;
		case 4: toSelect = (RadioButton)myView.findViewById(R.id.player4); break;
		}

		if(toSelect != null){
			toSelect.setChecked(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myView = inflater.inflate(R.layout.server_setting, container, false);

		//Show active players
		ListView playerList = (ListView) myView.findViewById(R.id.connectedPlayers);
		if(playerData == null){
			playerData = new ArrayAdapter<String>(myView.getContext() , 
					android.R.layout.simple_list_item_1, new ArrayList<String>());
		}
		playerList.setAdapter(playerData);
		forwardPlayersToAdapter();

		RadioGroup count = (RadioGroup)myView.findViewById(R.id.playerGroup);
		count.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId){
				case R.id.player1: server.setPlayerCount(1); break;
				case R.id.player2: server.setPlayerCount(2); break;
				case R.id.player3: server.setPlayerCount(3); break;
				case R.id.player4: server.setPlayerCount(4); break;
				}
			}
		});

		return myView;
	}

}
