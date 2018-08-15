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
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.rastating.droidbeard.R;
import com.rastating.droidbeard.entities.Episode;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EpisodeItem implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private View mView;
    private TextView mEpisodeNumber;
    private TextView mName;
    private TextView mAirdate;
    private CheckBox mInclude;
    private int mSeasonNumber;
    private EpisodeItemClickListener mItemClickListener;
    private SeasonTable mSeasonTable;
    private Episode mEpisode;

    public EpisodeItem(Context context, SeasonTable table, Episode episode) {
        mContext = context;
        mSeasonTable = table;
        mEpisode = episode;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.episode_item, null);
        mEpisodeNumber = (TextView) mView.findViewById(R.id.episode_number);
        mName = (TextView) mView.findViewById(R.id.name);
        mAirdate = (TextView) mView.findViewById(R.id.airdate);
        mInclude = (CheckBox) mView.findViewById(R.id.include);

        mView.setOnClickListener(this);
        mInclude.setOnCheckedChangeListener(this);
    }

    public void addToTable(TableLayout table) {
        table.addView(mView);
    }

    public boolean isChecked() {
        return mInclude.isChecked();
    }

    public Episode getEpisode() {
        return mEpisode;
    }

    public SeasonTable getSeasonTable() {
        return mSeasonTable;
    }

    public void setChecked(boolean value) {
        mInclude.setChecked(value);
    }

    public void setEpisodeNumber(int value) {
        mEpisodeNumber.setText(String.valueOf(value));
    }

    public void setName(String value) {
        mName.setText(value);
    }

    public void setAirdate(Date value) {
        if (value == null) {
            mAirdate.setText("Never");
        }
        else {
            mAirdate.setText(new SimpleDateFormat("yyyy-MM-dd").format(value));
        }
    }

    public void setStatus(String value) {
        if (value.equalsIgnoreCase("skipped")) {
            setStatus(Episode.EpisodeStatus.SKIPPED);
        }
        else if (value.equalsIgnoreCase("unaired")) {
            setStatus(Episode.EpisodeStatus.UNAIRED);
        }
        else if (value.equalsIgnoreCase("wanted")) {
            setStatus(Episode.EpisodeStatus.WANTED);
        }
        else if (value.equalsIgnoreCase("downloaded")) {
            setStatus(Episode.EpisodeStatus.DOWNLOADED);
        }
        else if (value.equalsIgnoreCase("snatched")) {
            setStatus(Episode.EpisodeStatus.SNATCHED);
        }
        else if (value.equalsIgnoreCase("ignored")) {
            setStatus(Episode.EpisodeStatus.IGNORED);
        }
        else if (value.equalsIgnoreCase("archived")) {
            setStatus(Episode.EpisodeStatus.ARCHIVED);
        }
        else {
            setStatus(Episode.EpisodeStatus.IGNORED);
        }
    }

    public void setStatus(Episode.EpisodeStatus value) {
        Resources resources = mContext.getResources();
        int color = resources.getColor(R.color.skipped_episode_background);
        switch (value) {
            case SNATCHED:
                color = resources.getColor(R.color.snatched_episode_background);
                break;

            case WANTED:
                color = resources.getColor(R.color.wanted_episode_background);
                break;

            case UNAIRED:
                color = resources.getColor(R.color.unaired_episode_background);
                break;

            case IGNORED:
            case SKIPPED:
                color = resources.getColor(R.color.skipped_episode_background);
                break;

            case ARCHIVED:
            case DOWNLOADED:
                color = resources.getColor(R.color.downloaded_episode_background);
                break;
        }

        mInclude.setBackgroundColor(color);
        mEpisodeNumber.setBackgroundColor(color);
        mName.setBackgroundColor(color);
        mAirdate.setBackgroundColor(color);
    }

    public void setSeasonNumber(int value) {
        mSeasonNumber = value;
    }

    public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener listener) {
        mView.setOnCreateContextMenuListener(listener);
    }

    public void setOnItemClickListener(EpisodeItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void showContextMenu() {
        mView.showContextMenu();
    }

    @Override
    public void onClick(View view) {
        if (view != mInclude) {
            mInclude.toggle();
        }
        else {
            if (mItemClickListener != null) {
                mSeasonTable.updateSelectAllState();
                mItemClickListener.onItemClick(this, mSeasonNumber, Integer.valueOf(mEpisodeNumber.getText().toString()), mName.getText().toString());
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        onClick(mInclude);
    }
}