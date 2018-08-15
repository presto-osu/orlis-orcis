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
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SetEpisodeStatusTask extends SickbeardAsyncTask<String, Void, Boolean> {
    private long mTvDBId;
    private int mSeason;
    private int mEpisode;

    public SetEpisodeStatusTask(Context context, long tvdbid, int season, int episode) {
        super(context);

        mTvDBId = tvdbid;
        mSeason = season;
        mEpisode = episode;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        List<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
        params.add(new Pair<String, Object>("tvdbid", mTvDBId));
        params.add(new Pair<String, Object>("season", mSeason));

        if (mEpisode > 0) {
            params.add(new Pair<String, Object>("episode", mEpisode));
        }

        params.add(new Pair<String, Object>("status", strings[0]));
        params.add(new Pair<String, Object>("force", 1));

        try {
            return getJson("episode.setstatus", params).contains("success");
        }
        catch (Exception e) {
            return false;
        }
    }
}