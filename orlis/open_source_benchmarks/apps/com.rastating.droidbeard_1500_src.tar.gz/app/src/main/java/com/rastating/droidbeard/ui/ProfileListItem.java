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
import android.widget.Button;
import android.widget.TextView;

import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;

public class ProfileListItem implements View.OnClickListener {
    private Context mContext;
    private String mName;
    private View mView;
    private TextView mNameView;
    private Button mSelectButton;
    private Button mDeleteButton;
    private ProfileStateChangeListener mStateChangeListener;

    public ProfileListItem(Context context, String name) {
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.profile_list_item, null);
        mNameView = (TextView) mView.findViewById(R.id.profile_name);
        mNameView.setText(name);
        mSelectButton = (Button) mView.findViewById(R.id.select);
        mDeleteButton = (Button) mView.findViewById(R.id.delete);

        mSelectButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mDeleteButton.setEnabled(!name.equals(Preferences.DEFAULT_PROFILE_NAME));
    }

    public void setStateChangeListener(ProfileStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    public void setSelected(boolean value) {
        Resources resources = mContext.getResources();
        if (value) {
            mView.setBackgroundColor(resources.getColor(R.color.downloaded_episode_background));
        }
        else {
            mView.setBackgroundColor(resources.getColor(android.R.color.transparent));
        }

        mSelectButton.setEnabled(!value);
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectButton) {
            setSelected(true);
            new Preferences(mContext).selectProfile(mNameView.getText().toString());
            if (mStateChangeListener != null) {
                mStateChangeListener.profileSelected(mNameView.getText().toString(), true);
            }
        }
        else if (v == mDeleteButton) {
            new Preferences(mContext).deleteProfile(mNameView.getText().toString());
            if (mStateChangeListener != null) {
                mStateChangeListener.profileDeleted(mNameView.getText().toString());
            }
        }
    }
}