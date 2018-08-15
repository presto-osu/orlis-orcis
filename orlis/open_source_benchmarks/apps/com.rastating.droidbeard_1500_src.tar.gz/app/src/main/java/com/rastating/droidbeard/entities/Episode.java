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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Episode {
    public enum EpisodeStatus {
        SKIPPED,
        UNAIRED,
        WANTED,
        DOWNLOADED,
        SNATCHED,
        IGNORED,
        ARCHIVED
    }

    private int mEpisodeNumber;
    private Date mAirdate;
    private String mName;
    private String mQuality;
    private int mSeasonNumber;
    private EpisodeStatus mStatus;
    private long mTVDBID;

    public Date getAirdate() {
        return mAirdate;
    }

    public String getAirdateString(String format) {
        return new SimpleDateFormat(format).format(mAirdate);
    }

    public String getName() {
        return mName;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public String getQuality() {
        return mQuality;
    }

    public int getSeasonNumber() {
        return mSeasonNumber;
    }

    public long getTVDBID() {
        return mTVDBID;
    }

    public EpisodeStatus getStatus() {
        return mStatus;
    }

    public String getStatusString() {
        switch (mStatus) {
            case DOWNLOADED:
                return "Downloaded";

            case SKIPPED:
                return "Skipped";

            case UNAIRED:
                return "Unaired";

            case WANTED:
                return "Wanted";

            case SNATCHED:
                return "Snatched";

            case IGNORED:
                return "Ignored";

            case ARCHIVED:
                return "Archived";
        }

        return "";
    }

    public void setAirdate(Date value) {
        mAirdate = value;
    }

    public void setAirdate(String value) {
        try {
            if (value.equals("")) {
                mAirdate = null;
            }
            else {
                mAirdate = new SimpleDateFormat("yyyy-MM-dd").parse(value);
            }
        } catch (ParseException e) {
            mAirdate = null;
        }
    }

    public void setEpisodeNumber(int value) {
        mEpisodeNumber = value;
    }

    public void setName(String value) {
        mName = value;
    }

    public void setQuality(String value) {
        mQuality = value;
    }

    public void setSeasonNumber(int value) {
        mSeasonNumber = value;
    }

    public void setStatus(EpisodeStatus value) {
        mStatus = value;
    }

    public void setStatus(String value) {
        if (value.equalsIgnoreCase("skipped")) {
            mStatus = EpisodeStatus.SKIPPED;
        }
        else if (value.equalsIgnoreCase("unaired")) {
            mStatus = EpisodeStatus.UNAIRED;
        }
        else if (value.equalsIgnoreCase("wanted")) {
            mStatus = EpisodeStatus.WANTED;
        }
        else if (value.equalsIgnoreCase("downloaded")) {
            mStatus = EpisodeStatus.DOWNLOADED;
        }
        else if (value.equalsIgnoreCase("snatched")) {
            mStatus = EpisodeStatus.SNATCHED;
        }
        else if (value.equalsIgnoreCase("ignored")) {
            mStatus = EpisodeStatus.IGNORED;
        }
        else if (value.equalsIgnoreCase("archived")) {
            mStatus = EpisodeStatus.ARCHIVED;
        }
        else {
            mStatus = EpisodeStatus.IGNORED;
        }
    }

    public void setTVDBID(long value) {
        mTVDBID = value;
    }
}