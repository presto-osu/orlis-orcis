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
 * Elevation table columns.
 *
 * @author Moshe Waisberg
 */
public interface ElevationColumns extends BaseColumns {

    /** The latitude. */
    String LATITUDE = "latitude";
    /** The longitude. */
    String LONGITUDE = "longitude";
    /** The elevation / altitude. */
    String ELEVATION = "elevation";
    /** The timestamp. */
    String TIMESTAMP = "timestamp";

}
