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

import android.graphics.Bitmap;

import java.util.Date;
import java.util.List;

public class TVShow {
    private boolean mAirByDate;
    private String mAirs;
    private Bitmap mBanner;
    private boolean mFlattenFolders;
    private String[] mGenres;
    private Language mLanguage;
    private String mLocation;
    private String mNetwork;
    private Date mNextAirdate;
    private boolean mPaused;
    private String mQuality;
    private List<Season> mSeasons;
    private String mShowName;
    private String mStatus;

    public boolean getAirByDate() {
        return mAirByDate;
    }

    public String getAirs() {
        return mAirs;
    }

    public Bitmap getBanner() {
        return mBanner;
    }

    public boolean getFlattenFolders() {
        return mFlattenFolders;
    }

    public String[] getGenres() {
        return mGenres;
    }

    public Language getLanguage() {
        return mLanguage;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public Date getNextAirdate() {
        return mNextAirdate;
    }

    public boolean getPaused() {
        return mPaused;
    }

    public String getQuality() {
        return mQuality;
    }

    public List<Season> getSeasons() {
        return mSeasons;
    }

    public String getShowName() {
        return mShowName;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setAirByDate(boolean value) {
        mAirByDate = value;
    }

    public void setAirs(String value) {
        mAirs = value;
    }

    public void setBanner(Bitmap value) {
        mBanner = value;
    }

    public void setFlattenFolders(boolean value) {
        mFlattenFolders = value;
    }

    public void setGenres(String[] value) {
        mGenres = value;
    }

    public void setLanguage(Language value) {
        mLanguage = value;
    }

    public void setLocation(String value) {
        mLocation = value;
    }

    public void setNetwork(String value) {
        mNetwork = value;
    }

    public void setNextAirdate(Date value) {
        mNextAirdate = value;
    }

    public void setPaused(boolean value) {
        mPaused = value;
    }

    public void setQuality(String value) {
        mQuality = value;
    }

    public void setSeasons(List<Season> value) {
        mSeasons = value;
    }

    public void setShowName(String value) {
        mShowName = value;
    }

    public void setStatus(String value) {
        mStatus = value;
    }
}