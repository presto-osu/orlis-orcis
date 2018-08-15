/*
 * APN Settings, an Android app that acts as a shortcut to the
 * APN Settings section of Android Settings
 *
 * Copyright 2015 tompreuss <tompreuss@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.tompreuss.apnsettings;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
        startActivity(intent);
    }

    /** Called when the user clicks the APN Settings button */
    public void openAPNSettings(View view) {
        Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
        startActivity(intent);
    }

}
