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

package com.rastating.droidbeard.entities;

import java.util.ArrayList;
import java.util.List;

public class Season {
    private int mSeasonNumber;
    private List<Episode> mEpisodes;
    private long mTVDBID;

    public Season() {
        mEpisodes = new ArrayList<Episode>();
    }

    public void addEpisode(Episode episode) {
        mEpisodes.add(episode);
    }

    public int getSeasonNumber() {
        return mSeasonNumber;
    }

    public String getTitle() {
        if (mSeasonNumber > 0) {
            return String.format("Season %d", mSeasonNumber);
        }
        else {
            return "Specials";
        }
    }

    public List<Episode> getEpisodes() {
        return mEpisodes;
    }

    public long getTVDBID() {
        return mTVDBID;
    }

    public void setSeasonNumber(int value) {
        mSeasonNumber = value;
    }

    public void setTVDBID(long value) {
        mTVDBID = value;
    }
}