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

import com.rastating.droidbeard.entities.HistoricalEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchHistoryTask extends SickbeardAsyncTask<Void, Void, HistoricalEvent[]> {

    public FetchHistoryTask(Context context) {
        super(context);
    }

    @Override
    protected HistoricalEvent[] doInBackground(Void... voids) {
        List<HistoricalEvent> events = new ArrayList<HistoricalEvent>();
        String json = getJson("history", null);

        try {
            if (json != null) {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray results = jsonObject.optJSONArray("data");
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject data = results.getJSONObject(i);
                        HistoricalEvent event = new HistoricalEvent();
                        event.setDate(data.getString("date"));
                        event.setEpisodeNumber(data.getInt("episode"));
                        event.setProvider(data.getString("provider"));
                        event.setQuality(data.getString("quality"));
                        event.setSeason(data.getInt("season"));
                        event.setShowName(data.getString("show_name"));
                        event.setStatus(data.getString("status"));
                        events.add(event);
                    }
                }
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

        return events.toArray(new HistoricalEvent[events.size()]);
    }
}