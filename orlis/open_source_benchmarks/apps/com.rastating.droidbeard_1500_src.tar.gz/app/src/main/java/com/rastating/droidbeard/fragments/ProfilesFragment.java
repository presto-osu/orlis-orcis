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

package com.rastating.droidbeard.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rastating.droidbeard.Preferences;
import com.rastating.droidbeard.R;
import com.rastating.droidbeard.ui.ProfileListItem;
import com.rastating.droidbeard.ui.ProfileStateChangeListener;

import java.util.Iterator;
import java.util.Set;

public class ProfilesFragment extends DroidbeardFragment implements ProfileStateChangeListener {
    private LinearLayout mContainer;

    public ProfilesFragment() {
        setTitle("Profiles");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profiles, null, false);
        mContainer = (LinearLayout) root.findViewById(R.id.profile_list);

        loadProfileItems();

        Button addProfileButton = (Button) root.findViewById(R.id.add_profile);
        addProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(ProfilesFragment.this.getActivity());
                new AlertDialog.Builder(ProfilesFragment.this.getActivity())
                    .setTitle("New Profile")
                    .setMessage("Enter the name of the new profile:")
                    .setView(input)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            addProfile(input.getText().toString());
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Do nothing.
                        }
                    }).show();
            }
        });
        return root;
    }

    private void loadProfileItems() {
        // Empty container view.
        mContainer.removeAllViews();

        // Add the mandatory default profile.
        Preferences preferences = new Preferences(getActivity());
        createProfileItem(Preferences.DEFAULT_PROFILE_NAME, preferences.getSelectedProfileName().equals(Preferences.DEFAULT_PROFILE_NAME));

        // Add the user created profiles.
        Set<String> profileSet = preferences.getProfileSet();
        Iterator<String> iterator = profileSet.iterator();
        while (iterator.hasNext()) {
            String profileName = iterator.next();
            createProfileItem(profileName, preferences.getSelectedProfileName().equals(profileName));
        }
    }

    private void createProfileItem(String name, boolean selected) {
        ProfileListItem profile = new ProfileListItem(getActivity(), name);
        profile.setSelected(selected);
        profile.setStateChangeListener(this);
        mContainer.addView(profile.getView());
    }

    private void addProfile(String profileName) {
        Preferences preferences = new Preferences(getActivity());
        Set<String> profiles = preferences.getProfileSet();
        if (profiles.contains(profileName)) {
            Toast.makeText(getActivity(), "A profile with this name already exists.", Toast.LENGTH_LONG).show();
        }
        else {
            profiles.add(profileName);
            preferences.updateProfileSet(profiles);
            createProfileItem(profileName, false);
            Toast.makeText(getActivity(), "Profile added.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void profileSelected(String name, boolean selected) {
        loadProfileItems();
        getMainActivity().invalidateFragmentCache();
    }

    @Override
    public void profileDeleted(String name) {
        loadProfileItems();
        getMainActivity().invalidateFragmentCache();
    }
}