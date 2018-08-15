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

import com.rastating.droidbeard.comparators.UpcomingEpisodeComparator;
import com.rastating.droidbeard.entities.UpcomingEpisode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FetchUpcomingEpisodesTask extends SickbeardAsyncTask<Void, Void, UpcomingEpisode[]> {
    public FetchUpcomingEpisodesTask(Context context) {
        super(context);
    }

    @Override
    protected UpcomingEpisode[] doInBackground(Void... voids) {
        List<UpcomingEpisode> episodes = new ArrayList<UpcomingEpisode>();
        String json = getJson("future", null);

        try {
            if (json != null) {
                JSONObject data = new JSONObject(json).getJSONObject("data");
                JSONArray missed = data.optJSONArray("missed");
                JSONArray today = data.optJSONArray("today");
                JSONArray soon = data.optJSONArray("soon");
                JSONArray later = data.optJSONArray("later");

                processEpisodes(missed, UpcomingEpisode.UpcomingEpisodeStatus.PAST, episodes);
                processEpisodes(today, UpcomingEpisode.UpcomingEpisodeStatus.CURRENT, episodes);
                processEpisodes(soon, UpcomingEpisode.UpcomingEpisodeStatus.FUTURE, episodes);
                processEpisodes(later, UpcomingEpisode.UpcomingEpisodeStatus.DISTANT, episodes);
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

        Collections.sort(episodes, new UpcomingEpisodeComparator());
        return episodes.toArray(new UpcomingEpisode[episodes.size()]);
    }

    private void processEpisodes(JSONArray data, UpcomingEpisode.UpcomingEpisodeStatus status, List<UpcomingEpisode> episodes) throws JSONException {
        if (data != null) {
            for (int i = 0; i < data.length(); i++) {
                JSONObject episodeData = data.getJSONObject(i);
                UpcomingEpisode episode = new UpcomingEpisode();
                episode.setAirdate(episodeData.getString("airdate"));
                episode.setName(episodeData.getString("ep_name"));
                episode.setEpisodeNumber(episodeData.getInt("episode"));
                episode.setSeasonNumber(episodeData.getInt("season"));
                episode.setShowName(episodeData.getString("show_name"));
                episode.setTVDBID(episodeData.getInt("tvdbid"));
                episode.setUpcomingStatus(status);
                episodes.add(episode);
            }
        }
    }
}