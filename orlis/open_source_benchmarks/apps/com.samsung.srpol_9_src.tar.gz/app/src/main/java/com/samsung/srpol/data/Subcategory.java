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

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

public class Subcategory {

    public static final int CAN_SEND_DATA_SUB_CATEGORY_ID = 1;
    public static final String CAN_SEND_DATA_SUB_CATEGORY_HEADER = "uprawnienia do przesyłu danych";
    private static final String DARK_SUFIX = "_dark";
    private static final String DISABLE_SUFIX = "_disable";
    private static int mIdGenerator = 2;

    private int mSubcatId;
    private String mHeader;
    private String mDescription;
    private int mIconRes;
    private Drawable mIconDrawable;
    private Drawable mIconDarkDrawable;
    private Drawable mIconDisabledDrawable;
    private ArrayList<String> mPermissions;

    public Subcategory(Context context, String header, String description,
            String icon, ArrayList<String> permissions) {
        mHeader = header;
        mDescription = description;
        if (mHeader.equals(CAN_SEND_DATA_SUB_CATEGORY_HEADER)) {
            mSubcatId = CAN_SEND_DATA_SUB_CATEGORY_ID;
        } else {
            mSubcatId = mIdGenerator;
            mIdGenerator = mIdGenerator << 1;
        }

        String packageName = context.getPackageName();
        mIconRes = context.getResources().getIdentifier(icon, "drawable",
                packageName);
        mIconDrawable = context.getResources().getDrawable(mIconRes);

        int iconDarkRes = context.getResources().getIdentifier(
                icon.concat(DARK_SUFIX), "drawable", packageName);
        try {
            mIconDarkDrawable = context.getResources().getDrawable(iconDarkRes);
        } catch (NotFoundException ex) {
            mIconDarkDrawable = mIconDrawable;
        }
        
        int iconDisableRes = context.getResources().getIdentifier(icon.concat(DISABLE_SUFIX), "drawable", packageName);
        try {
            mIconDisabledDrawable = context.getResources().getDrawable(iconDisableRes);
        } catch (NotFoundException ex) {
            mIconDisabledDrawable = mIconDrawable;
        }
        
        mPermissions = permissions;
    }

    public ArrayList<String> getPermissions() {
        return mPermissions;
    }

    public int getId() {
        return mSubcatId;
    }

    public String getHeader() {
        return mHeader;
    }

    public Drawable getIconDrawable() {
        return mIconDrawable;
    }

    public Drawable getDarkIcon() {
        return mIconDarkDrawable;
    }
    
    public Drawable getDisabledIcon() {
        return mIconDisabledDrawable;
    }

    public String getDescription() {
        return mDescription;
    }

    public static void resetGenerator() {
        mIdGenerator = 2;
    }

}
