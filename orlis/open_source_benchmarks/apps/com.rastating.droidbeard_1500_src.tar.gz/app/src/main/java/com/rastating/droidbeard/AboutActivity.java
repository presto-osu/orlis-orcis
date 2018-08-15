/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        findViewById(R.id.rastating_link).setOnClickListener(this);
        findViewById(R.id.adam_prescott_link).setOnClickListener(this);
        findViewById(R.id.reddit_link).setOnClickListener(this);
        findViewById(R.id.phyushin_link).setOnClickListener(this);
        findViewById(R.id.official_website_link).setOnClickListener(this);
        findViewById(R.id.facebook_link).setOnClickListener(this);
        findViewById(R.id.google_link).setOnClickListener(this);

        ((TextView) findViewById(R.id.version_number)).setText("Version " + Application.getVersionName());
    }

    @Override
    public void onClick(View view) {
        String uri = "";
        switch (view.getId()) {
            case R.id.rastating_link:
                uri = "https://twitter.com/iamrastating";
                break;

            case R.id.adam_prescott_link:
                uri = "https://twitter.com/Adam_Prescott";
                break;

            case R.id.reddit_link:
                uri = "http://www.reddit.com/r/sickbeard/";
                break;

            case R.id.phyushin_link:
                uri = "https://twitter.com/Phyushin";
                break;

            case R.id.official_website_link:
                uri = "http://www.droidbeard.com/";
                break;

            case R.id.facebook_link:
                uri = "https://www.facebook.com/droidbeard";
                break;

            case R.id.google_link:
                uri = "https://plus.google.com/u/0/communities/109361186281608237451";
                break;

            case R.id.dventurino_link:
                uri = "https://github.com/dventurino";
                break;

            case R.id.serramat_link:
                uri = "https://github.com/serramat";
                break;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(browserIntent);
    }
}