/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat;

import android.content.ContentValues;
import android.database.Cursor;

import org.pixmob.freemobile.netstat.content.NetstatContract.Events;

/**
 * Network event.
 * @author Pixmob
 */
public class Event {
    public long timestamp;
    public boolean screenOn;
    public boolean wifiConnected;
    public boolean mobileConnected;
    public String mobileOperator;
    public int mobileNetworkType;
    public int batteryLevel;
    public boolean powerOn;
    public boolean femtocell;
    public boolean firstInsert;

    public Event() {}

    public Event(Event e) {
        timestamp = e.timestamp;
        screenOn = e.screenOn;
        wifiConnected = e.wifiConnected;
        mobileConnected = e.mobileConnected;
        mobileOperator = e.mobileOperator;
        mobileNetworkType = e.mobileNetworkType;
        batteryLevel = e.batteryLevel;
        powerOn = e.powerOn;
        femtocell = e.femtocell;
        firstInsert = e.firstInsert;
    }

    /**
     * Read an {@link Event} instance from a database {@link Cursor}. The cursor
     * should include every columns defined in {@link Events}.
     */
    public void read(Cursor c) {
        timestamp = c.getLong(c.getColumnIndexOrThrow(Events.TIMESTAMP));
        screenOn = c.getInt(c.getColumnIndexOrThrow(Events.SCREEN_ON)) == 1;
        wifiConnected = c.getInt(c.getColumnIndexOrThrow(Events.WIFI_CONNECTED)) == 1;
        mobileConnected = c.getInt(c.getColumnIndexOrThrow(Events.MOBILE_CONNECTED)) == 1;
        mobileOperator = c.getString(c.getColumnIndexOrThrow(Events.MOBILE_OPERATOR));
        if (mobileOperator != null) {
            mobileOperator = mobileOperator.intern();
        }
        mobileNetworkType = c.getInt(c.getColumnIndexOrThrow(Events.MOBILE_NETWORK_TYPE));
        batteryLevel = c.getInt(c.getColumnIndexOrThrow(Events.BATTERY_LEVEL));
        powerOn = c.getInt(c.getColumnIndexOrThrow(Events.POWER_ON)) == 1;
        femtocell = c.getInt(c.getColumnIndexOrThrow(Events.FEMTOCELL)) == 1;
        firstInsert = c.getInt(c.getColumnIndexOrThrow(Events.FIRST_INSERT)) == 1;
    }

    /**
     * Fill a {@link ContentValues} instance with values from this instance.
     */
    public void write(ContentValues values) {
        values.put(Events.TIMESTAMP, timestamp);
        values.put(Events.SCREEN_ON, screenOn ? 1 : 0);
        values.put(Events.WIFI_CONNECTED, wifiConnected ? 1 : 0);
        values.put(Events.MOBILE_CONNECTED, mobileConnected ? 1 : 0);
        values.put(Events.MOBILE_OPERATOR, mobileOperator);
        values.put(Events.MOBILE_NETWORK_TYPE, mobileNetworkType);
        values.put(Events.BATTERY_LEVEL, batteryLevel);
        values.put(Events.POWER_ON, powerOn ? 1 : 0);
        values.put(Events.FEMTOCELL, femtocell ? 1 : 0);
        values.put(Events.FIRST_INSERT, firstInsert ? 1 : 0);
    }

	@Override
	public String toString() {
		return "Event [timestamp=" + timestamp + ", screenOn=" + screenOn
				+ ", wifiConnected=" + wifiConnected + ", mobileConnected="	+ mobileConnected
				+ ", mobileOperator=" + mobileOperator + ", mobileNetworkType=" + mobileNetworkType
				+ ", batteryLevel=" + batteryLevel + ", powerOn=" + powerOn
				+ ", femtocell=" + femtocell + ", firstInsert=" + firstInsert + "]";
	}
    
}
