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

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.gabm.fancyplaces.data.FancyPlace;

/**
 * Created by gabm on 24.12.14.
 */
public class SEPState implements Parcelable {

    public static final Parcelable.Creator<SEPState> CREATOR
            = new Parcelable.Creator<SEPState>() {
        public SEPState createFromParcel(Parcel in) {
            return new SEPState(in);
        }

        public SEPState[] newArray(int size) {
            return new SEPState[size];
        }
    };
    public int mode;
    public int result_code = 0;
    public FancyPlace data = null;
    public Bitmap image = null;
    // visibilities
    public ViewElementVisibility viewElementVisibility = new ViewElementVisibility();

    public SEPState() {
    }

    public SEPState(Parcel in) {
        mode = in.readInt();
        result_code = in.readInt();

        data = in.readParcelable(FancyPlace.class.getClassLoader());
        image = in.readParcelable(Bitmap.class.getClassLoader());

        viewElementVisibility = in.readParcelable(ViewElementVisibility.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mode);
        dest.writeInt(result_code);

        dest.writeParcelable(data, flags);
        dest.writeParcelable(image, flags);

        dest.writeParcelable(viewElementVisibility, flags);
    }

    public class ViewElementVisibility implements Parcelable {

        // title
        public int titleEditTextVisibility = View.GONE;

        // map
        public int mapCardVisibility = View.GONE;
        public int mapVisibility = View.GONE;
        public int mapUpdateButtonVisibility = View.GONE;
        // note
        public int notesCardVisibility = View.GONE;
        public int notesTextViewVisibility = View.GONE;
        public int notesEditTextVisibility = View.GONE;


        public ViewElementVisibility() {
        }

        public ViewElementVisibility(Parcel in) {
            titleEditTextVisibility = in.readInt();

            mapCardVisibility = in.readInt();
            mapUpdateButtonVisibility = in.readInt();
            mapVisibility = in.readInt();

            notesCardVisibility = in.readInt();
            notesEditTextVisibility = in.readInt();
            notesTextViewVisibility = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(titleEditTextVisibility);

            dest.writeInt(mapCardVisibility);
            dest.writeInt(mapUpdateButtonVisibility);
            dest.writeInt(mapVisibility);

            dest.writeInt(notesCardVisibility);
            dest.writeInt(notesEditTextVisibility);
            dest.writeInt(notesTextViewVisibility);

        }
    }

}