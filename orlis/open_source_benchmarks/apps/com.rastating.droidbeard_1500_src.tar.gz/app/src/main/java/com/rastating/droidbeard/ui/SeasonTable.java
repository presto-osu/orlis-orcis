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

package com.rastating.droidbeard.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.Episode;
import com.rastating.droidbeard.entities.Season;

import java.util.ArrayList;

public class SeasonTable extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private String mTitle;
    private TableLayout mTable;
    private CheckBox mSelectAll;
    private ArrayList<EpisodeItem> mEpisodes;
    private Season mSeason;

    public SeasonTable(Context context, Season season) {
        super(context);

        mContext = context;
        mSeason = season;
        mEpisodes = new ArrayList<EpisodeItem>();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.season_table, this, true);
        mTable = (TableLayout) this.findViewById(R.id.table);
        mSelectAll = (CheckBox) this.findViewById(R.id.select_all);
        mSelectAll.setOnCheckedChangeListener(this);
    }

    public EpisodeItem addEpisode(Episode episode) {
        EpisodeItem item = new EpisodeItem(mContext, this, episode);
        item.setEpisodeNumber(episode.getEpisodeNumber());
        item.setName(episode.getName());
        item.setAirdate(episode.getAirdate());
        item.setStatus(episode.getStatus());
        item.setSeasonNumber(episode.getSeasonNumber());
        item.addToTable(mTable);
        mEpisodes.add(item);

        return item;
    }

    public boolean allEpisodesChecked() {
        boolean allChecked = true;
        for (EpisodeItem item : mEpisodes) {
            if (!item.isChecked()) {
                allChecked = false;
                break;
            }
        }

        return allChecked;
    }

    public boolean allEpisodesNotChecked() {
        boolean allNotChecked = true;
        for (EpisodeItem item : mEpisodes) {
            if (item.isChecked()) {
                allNotChecked = false;
                break;
            }
        }

        return allNotChecked;
    }

    public ArrayList<EpisodeItem> getEpisodeItems() {
        return mEpisodes;
    }

    public Season getSeason() {
        return mSeason;
    }

    public void setChecked(boolean value) {
        mSelectAll.setChecked(value);
    }

    public void setTitle(String value) {
        mTitle = value;
        TextView titleView = (TextView) this.findViewById(R.id.season_title);
        titleView.setText(value);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mSelectAll.isChecked() || (!mSelectAll.isChecked() && allEpisodesChecked())) {
            for (int i = 0; i < mEpisodes.size(); i++) {
                mEpisodes.get(i).setChecked(mSelectAll.isChecked());
            }
        }
    }

    public void updateSelectAllState() {
        if (allEpisodesChecked()) {
            mSelectAll.setChecked(true);
        }

        if (allEpisodesNotChecked()) {
            mSelectAll.setChecked(false);
        }
    }
}