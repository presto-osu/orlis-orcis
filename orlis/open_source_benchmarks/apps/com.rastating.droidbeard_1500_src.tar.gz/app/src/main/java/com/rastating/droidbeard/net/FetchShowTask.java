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
import android.graphics.Bitmap;
import android.util.Pair;

import com.rastating.droidbeard.comparators.SeasonComparator;
import com.rastating.droidbeard.entities.Episode;
import com.rastating.droidbeard.entities.Season;
import com.rastating.droidbeard.entities.TVShow;
import com.rastating.droidbeard.entities.Language;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FetchShowTask extends SickbeardAsyncTask<Long, Void, TVShow> {

    public FetchShowTask(Context context) {
        super(context);
    }

    private List<Season> getSeasons(long tvdbid) {
        ArrayList<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
        params.add(new Pair<String, Object>("tvdbid", tvdbid));

        try {
            String json = getJson("show.seasons", params);
            if (json != null && !json.equals("")) {
                JSONObject data = new JSONObject(json).getJSONObject("data");
                List<Season> seasons = new ArrayList<Season>();
                Iterator<String> seasonKeys = data.keys();
                while (seasonKeys.hasNext()) {
                    String seasonKey = (String) seasonKeys.next();
                    JSONObject seasonData = data.getJSONObject(seasonKey);
                    Iterator<String> episodeKeys = seasonData.keys();
                    Season season = new Season();
                    season.setSeasonNumber(Integer.valueOf(seasonKey));
                    season.setTVDBID(tvdbid);

                    while (episodeKeys.hasNext()) {
                        String episodeKey = episodeKeys.next();
                        JSONObject episodeData = seasonData.getJSONObject(episodeKey);
                        Episode episode = new Episode();
                        episode.setEpisodeNumber(Integer.valueOf(episodeKey));
                        episode.setAirdate(episodeData.getString("airdate"));
                        episode.setName(episodeData.getString("name"));
                        episode.setQuality(episodeData.getString("quality"));
                        episode.setStatus(episodeData.getString("status"));
                        episode.setSeasonNumber(Integer.valueOf(seasonKey));
                        episode.setTVDBID(tvdbid);
                        season.addEpisode(episode);
                    }

                    seasons.add(season);
                }

                return seasons;
            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private TVShow getTVShow(long tvdbid) {
        ArrayList<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
        params.add(new Pair<String, Object>("tvdbid", tvdbid));
        String json = getJson("show", params);

        try {
            if (json != null && !json.equals("")) {
                JSONObject data = new JSONObject(json).getJSONObject("data");
                TVShow show = new TVShow();

                JSONObject cacheInfo = data.optJSONObject("cache");
                Bitmap banner = getShowBanner(tvdbid, cacheInfo != null ? cacheInfo.optInt("banner", 0) : 0);
                show.setBanner(banner);

                show.setAirByDate(data.getInt("air_by_date") == 1);
                show.setAirs(data.getString("airs"));
                show.setFlattenFolders(data.getInt("flatten_folders") == 1);

                JSONArray genresJsonArray = data.getJSONArray("genre");
                String[] genres = new String[genresJsonArray.length()];
                for (int i = 0; i < genresJsonArray.length(); i++) {
                    genres[i] = genresJsonArray.getString(i);
                }

                show.setGenres(genres);
                show.setLanguage(new Language(data.getString("language")));
                show.setLocation(data.getString("location"));
                show.setNetwork(data.getString("network"));

                try {
                    String nextDateString = data.getString("next_ep_airdate");
                    if (!nextDateString.equals("")) {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(nextDateString);
                        show.setNextAirdate(date);
                    }
                } catch (ParseException e) {
                    show.setNextAirdate(null);
                }

                show.setPaused(data.getInt("paused") == 1);
                show.setQuality(data.getString("quality"));
                show.setShowName(data.getString("show_name"));
                show.setStatus(data.getString("status"));

                return show;
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

    @Override
    protected TVShow doInBackground(Long... longs) {
        long tvdbid = longs[0];
        TVShow show = getTVShow(tvdbid);

        if (show != null) {
            List<Season> seasons = getSeasons(tvdbid);

            // Sort the seasons in reverse order.
            if (seasons != null) {
                Collections.sort(seasons, new SeasonComparator());
                Collections.reverse(seasons);

                show.setSeasons(seasons);
            }
        }

        return show;
    }
}