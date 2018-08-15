/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.ui;

import android.os.Parcel;
import android.os.Parcelable;

import com.gabm.fancyplaces.data.ImageFile;

/**
 * Created by gabm on 29.12.14.
 */
public class LFPState implements Parcelable {
    public int mode = 1;
    public ImageFile OriginalImageFile = null;
    public int curMenu = 0;

    LFPState() {
    }

    LFPState(Parcel in) {
        mode = in.readInt();
        OriginalImageFile = in.readParcelable(ImageFile.class.getClassLoader());
        curMenu = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeParcelable(OriginalImageFile, flags);
        dest.writeInt(curMenu);

    }
}
