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

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

public interface ZmanimLocationListener extends LocationListener {

    /** The location parameter. */
    String PARAMETER_LOCATION = LocationManager.KEY_LOCATION_CHANGED;
    /** The address parameter. */
    String PARAMETER_ADDRESS = "address";
    /** The intent action for an address that was found. */
    String ADDRESS_ACTION = "net.sf.times.location.ADDRESS";
    /** The intent action for a location with elevation that was found. */
    String ELEVATION_ACTION = "net.sf.times.location.ELEVATION";

    /**
     * Called when an address is found.
     *
     * @param location
     *         the requested location.
     * @param address
     *         the address for the location.
     */
    void onAddressChanged(Location location, ZmanimAddress address);

    /**
     * Called when an address is found.
     *
     * @param location
     *         the location with elevation.
     */
    void onElevationChanged(Location location);
}
