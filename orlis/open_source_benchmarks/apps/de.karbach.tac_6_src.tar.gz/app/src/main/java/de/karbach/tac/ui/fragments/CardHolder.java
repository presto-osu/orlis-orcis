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
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import de.karbach.tac.R;
import de.karbach.tac.network.Client;
import de.karbach.tac.network.ClientStateListener;
import de.karbach.tac.ui.CardStackControl;
import de.karbach.tac.ui.CardStackView;
import de.karbach.tac.ui.ColorToBallImage;
import de.karbach.tac.ui.EnDisImageButton;

/**
 * This activity is a cardholder with integrated bluetooth client.
 * It allows players to view their cards and play them.
 * 
 * @author Carsten Karbach
 *
 */
public class CardHolder extends Fragment{

	/**
	 * Identifier for the mac adress to save
	 */
	private static final String remoteMAC = "MAC";
	
	/**
	 * Server connector for all bluetooth actions
	 */
	private Client client;

	/**
	 * Button for searching other devices.
	 */
	private EnDisImageButton searchButton;
	
	/**
	 * Is triggered for every finished device discovery
	 */
	private BroadcastReceiver discReceiver;
	
	/**
	 * View showing the client's cards
	 */
	private CardStackView stackView;
	
	/**
	 * Controller for the stackview, react on gesture of user
	 */
	private CardStackControl stackController;
	
	/**
	 * Wrapping gesture detector around stackController
	 */
	private GestureDetector gdt;
	
	/**
	 * Shows the name of the device, to which this client connected
	 */
	private TextView connectionText;
	
	/**
	 * Show a ball with the color of the chosen player
	 */
	private EnDisImageButton playerPicture;
	
	/**
	 * Maps colors to bitmaps
	 */
	private ColorToBallImage colorToBall;
	
	/**
	 * If true, the paired devices should be shown directly, otherwise
	 * the BT client should search for other devices.
	 */
	private boolean showPairedDevices = true;
	
	public CardHolder(){
		super();
		client = new Client(this);
		client.addListener(new ClientStateListener() {
			
			@Override
			public void connectionStateChanged(boolean connected) {
				if(getActivity() != null){
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							setConnectionText();
							setPlayerImage();
						}
					});
				}
				if(! client.isConnected()){
					client.setActive(-1);
					client.getCards().clear();
				}
			}
			
			@Override
			public void clientPlayerId(int newId) {
				
			}
			
