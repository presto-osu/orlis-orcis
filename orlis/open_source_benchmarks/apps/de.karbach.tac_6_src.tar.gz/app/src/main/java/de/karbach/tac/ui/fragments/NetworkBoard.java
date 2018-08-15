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

import java.util.List;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import de.karbach.tac.R;
import de.karbach.tac.core.CardManager;
import de.karbach.tac.network.Server;
import de.karbach.tac.ui.EnDisImageButton;
import de.karbach.tac.ui.NetworkBoardControl;

/**
 * This fragment is a board with network server integrated.
 * It allows others to view the board and hands out cards to
 * the players.
 * 
 * @author Carsten Karbach
 *
 */
public class NetworkBoard extends LocalBoard{

	/**
	 * Server connector for all bluetooth actions
	 */
	private Server server;
	
	/**
	 * The cardmanager used by the server to hand-out cards.
	 */
	private CardManager cardManager;

	/**
	 * Button for allowing discovery of this device.
	 */
	private EnDisImageButton discoverButton;

	/**
	 * Button for searching other devices.
	 */
	private EnDisImageButton searchButton;

	/**
	 * Is triggered for every finished device discovery
	 */
	private BroadcastReceiver discReceiver;
	
	/**
	 * Button for showing the settings for the server.
	 */
	private ImageButton settingsButton;
	
	/**
	 * Action on the cardmanager. Switch cards according to the Narr card.
	 */
	private ImageButton narrButton;

	/**
	 * Should check the button states everytime bluetooth is enabled/disabled or 
	 * the device is made discoverable
	 */
	private BroadcastReceiver stateChangedReceiver;

	public NetworkBoard(){
		super();

		layoutId = R.layout.network_board;
		
		server = new Server(this);
		cardManager = new CardManager();
		cardManager.restart();
		server.setCardManager(cardManager);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		server.setBoardData( controller.getBoardData() );
		
		addBroadcastListeners();
		
		return rootView;
	}
	
	/**
	 * Stores the settings view
	 */
	private ServerSettings settings;
	
	/**
	 * Display the server settings as a modal fragment.
	 */
	public void showSettings(){
		if(settings == null){
			settings = new ServerSettings();
		}
		settings.setServer(server);
		FragmentManager fm = getActivity().getSupportFragmentManager();
		settings.show(fm, "Settings");
	}
	
	/* (non-Javadoc)
	 * @see de.karbach.tac.TAC#initController()
	 */
	@Override
	protected void initController(){
		//Use network controller here
		NetworkBoardControl mycontroller = new NetworkBoardControl(board, this, cardManager);
		mycontroller.setServer(server);
		
		controller = mycontroller;
	}

	/**
	 * Check and adjust the states of the buttons according to the bluetooth adapter state
	 */
	protected void checkButtonStates(){
		if(! server.isDiscoverable()){
			discoverButton.setActive(false);
		}
		else{
			discoverButton.setActive(true);
		}

		if(server.isBlueToothStarted()){
			//Now make searchbutton also available
			searchButton.setActive(server.isDiscovering());
			searchButton.setEnabled(true);

			server.listenForClients();
		}
		else{
			searchButton.setActive(false);
			searchButton.setEnabled(false);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		server.onActivityResult(requestCode, resultCode, data);

		checkButtonStates();
	}
	
	@Override
	public void onStop(){
		super.onStop();
		unregisterReceivers();
	}

	@Override
	public void onDestroy (){
		super.onDestroy();
		server.disconnect();
		server.clearBlueTooth();
	}

	/**
	 * Set up listener, which makes popup menu,
	 * when devices where discovered.
	 */
	protected void addBroadcastListeners(){
		//Called if discovery is finished
		discReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				showListOfAvailableDevices();
				checkButtonStates();
			}
		};

		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getActivity().registerReceiver(discReceiver, filter);

		stateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				checkButtonStates();
			}
		};

		IntentFilter filterScan = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		getActivity().registerReceiver(stateChangedReceiver, filterScan);
		IntentFilter filterState = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		getActivity().registerReceiver(stateChangedReceiver, filterState);
	}
	
	/**
	 * Free the event receivers again.
	 */
	protected void unregisterReceivers(){
		if(discReceiver != null){
			getActivity().unregisterReceiver(discReceiver);
			discReceiver = null;
		}
		if(stateChangedReceiver != null){
			getActivity().unregisterReceiver(stateChangedReceiver);
			stateChangedReceiver = null;
		}
	}

	/**
	 * Creates a popup menu with all discovered devices.
	 * If not device was found a simple toast showing that message is shown.
	 */
	private void showListOfAvailableDevices(){
		List<String> devices = server.getDeviceNames();
		final CharSequence[] items = new CharSequence[devices.size()];
		for(int i=0; i<items.length; i++){
			items[i] = devices.get(i);
		}

		if(items.length == 0){
			Toast.makeText(getContext(), "No other device found", Toast.LENGTH_SHORT).show();
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setTitle(R.string.Devices);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.create();
			builder.show();
		}
	}

	/**
	 * Define additional actions for all bluetooth activities.
	 */
	@Override
	protected void initButtonActions(){
		super.initButtonActions();

		//Play the narr on the active cardmanager
		narrButton = (ImageButton) rootView.findViewById(R.id.playNarrButton);
		narrButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((NetworkBoardControl)controller).playNarr();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(rootView.getContext());
				builder.setTitle(R.string.cardsswitched);
				builder.setPositiveButton(R.string.ok, null);
				builder.create();
				builder.show();
			}
		});
		
		//Show server settings
		settingsButton = (ImageButton) rootView.findViewById(R.id.showSettingsButton);
		settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSettings();
			}
		});
		
		//Button to allow discovery of this device
		discoverButton = (EnDisImageButton)rootView.findViewById(R.id.discoverButton);
		discoverButton.setActive(server.isDiscoverable());

		if(server.isBlueToothStarted()){
			server.listenForClients();
		}

		discoverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(discoverButton.isActive() ){
					server.disconnect();
					//Because disabling is a fast operation, wait for it till bluetooth is actually disabled
					while(server.isBlueToothStarted()){
						server.disableBluetooth();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				}
				else{
					server.askForBluetoothDiscovering();
				}
				checkButtonStates();
			}
		});

		//Search for available bluetooth devices
		searchButton = (EnDisImageButton)rootView.findViewById(R.id.showDevButton);
		searchButton.setActive(server.isDiscovering());
		searchButton.setEnabled(server.isBlueToothStarted());
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(server.isBlueToothStarted()){
					server.discoverDevices();
					Toast.makeText(rootView.getContext(), "Searching for others", Toast.LENGTH_SHORT).show();
					//Corresponding listeners detects, when discovery is completed
					checkButtonStates();
				}
			}
		});

		//Mix all cards again, if game is restarted
	}

}
