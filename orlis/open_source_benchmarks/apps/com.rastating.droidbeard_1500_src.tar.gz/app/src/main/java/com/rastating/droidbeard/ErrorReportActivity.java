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
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.rastating.droidbeard.net.ErrorReportTask;

import org.json.JSONObject;

public class ErrorReportActivity extends Activity implements View.OnClickListener {
    JSONObject mReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.error_report);

        mReport = new JSONObject();
        try {
            Preferences preferences = new Preferences(this);
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                String exception = extras.getString("exception");
                mReport.put("exception", exception == null ? "" : exception);
                mReport.put("stackTrace", extras.getString("stackTrace"));
                mReport.put("https_enabled", preferences.getHttpsEnabled());
                mReport.put("trust_all_certificates", preferences.getTrustAllCertificatesFlag());
                mReport.put("version", Application.getVersionName());
                mReport.put("data", extras.getString("data"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ((EditText) findViewById(R.id.exception)).setText(mReport.toString());
        findViewById(R.id.send).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        new ErrorReportTask().execute(mReport);
        finish();
    }
}