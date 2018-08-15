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
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class TVShowSummary implements Parcelable {
    private String mAirs;
    private String mName;
    private String mNetwork;
    private Date mNextAirDate;
    private boolean mPaused;
    private String mStatus;
    private long mTvDbId;
    private Bitmap mBanner;

    public TVShowSummary(String name) {
        mName = name;
        mNextAirDate = null;
    }

    public TVShowSummary(Parcel in) {
        String[] strings = new String[4];
        in.readStringArray(strings);
        mAirs = strings[0];
        mName = strings[1];
        mNetwork = strings[2];
        mStatus = strings[3];

        long airDateTimeStamp = in.readLong();
        mNextAirDate = airDateTimeStamp > 0 ? new Date(airDateTimeStamp) : null;

        mPaused = in.readInt() == 1;
        mTvDbId = in.readLong();
    }

    public Bitmap getBanner() {
        return mBanner;
    }

    public String getAirs() {
        return mAirs;
    }

    public String getName() {
        return mName;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public Date getNextAirDate() {
        return mNextAirDate;
    }

    public boolean getPaused() {
        return mPaused;
    }

    public String getStatus() {
        return mStatus;
    }

    public long getTvDbId() {
        return mTvDbId;
    }

    public void setAirs(String value) {
        mAirs = value;
    }

    public void setBanner(Bitmap value) {
        mBanner = value;
    }

    public void setNetwork(String value) {
        mNetwork = value;
    }

    public void setNextAirDate(Date value) {
        mNextAirDate = value;
    }

    public void setPaused(boolean value) {
        mPaused = value;
    }

    public void setStatus(String value) {
        mStatus = value;
    }

    public void setTvDbId(long value) {
        mTvDbId = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeStringArray(new String[] { mAirs, mName, mNetwork, mStatus });
        parcel.writeLong(mNextAirDate != null ? mNextAirDate.getTime() : 0);
        parcel.writeInt(mPaused ? 1 : 0);
        parcel.writeLong(mTvDbId);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TVShowSummary createFromParcel(Parcel in) {
            return new TVShowSummary(in);
        }

        public TVShowSummary[] newArray(int size) {
            return new TVShowSummary[size];
        }
    };
}