/**
 *
 *  This file is part of the Berlin-Vegan Guide (Android app),
 *  Copyright 2015-2016 (c) by the Berlin-Vegan Guide Android app team
 *
 *      <https://github.com/Berlin-Vegan/berlin-vegan-guide/graphs/contributors>.
 *
 *  The Berlin-Vegan Guide is Free Software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation,
 *  either version 2 of the License, or (at your option) any later version.
 *
 *  The Berlin-Vegan Guide is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with The Berlin-Vegan Guide.
 *
 *  If not, see <https://www.gnu.org/licenses/old-licenses/gpl-2.0.html>.
 *
**/


package org.berlin_vegan.bvapp.fragments.LocationDetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.activities.LocationDetailActivity;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.helpers.UiUtils;


public class LocationHeadFragment extends LocationBaseFragment {
    private Location mLocation;

    public LocationHeadFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_head_fragment, container, false);
        mLocation = initLocation(savedInstanceState);
        final TextView titleTextView = (TextView) view.findViewById(R.id.text_view_title);
        titleTextView.setText(mLocation.getName());
        final TextView streetTextView = (TextView) view.findViewById(R.id.text_view_street);
        streetTextView.setText(mLocation.getStreet());

        final TextView distanceTextView = (TextView) view.findViewById(R.id.text_view_distance);
        final Float distToCurLoc = mLocation.getDistToCurLoc();
        if (distToCurLoc > -1.0f) {
            distanceTextView.setText(UiUtils.getFormattedDistance(distToCurLoc, getActivity()));
        } else {
            distanceTextView.setText("");
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(LocationDetailActivity.EXTRA_LOCATION, mLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

}
