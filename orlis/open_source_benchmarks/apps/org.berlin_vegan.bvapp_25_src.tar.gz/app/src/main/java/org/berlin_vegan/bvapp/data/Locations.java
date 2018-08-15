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


package org.berlin_vegan.bvapp.data;

import android.content.Context;

import org.berlin_vegan.bvapp.activities.LocationsOverviewActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * class, that holds an object of all gastro locations, filtered gastro locations, and the gastro locations,
 * which are currently presented to the user. Latter includes searching through the gastro locations lists.
 */
public class Locations {

    private static Set<String> sFavoriteIDs = new HashSet<>();
    //needed for UI thread updateLocationAdapter
    private final LocationsOverviewActivity mLocationListActivity;
    /**
     * holds favorite locations
     */
    private final List<Location> mFavorites = new ArrayList<>();
    private DATA_TYPE mDataType = DATA_TYPE.GASTRO;
    private boolean mSearchState = false;
    private android.location.Location mGpsLocationFound;
    /**
     * holds all locations. used to create the filtered lists
     */
    private List<Location> mAll = new ArrayList<>();
    /**
     * holds filtered locations. can be searched with {@code mQueryFilter}
     */
    private List<Location> mFiltered = new ArrayList<>();
    /**
     * holds the locations, that are presented to the user in {@code MainListActivity}
     */
    private List<Location> mShown = new ArrayList<>();
    private String mQueryFilter;

    public Locations(LocationsOverviewActivity locationListactivity) {
        mLocationListActivity = locationListactivity;
        sFavoriteIDs = Preferences.getFavorites(mLocationListActivity);
    }

    // --------------------------------------------------------------------
    // favorites

    public static boolean containsFavorite(String id) {
        return sFavoriteIDs.contains(id);
    }

    public static void addFavorite(Context context, String id) {
        sFavoriteIDs.add(id);
        Preferences.saveFavorites(context, sFavoriteIDs);
    }

    public static void removeFavorite(Context context, String id) {
        sFavoriteIDs.remove(id);
        Preferences.saveFavorites(context, sFavoriteIDs);
    }

    public DATA_TYPE getDataType() {
        return mDataType;
    }

    public void setDataType(DATA_TYPE type) {
        mDataType = type;
    }

    public boolean getSearchState() {
        return mSearchState;
    }

    public void setSearchState(boolean enabled) {
        mSearchState = enabled;
    }

    private void sortByDistance() {
        if (mGpsLocationFound == null) {
            return;
        }
        android.location.Location locationFromJson = new android.location.Location("DummyProvider");
        float distanceInMeters;
        float distanceInKiloMeters;
        float distanceInMiles;
        float distanceRoundOnePlace;
        for (int i = 0; i < mShown.size(); i++) {
            Location location = mShown.get(i);
            locationFromJson.setLatitude(location.getLatCoord());
            locationFromJson.setLongitude(location.getLongCoord());
            distanceInMeters = locationFromJson.distanceTo(mGpsLocationFound);
            distanceInKiloMeters = distanceInMeters / 1000;
            distanceInMiles = distanceInKiloMeters * (float) 0.621371192;

            // 1. explicit cast to float necessary, otherwise we always get x.0 values
            // 2. Math.round(1.234 * 10) / 10 = Math.round(12.34) / 10 = 12 / 10 = 1.2
            if (Preferences.isMetricUnit(mLocationListActivity.getApplicationContext())) {
                distanceRoundOnePlace = (float) Math.round(distanceInKiloMeters * 10) / 10;
            } else {
                distanceRoundOnePlace = (float) Math.round(distanceInMiles * 10) / 10;
            }
            location.setDistToCurLoc(distanceRoundOnePlace);
        }
        Collections.sort(mShown);
    }

    public void showFiltersResult(GastroLocationFilter filter) {
        if (mAll != null && !mAll.isEmpty()) {
            mFiltered.clear();
            mFiltered = getFilterResult(filter);
            mShown = new ArrayList<>(mFiltered);
            updateLocationAdapter();
        }
    }

    public void showFavorites() {
        mFavorites.clear();
        for (Location gastro : mAll) {
            if (sFavoriteIDs.contains(gastro.getId())) {
                mFavorites.add(gastro);
            }
        }
        mShown = new ArrayList<>(mFavorites);
        updateLocationAdapter();
    }

    public void showGastroLocations() { // todo should also base on instance type, not only filter
        // filter the locations with current filter
        final GastroLocationFilter filter = Preferences.getGastroFilter(mLocationListActivity);
        showFiltersResult(filter);

    }

    public void showShoppingLocations() {
        mShown = new ArrayList<>();
        if (mAll != null && !mAll.isEmpty()) {
            for (Location location : mAll) {
                if (location instanceof ShoppingLocation)
                    mShown.add(location);
            }
        }
        mFiltered = new ArrayList<>(mShown); // no filtering at the moment, so show && filtered are equal
        updateLocationAdapter();
    }

    // --------------------------------------------------------------------
    // query
    // todo remove "instance of" usage -> separate classes to handle search for different types

    public void processQueryFilter(String query) {
        mQueryFilter = query;
        final List<Location> queryFilteredList = new ArrayList<>();
        for (Location location : mFiltered) {
            final String gastroName = location.getName().toUpperCase();
            final String gastroComment = location.getCommentWithoutSoftHyphens().toUpperCase();
            final String getStreet = location.getStreet().toUpperCase();
            final String queryFilter = mQueryFilter.toUpperCase();

            boolean match = gastroName.contains(queryFilter)
                    || gastroComment.contains(queryFilter)
                    || getStreet.contains(queryFilter);

            if (location instanceof GastroLocation) { // if GastroLocation search also district
                GastroLocation loc = (GastroLocation) location;
                final String getDistrict = loc.getDistrict().toUpperCase();
                if (getDistrict.contains(queryFilter)) {
                    match = true;
                }
            }
            if (match) {
                queryFilteredList.add(location);
            }
        }
        mShown = new ArrayList<>(queryFilteredList);
        updateLocationAdapter();
    }

    public void resetQueryFilter() {
        mQueryFilter = "";
    }

    // --------------------------------------------------------------------
    // updating

    public void updateLocationAdapter() {
        sortByDistance();
        mLocationListActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLocationListActivity.getLocationAdapter().notifyDataSetChanged();
            }
        });
    }

    public void updateLocationAdapter(android.location.Location location) {
        mGpsLocationFound = location;
        updateLocationAdapter();
    }

    // --------------------------------------------------------------------
    // getters & setters

    public void set(List<Location> locations) {
        mAll = mAll.isEmpty() ? locations : throw_();
        mShown = new ArrayList<>(mAll);
        updateLocationAdapter();
        // has to be set after {@code updateLocationAdapter()} so that {@code mShown} is already sorted
        mFiltered = new ArrayList<>(mShown);
    }

    private List<Location> throw_() {
        throw new RuntimeException("gastro locations are already set");
    }

    public int size() {
        return mShown.size();
    }

    public Location get(int i) {
        return mShown.get(i);
    }

    /**
     * returns the locations if the gastroFilter is applied
     */
    public List<Location> getFilterResult(GastroLocationFilter filter) {
        List<Location> filtered = new ArrayList<>();
        for (Location gastro : mAll) {
            if (filter.matchToFilter(gastro)) {
                filtered.add(gastro);
            }
        }
        return filtered;
    }


    public enum DATA_TYPE {GASTRO, SHOPPING, FAVORITE;}
}
