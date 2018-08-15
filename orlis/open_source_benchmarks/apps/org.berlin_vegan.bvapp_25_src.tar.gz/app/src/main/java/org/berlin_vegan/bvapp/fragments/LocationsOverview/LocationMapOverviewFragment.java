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


package org.berlin_vegan.bvapp.fragments.LocationsOverview;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.berlin_vegan.bvapp.R;
import org.berlin_vegan.bvapp.activities.LocationDetailActivity;
import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;
import org.berlin_vegan.bvapp.data.Location;
import org.berlin_vegan.bvapp.data.Locations;
import org.berlin_vegan.bvapp.helpers.UiUtils;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

/**
 * Created by micu on 02/02/16.
 */

public class LocationMapOverviewFragment extends Fragment {

    protected MapView mMapView;
    protected ResourceProxy mResourceProxy;

    protected ItemizedIconOverlay<LocationOverlayItem> mLocationOverlay;
    protected ArrayList<LocationOverlayItem> mOverlayItemList;

    public LocationMapOverviewFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mResourceProxy = new ResourceProxyImpl(inflater.getContext().getApplicationContext());
        mMapView = new MapView(inflater.getContext(), mResourceProxy);

        mMapView.getController().setInvertedTiles(false);

        mMapView.setTileSource(UiUtils.GOOGLE_MAPS_TILE);
        mMapView.setMultiTouchControls(true);

        mOverlayItemList = new ArrayList<>();

        // inner class seems HACKy here ....
        OnItemGestureListener<LocationOverlayItem> myOnItemGestureListener
                = new OnItemGestureListener<LocationOverlayItem>() {

            @Override
            public boolean onItemLongPress(int arg0, LocationOverlayItem arg1) {
                // TODO
                return false;
            }

            @Override
            public boolean onItemSingleTapUp(int index, LocationOverlayItem item) {
                final Intent intent = new Intent(getContext(), LocationDetailActivity.class);
                intent.putExtra(LocationDetailActivity.EXTRA_LOCATION, item.getCorrespondingLocation());
                startActivity(intent);
                return true;
            }
        };

        mLocationOverlay = new ItemizedIconOverlay<>(getContext(), mOverlayItemList, myOnItemGestureListener);
        mMapView.getOverlays().add(mLocationOverlay);

        IMapController mapController = mMapView.getController();
        mapController.setZoom(10);

        // set Center of the map to Alex
        GeoPoint gPoint = new GeoPoint(52.521918, 13.413215);
        mapController.setCenter(gPoint);

        Locations locations = ((LocationsOverviewActivity) getActivity()).getLocations();

        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            gPoint = new GeoPoint(location.getLatCoord(), location.getLongCoord());
            LocationOverlayItem mMarkerItem = new LocationOverlayItem(location.getName(), location.getVegan().toString(), gPoint, location);
            Drawable marker = getResources().getDrawable(R.mipmap.ic_map_pin_red);
            mMarkerItem.setMarker(marker);
            mLocationOverlay.addItem(mMarkerItem);
        }

        return mMapView;
    }

    // inner class seems HACKy here ....
    class LocationOverlayItem extends OverlayItem {
        private Location mCorrespondingLocation;

        public LocationOverlayItem(final String aTitle, final String aSnippet, final IGeoPoint aGeoPoint, Location correspondingLocation) {
            super(aTitle, aSnippet, aGeoPoint);

            mCorrespondingLocation = correspondingLocation;
        }

        public Location getCorrespondingLocation() {
            return mCorrespondingLocation;
        }
    }
}
