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

public class HistoricalEvent {
    private String mDate;
    private int mEpisodeNumber;
    private String mProvider;
    private String mQuality;
    private int mSeason;
    private String mShowName;
    private String mStatus;

    public String getDate() {
        return mDate;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public String getProvider() {
        return mProvider;
    }

    public String getQuality() {
        return mQuality;
    }

    public int getSeason() {
        return mSeason;
    }

    public String getShowName() {
        return mShowName;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setDate(String value) {
        mDate = value;
    }

    public void setEpisodeNumber(int value) {
        mEpisodeNumber = value;
    }

    public void setProvider(String value) {
        mProvider = value;
    }

    public void setQuality(String value) {
        mQuality = value;
    }

    public void setSeason(int value) {
        mSeason = value;
    }

    public void setShowName(String value) {
        mShowName = value;
    }

    public void setStatus(String value) {
        mStatus = value;
    }
}