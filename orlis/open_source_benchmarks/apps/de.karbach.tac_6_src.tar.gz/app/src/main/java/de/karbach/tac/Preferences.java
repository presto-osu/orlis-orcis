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
package de.karbach.tac;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Shows preferences for the TAC game to the user
 */
public class Preferences extends PreferenceActivity {

	/**
	 * Key for boolean setting, whether cards are to be animated
	 */
	public static final String ANIMATION_KEY = "animate_cards";
	
	/**
	 * Key for boolean setting, whether card is played immediately
	 */
	public static final String CARDDIRECTPLAY_KEY = "play_directly";
	
	@SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Use of deprecated function here, because support library does not contain PreferenceActivity
        //See http://developer.android.com/guide/topics/ui/settings.html that PreferenceFragment is only available for API version >=11
        addPreferencesFromResource(R.xml.preferences);
    }

	
}
