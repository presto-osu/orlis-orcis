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
package net.sf.times;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import net.sf.times.location.AddressProvider;
import net.sf.times.location.ZmanimLocations;

/**
 * Zmanim application.
 *
 * @author Moshe Waisberg
 */
public class ZmanimApplication extends Application {

    /** Provider for locations. */
    private ZmanimLocations locations;
    /** Provider for addresses. */
    private AddressProvider addressProvider;

    /**
     * Constructs a new application.
     */
    public ZmanimApplication() {
    }

    /**
     * Get the locations provider instance.
     *
     * @return the provider.
     */
    public ZmanimLocations getLocations() {
        if (locations == null) {
            locations = new ZmanimLocations(this);
        }
        return locations;
    }

    /**
     * Get the addresses provider instance.
     *
     * @return the provider.
     */
    public AddressProvider getAddresses() {
        if (addressProvider == null) {
            addressProvider = new AddressProvider(this);
        }
        return addressProvider;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        SQLiteDatabase.releaseMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        if (addressProvider != null) {
            addressProvider.close();
        }
        if (locations != null) {
            locations.quit();
        }
        super.onTerminate();
    }
}