			@Override
			public void activePlayerUpdate(int newActive) {
				if(getActivity() != null){
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							setPlayerImage();
						}
					});
				}
			}

			@Override
			public void colorsChanged(int[] colors) {
				if(getActivity() != null){
					getActivity().runOnUiThread(new Runnable(){
						@Override
						public void run() {
							setPlayerImage();
						}
					});
				}
			}
		});
	}
	
	/**
	 * This function must be called by the surrounding activity
	 * so that gestures can be detected by this instance's detector.
	 * @param event the occurred motion event
	 * @return true, if the event was consumed, false otherwise
	 */
	public boolean onTouchEvent(MotionEvent event) {
		if(gdt != null ){
			gdt.onTouchEvent(event);
		}
		return false;
	}
	
	/**
	 * Set the image of the player used here.
	 */
	private void setPlayerImage(){
		int playerId = client.getID();
		boolean isActive = client.isActive();
		
		playerPicture.setActive(isActive);
		
		Bitmap playerBitmap = colorToBall.colorToBitmap(client.getColor(playerId));
		playerPicture.setImageBitmap(playerBitmap);	
		playerPicture.invalidate();
	}
	
	/**
	 * Set the text for the connection display
	 */
	private void setConnectionText(){
		String gameName = client.getGameName();
		if(gameName == null || !client.isConnected()){
			gameName = "please connect ...";
		}
		else{
			gameName = "connected to "+gameName;
		}
		
		connectionText.setText(gameName);
		
		if(client.isConnected()){
			connectionText.setBackgroundColor(Color.GREEN);
		}
		else{
			connectionText.setBackgroundColor(Color.RED);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		return inflater.inflate(R.layout.cardholder, container, false);
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
		initButtonActions();
		addBroadcastListeners();

        View rootView = getView();

        if(rootView != null) {
            //Init cardholder view
            stackView = (CardStackView) rootView.findViewById(R.id.cardstack);
            connectionText = (TextView) rootView.findViewById(R.id.connectText);
            playerPicture = (EnDisImageButton) rootView.findViewById(R.id.showPlayer);
        }

		stackController = new CardStackControl(stackView, client);
		gdt = new GestureDetector(stackView.getContext(), stackController);
		
		colorToBall = new ColorToBallImage(stackView.getContext());
		//Restore connection
		if(savedInstanceState != null){
			String deviceMAC = savedInstanceState.getString(remoteMAC);
			if(deviceMAC != null){
				clientConnect(deviceMAC);
			}
		}
		
		setConnectionText();
		setPlayerImage();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    if(client.isConnected()){
	    	outState.putString(remoteMAC, client.getRemoteMAC());
	    }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		client.onActivityResult(requestCode, resultCode, data);
		
		if(client.isBlueToothStarted()){
			if(showPairedDevices){
				showListOfAvailableDevices(client.getPairedDeviceNames());
			}
			else{
				client.discoverDevices();
				synchronized(CardHolder.this){
					searchButton.setActive(true);
				}
				//Corresponding listeners detects, when discovery is completed
			}
		}
		
		initButtonActions();
	}

	@Override
	public void onDestroy (){
		super.onDestroy();
		client.disconnect();
		client.clearBlueTooth();
		getActivity().unregisterReceiver(discReceiver);
	}
	
	/**
	 * Set up listener, which makes popup menu,
	 * when devices where discovered.
	 * 
	 * Also add listener for button states checks.
	 */
	protected void addBroadcastListeners(){
		//Called if discovery is finished
		discReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				synchronized(CardHolder.this){
					if(!searchButton.isActive()){
						return;
					}
					searchButton.setActive(false);
				}
				
				showListOfAvailableDevices(client.getDeviceNames());
			}
		};
		
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getActivity().registerReceiver(discReceiver, filter);
	}

	/**
	 * Creates a popup menu with devices to which to connect.
	 * If the list is empty or null a simple toast showing that message is shown.
	 */
	private void showListOfAvailableDevices(List<String> deviceNames){
		final CharSequence[] items = new CharSequence[deviceNames.size()];
		for(int i=0; i<items.length; i++){
			items[i] = deviceNames.get(i);
		}

		if(items.length == 0){
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setPositiveButton(R.string.ok, null);
			builder.setTitle(R.string.nootherdevice);
			builder.show();
		}
		else{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.connect_to);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					clientConnect( items[which].toString() );
				}
			});
			builder.setPositiveButton("Update", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					searchOtherDevices();
				}
			});
			builder.create();
			builder.show();
		}
	}
	
	/**
	 * Connect this client with the server bluetooth device.
	 * @param deviceName name of the remote bluetooth device, or MAC adress
	 */
	protected void clientConnect(String deviceName){
		client.connect(deviceName);
		if( client.isConnected() ){
			stackView.setCards(client.getCards());
		}
		setConnectionText();
	}

	/**
	 * Start BT discovering if necessary.
	 * Start searching for other BT devices.
	 */
	protected void searchOtherDevices(){
		if(!client.isBlueToothStarted()){
			showPairedDevices = false;
			client.askForBluetoothDiscovering();
		}
		else{
			client.discoverDevices();
			synchronized(CardHolder.this){
				searchButton.setActive(true);
			}
			//Corresponding listeners detects, when discovery is completed
		}
	}
	
	/**
	 * Ask for starting bluetooth and show paired
	 * devices once the BT is active.
	 */
	protected void startBTAndShowPairedDevices(){
		if(!client.isBlueToothStarted()){
			showPairedDevices = true;
			client.askForBluetoothDiscovering();
		}
	}
	
	/**
	 * Define actions for all bluetooth activities.
	 */
	protected void initButtonActions(){
        View rootView = getView();
        if(rootView == null){
            return;
        }
		//Search for available bluetooth devices
		searchButton = (EnDisImageButton)rootView.findViewById(R.id.showDevButton);
		searchButton.setActive(false);
		searchButton.setBlinking(true);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(searchButton.isActive()){
					client.stopDiscovering();
					searchButton.setActive(false);
				}
				else{
					if(!client.isBlueToothStarted()){
						startBTAndShowPairedDevices();
					}
					else{
						showListOfAvailableDevices(client.getPairedDeviceNames());
					}
				}
			}
		});
		
		//Player chooser
		Spinner playerSpinner = (Spinner) rootView.findViewById(R.id.playerid);
		
		playerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {

				client.setID(pos-1);
				setPlayerImage();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

}
