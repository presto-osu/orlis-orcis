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

import android.provider.BaseColumns;

/**
 * Address table columns.
 *
 * @author Moshe Waisberg
 */
public interface AddressColumns extends BaseColumns {

    /** The location's latitude. */
    String LOCATION_LATITUDE = "loc_latitude";
    /** The location's longitude. */
    String LOCATION_LONGITUDE = "loc_longitude";
    /** The latitude. */
    String LATITUDE = "latitude";
    /** The longitude. */
    String LONGITUDE = "longitude";
    /** The formatted name. */
    String ADDRESS = "address";
    /** The language. */
    String LANGUAGE = "language";
    /** The timestamp. */
    String TIMESTAMP = "timestamp";
    /** Is favourite address? */
    String FAVORITE = "favorite";

}
