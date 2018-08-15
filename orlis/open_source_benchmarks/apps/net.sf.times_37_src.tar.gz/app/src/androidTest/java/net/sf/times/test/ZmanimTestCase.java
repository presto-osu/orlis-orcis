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
package net.sf.times.test;

import java.util.TimeZone;

import net.sf.times.location.ZmanimLocations;
import net.sourceforge.zmanim.util.GeoLocation;
import android.content.Context;
import android.location.Location;
import android.test.AndroidTestCase;

public class ZmanimTestCase extends AndroidTestCase {

	/**
	 * Test time zones.
	 * <p>
	 * Loop through all TZs and check that their longitude and latitude are
	 * valid.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void testTZ() throws Exception {
		Context context = getContext();
		assertNotNull(context);
		assertEquals("net.sf.times", context.getPackageName());
		ZmanimLocations locations = new ZmanimLocations(context);
		assertNotNull(locations);

		String[] ids = TimeZone.getAvailableIDs();
		assertNotNull(ids);

		TimeZone tz;
		Location loc;
		GeoLocation geoloc;
		double latitude;
		double longitude;

		for (String id : ids) {
			assertNotNull(id);
			tz = TimeZone.getTimeZone(id);
			assertNotNull(tz);

			loc = locations.getLocationTZ(tz);
			assertNotNull(loc);
			latitude = loc.getLatitude();
			assertTrue(id + " " + latitude, latitude >= -90);
			assertTrue(id + " " + latitude, latitude <= 90);
			longitude = loc.getLongitude();
			assertTrue(id + " " + longitude, longitude >= -180);
			assertTrue(id + " " + longitude, longitude <= 180);
			geoloc = new GeoLocation(id, latitude, longitude, tz);
			assertNotNull(geoloc);
		}
	}
}
