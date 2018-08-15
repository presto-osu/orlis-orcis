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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EpisodeSearchTask extends SickbeardAsyncTask<Void, Void, Boolean> {
    private long mTvDBId;
    private int mSeason;
    private int mEpisode;

    protected static BlockingQueue BLOCKING_QUEUE = new ArrayBlockingQueue(100);
    protected static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(10, 100, 5, TimeUnit.SECONDS, BLOCKING_QUEUE);

    public EpisodeSearchTask(Context context, long tvdbid, int season, int episode) {
        super(context);

        mTvDBId = tvdbid;
        mSeason = season;
        mEpisode = episode;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        List<Pair<String, Object>> params = new ArrayList<Pair<String, Object>>();
        params.add(new Pair<String, Object>("tvdbid", mTvDBId));
        params.add(new Pair<String, Object>("season", mSeason));
        params.add(new Pair<String, Object>("episode", mEpisode));

        String json = getJson("episode.search", params);
        try {
            return json != null && json.contains("success");
        }
        catch (Exception e) {
            setLastException(json, e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void start(Void... args) {
        super.start(EpisodeSearchTask.EXECUTOR, args);
    }
}