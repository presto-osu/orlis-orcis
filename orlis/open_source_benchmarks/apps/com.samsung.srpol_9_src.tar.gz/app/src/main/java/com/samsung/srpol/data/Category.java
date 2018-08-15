/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.samsung.srpol.loader.AppDetails;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class Category {

    private String mTitle;
    private String mHeader;
    private String mDescription;
    private String mShortDescription;
    private int mSubCategoriesMask;
    private int mIconRes;
    private String mLink;
    private boolean mCanSendData;
    private Drawable mIconDrawable;
    private ArrayList<Subcategory> mSubCategories;
    private ArrayList<AppDetails> mRelatedApps = new ArrayList<AppDetails>();
    private int mCurrentlyVisible;

    public Category(Context ctx, String title, String header,
            String shortDescription, String description, int icon, String link,
            boolean dataSend, ArrayList<Subcategory> subCategories) {
        mTitle = title;
        mHeader = header;
        mShortDescription = shortDescription;
        mDescription = description;
        mIconRes = icon;
        mLink = link;
        mIconDrawable = ctx.getResources().getDrawable(mIconRes);
        mSubCategories = subCategories;
        for (Subcategory subcategory : mSubCategories)
            mSubCategoriesMask = mSubCategoriesMask | subcategory.getId();
        mCanSendData = dataSend;
        mCurrentlyVisible = mRelatedApps.size();
    }

    public void removeAppFromList(AppDetails removed) {
        mRelatedApps.remove(removed);
    }

    public int getSubCategoriesMask() {
        return mSubCategoriesMask;
    }
    
    public void addApplicationToCategory(AppDetails toBeAdded) {
        int size = mRelatedApps.size();
        addAppToList(toBeAdded);
        if (size < mRelatedApps.size())
            Collections.sort(mRelatedApps, AppDetails.SMART_COMPARATOR);
    }

    private void addAppToList(AppDetails toBeAdded) {
        // Checking if any subcategory fits
        if (!toBeAdded.isInSubcategory(mSubCategoriesMask)
                || (mCanSendData && !toBeAdded
                        .isInSubcategory(Subcategory.CAN_SEND_DATA_SUB_CATEGORY_ID)))
            return;
        mRelatedApps.add(toBeAdded);
    }

    public List<Subcategory> getSubCategories() {
        return mSubCategories;
    }

    /**
     * Create app list
     */
    public void assignAppsToCategory(List<AppDetails> appDetailsList) {
        mRelatedApps.clear();

        if (appDetailsList != null && mSubCategories != null) {
            for (AppDetails appDetails : appDetailsList) {
                addAppToList(appDetails);
            }
        }
    }

    /**
     * @return the mRelatedApps
     */
    public ArrayList<AppDetails> getRelatedApps() {
        return mRelatedApps;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public Drawable getIconDrawable() {
        return mIconDrawable;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getHeader() {
        return mHeader;
    }

    public String getShortDescription() {
        return mShortDescription;
    }

    public void updateVisibleCount(int size) {
        mCurrentlyVisible = size;
    }

    public int getCurrentlyVisible() {
        return mCurrentlyVisible;
    }

    public String getLink() {
        return mLink;
    }
}
