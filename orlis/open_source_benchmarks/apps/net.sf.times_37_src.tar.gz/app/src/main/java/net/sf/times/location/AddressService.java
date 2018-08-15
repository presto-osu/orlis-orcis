/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.location;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import net.sf.times.ZmanimApplication;
import net.sf.times.location.AddressProvider.OnFindAddressListener;

/**
 * Service to find an address.
 *
 * @author Moshe Waisberg
 */
public class AddressService extends IntentService implements OnFindAddressListener {

    private static final String PARAMETER_LOCATION = ZmanimLocationListener.PARAMETER_LOCATION;
    private static final String PARAMETER_ADDRESS = ZmanimLocationListener.PARAMETER_ADDRESS;
    private static final String ADDRESS_ACTION = ZmanimLocationListener.ADDRESS_ACTION;
    private static final String ELEVATION_ACTION = ZmanimLocationListener.ELEVATION_ACTION;

    private static final String NAME = "AddressService";

    private AddressProvider addressProvider;

    /**
     * Constructs a new service.
     *
     * @param name
     *         the worker thread name.
     */
    public AddressService(String name) {
        super(name);
    }

    /**
     * Constructs a new service.
     */
    public AddressService() {
        this(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;
        Location location = extras.getParcelable(PARAMETER_LOCATION);
        if (location == null)
            return;

        final AddressProvider provider = addressProvider;
        if (provider == null)
            return;
        String action = intent.getAction();
        if (ADDRESS_ACTION.equals(action)) {
            provider.findNearestAddress(location, this);
        } else if (ELEVATION_ACTION.equals(action)) {
            provider.findElevation(location, this);
        }
    }

    @Override
    public void onFindAddress(AddressProvider provider, Location location, Address address) {
        ZmanimAddress addr = null;
        if (address != null) {
            if (address instanceof ZmanimAddress) {
                addr = (ZmanimAddress) address;
            } else {
                addr = new ZmanimAddress(address);
                if (location.hasAltitude())
                    addr.setElevation(location.getAltitude());
            }
            provider.insertOrUpdateAddress(location, addr);
        }

        Intent result = new Intent(ADDRESS_ACTION);
        result.putExtra(PARAMETER_LOCATION, location);
        result.putExtra(PARAMETER_ADDRESS, addr);
        sendBroadcast(result);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ZmanimApplication app = (ZmanimApplication) getApplication();
        addressProvider = app.getAddresses();
    }

    @Override
    public void onFindElevation(AddressProvider provider, Location location, ZmanimLocation elevated) {
        if (elevated != null) {
            provider.insertOrUpdateElevation(elevated);

            Intent result = new Intent(ELEVATION_ACTION);
            result.putExtra(PARAMETER_LOCATION, elevated);
            sendBroadcast(result);
        }
    }
}
