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

package com.rastating.droidbeard.net;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchLogsTask extends SickbeardAsyncTask<Void, Void, String[]> {
    public FetchLogsTask(Context context) {
        super(context);
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        String json = getJson("logs", "min_level", "info");
        try {
            if (json != null) {
                List<String> logs = new ArrayList<String>();
                JSONArray data = new JSONObject(json).getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    logs.add(data.getString(i));
                }

                return logs.toArray(new String[logs.size()]);
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            setLastException(json, e);
            e.printStackTrace();
            return null;
        }
    }
}
