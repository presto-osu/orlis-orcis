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


package org.berlin_vegan.bvapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.activities.LocationDetailActivity;
import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.data.GastroLocation;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.helpers.DateUtil;
import org.berlin_vegan.bvapp.helpers.UiUtils;

import java.util.Date;
import java.util.GregorianCalendar;

import static android.view.View.OnClickListener;

/**
 * Fills the {@code RecyclerView} of {@link LocationsOverviewActivity} with its content.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final LocationsOverviewActivity mLocationListActivity;

    public LocationAdapter(LocationsOverviewActivity locationListActivity) {
        mLocationListActivity = locationListActivity;
    }

    @Override
    public int getItemCount() {
        return mLocationListActivity.getLocations().size();
    }

    @Override
    public void onBindViewHolder(LocationViewHolder viewHolder, int i) {
        final Location location = mLocationListActivity.getLocations().get(i);
        viewHolder.vTitle.setText(location.getName());
        viewHolder.vStreet.setText(location.getStreet());
        final float distToCurLoc = location.getDistToCurLoc();
        if (distToCurLoc > -1.0f) {
            // TODO: speed up reloading the distances after a settings change
            // string for distance unit depends on settings
            final String distance = UiUtils.getFormattedDistance(distToCurLoc, mLocationListActivity);
            viewHolder.vDistance.setText(distance);
        }
        // update opening hours field
        final Date currentTime = GregorianCalendar.getInstance().getTime();
        final Date currentTimePlus30Minutes = DateUtil.addMinutesToDate(currentTime, 30);
        if (!location.isOpen(currentTime)) {
            viewHolder.vClosed.setText(mLocationListActivity.getString(R.string.gastro_list_closed));
            viewHolder.vClosed.setTextColor(mLocationListActivity.getResources().getColor(R.color.disabled));
            viewHolder.vDistance.setTextColor(mLocationListActivity.getResources().getColor(R.color.disabled));
        } else if (!location.isOpen(currentTimePlus30Minutes)) {
            final String formattedClosingTime = location.getFormattedClosingTime(currentTime);
            viewHolder.vClosed.setText(mLocationListActivity.getString(R.string.gastro_list_closed_soon, formattedClosingTime));
            viewHolder.vClosed.setTextColor(mLocationListActivity.getResources().getColor(R.color.text_attention));
            viewHolder.vDistance.setTextColor(mLocationListActivity.getResources().getColor(R.color.theme_primary));
        } else {
            viewHolder.vClosed.setText(""); // clear
            viewHolder.vDistance.setTextColor(mLocationListActivity.getResources().getColor(R.color.theme_primary));
        }

        // update vegan label, indicate 100% vegan locations
        if (location.getVegan() == GastroLocation.VEGAN) {
            viewHolder.vVeganLabel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.vVeganLabel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.location_list_item, viewGroup, false);

        return new LocationViewHolder(itemView, mLocationListActivity);
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        final TextView vTitle;
        final TextView vClosed;
        final TextView vStreet;
        final TextView vDistance;
        final ImageView vVeganLabel;
        private final LocationsOverviewActivity mLocationListActivity;

        public LocationViewHolder(View v, LocationsOverviewActivity locationListActivity) {
            super(v);
            v.setOnClickListener(this);
            mLocationListActivity = locationListActivity;
            vTitle = (TextView) v.findViewById(R.id.text_view_title);
            vVeganLabel = (ImageView) v.findViewById(R.id.image_view_vegan_label);
            vClosed = (TextView) v.findViewById(R.id.text_view_closed);
            vStreet = (TextView) v.findViewById(R.id.text_view_street);
            vDistance = (TextView) v.findViewById(R.id.text_view_distance);
        }

        @Override
        public void onClick(View view) {
            final Context context = view.getContext();
            final Intent intent = new Intent(context, LocationDetailActivity.class);
            final int position = getAdapterPosition();
            final Location gastroLocation = mLocationListActivity.getLocations().get(position);
            intent.putExtra(LocationDetailActivity.EXTRA_LOCATION, gastroLocation);
            context.startActivity(intent);
        }
    }
}
