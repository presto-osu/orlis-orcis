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
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;

public class Connector {
	/**
	 * Needed for enabling bluetooth
	 */
	protected Fragment fragment;

	/**
	 * Message identifier for enabling bluetooth request
	 */
	private static int REQUEST_ENABLE_BT=1;
	
	/**
	 * Duration the device should be discoverable
	 */
	private static int REQUEST_DISCOVERY_DURATION = 300;

	/**
	 * List of discovered bluetooth devices
	 */
	private List<BluetoothDevice> discoveredDevices;

	/**
	 * Finds new devices for connections
	 */
	private BroadcastReceiver bcReceiver;

	/**
	 * Allows to interact with blue tooth system
	 */
	protected BluetoothAdapter mBluetoothAdapter;
	
	/**
	 * Name of communication service
	 */
	protected static final String COMMNAME = "TAC";
	
	/**
	 * ID for the service
	 */
	protected static final String MYUUID = "decbaf00-2b77-11e3-8224-0800200c9a66";
	
	/**
	 * If true, another intent is currently active asking the user,
	 * if bluetooth may be started.
	 */
	protected boolean isAskingForBT = false;

	/**
	 * Initiates the server part. Requests to use
	 * blue tooth. The server is not usable until the bluetooth request
	 * was accepted by the application.
	 * 
	 * @param fragment the application part, which has to be used for blue tooth enabling request
	 */
	public Connector(Fragment fragment){

		this.fragment = fragment;

		discoveredDevices = new ArrayList<BluetoothDevice>();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		bcReceiver = new DeviceReceiver(this);
	}
	
	/**
	 * Asks user to start blue tooth and to be discoverable.
	 * Ask for permission only, if it is not already available.
	 */
	public void askForBluetoothDiscovering(){
		//Do not enable blue tooth twice
		if(isDiscoverable()){
			return;
		}
		//Make this device discoverable for the other connectors
		Intent discoverableIntent = new
				Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, REQUEST_DISCOVERY_DURATION);
		fragment.startActivityForResult(discoverableIntent, REQUEST_ENABLE_BT);
		
		isAskingForBT = true;
	}
	
	/**
	 * Cancel discovery of other blue tooth devices.
	 */
	public void stopDiscovering(){
		mBluetoothAdapter.cancelDiscovery();
	}
	
	/**
	 * Disable bluetooth if it was enabled before.
	 */
	public void disableBluetooth(){
		if(isBlueToothStarted()){
			mBluetoothAdapter.disable();
		}
	}
	
	/**
	 * Method called by the activity as soon as blue tooth enabling request
	 * is answered by the system.
	 * 
	 * @param requestCode  The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from. 
	 * @param resultCode  The integer result code returned by the child activity through its setResult(). 
	 * @param data  An Intent, which can return result data to the caller (various data can be attached to Intent "extras"). 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_ENABLE_BT){
			isAskingForBT = false;
			/*if(resultCode == Activity.RESULT_OK || resultCode == REQUEST_DISCOVERY_DURATION){
				//Now this device can be discovered
			}*/
		}
	}
	
	/**
	 * 
	 * @return true, if discover process is still running, false otherwise
	 */
	public boolean isDiscovering(){
		if(! isBlueToothStarted()){
			return false;
		}
		return mBluetoothAdapter.isDiscovering();
	}
	
	/**
	 * @return true, if the user is currently asked for the bluetooth activation
	 */
	public boolean isAskingForBlueTooth(){
		return isAskingForBT;
	}

	/**
	 * 
	 * @return true, if bluetooth is allowed to be used, false otherwise
	 */
	public boolean isBlueToothStarted(){
		return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
	}

	/**
	 * If true, a broadcast receiver was registered, false if nothing was registered.
	 */
	private boolean registered = false;
	
	/**
	 * Starts discovering of available blue tooth devices.
	 */
	public void discoverDevices(){
		//Can only work if bluetooth is enabled
		if(!isBlueToothStarted()){
			return;
		}

		if(!registered){
			// Register the BroadcastReceiver
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			fragment.getActivity().registerReceiver(bcReceiver, filter);

			registered = true;
		}
		
		discoveredDevices = new ArrayList<BluetoothDevice>();

		mBluetoothAdapter.startDiscovery();
	}
	
	/**
	 * Call this method on destroy event of the activity
	 */
	public void clearBlueTooth(){
		if(registered){
			fragment.getActivity().unregisterReceiver(bcReceiver);
		}
	}

	/**
	 * Add a new device to the list of devices, from which the user can choose to connect to
	 * @param device new device, which was discovered
	 */
	public void addDevice(BluetoothDevice device){
		if(discoveredDevices.contains(device)){
			return;
		}
		discoveredDevices.add(device);
	}
	
	/**
	 * 
	 * @return true, if this device is discoverable, false otherwise
	 */
	public boolean isDiscoverable(){
		return isBlueToothStarted() && mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
	}

	/**
	 * @return list of all device names, which were discovered so far.
	 */
	public List<String> getDeviceNames(){
		List<String> result = new ArrayList<String>();

		for(BluetoothDevice dev: discoveredDevices){
			String name = dev.getName();
			if(name != null){
				result.add(dev.getName());
			}
		}

		return result;
	}
	
	/**
	 * @return list of device names, to which this device is paired
	 */
	public List<String> getPairedDeviceNames(){
		List<String> result = new ArrayList<String>();
		Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
		if(paired == null){
			return result;
		}
		
		for(BluetoothDevice device: paired){
			result.add(device.getName() );
		}
		
		return result;
	}
	
	/**
	 * Finds the BT device with the given name.
	 * @param name the name of the BT device requested
	 * @return the BT device from the paired list with the passed name or null if the device is not found
	 */
	public BluetoothDevice getPairedDeviceByName(String name){
		Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
		if(paired == null){
			return null;
		}
		
		for(BluetoothDevice device: paired){
			if(device.getName().equals(name)){
				return device;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param name nice name of the bluetooth device
	 * @return null if there is no device with the passed name, or the discovered device.
	 */
	public BluetoothDevice getDeviceByName(String name){
		
		for(BluetoothDevice device : discoveredDevices){
			if(device.getName().equals(name) ){
				return device;
			}
		}
		
		return null;
	}
	
	/**
	 * @param mac the hardware adress of remote system
	 * @return the bluetooth device instance for this adress, or null, if it cannot be obtained
	 */
	public BluetoothDevice getDeviceByMAC(String mac){
		return mBluetoothAdapter.getRemoteDevice(mac);
	}

}
