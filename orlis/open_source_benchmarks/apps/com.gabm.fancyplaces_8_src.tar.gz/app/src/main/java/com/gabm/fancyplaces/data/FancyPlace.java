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

package com.gabm.fancyplaces.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by gabm on 22.12.14.
 */
public class FancyPlace implements Parcelable, Serializable {
    public static final Parcelable.Creator<FancyPlace> CREATOR
            = new Parcelable.Creator<FancyPlace>() {
        public FancyPlace createFromParcel(Parcel in) {
            return new FancyPlace(in);
        }

        public FancyPlace[] newArray(int size) {
            return new FancyPlace[size];
        }
    };
    // private data fields
    private String sTitle = "";
    private String sNotes = "";
    private String sLocation_lat = "";
    private String sLocation_long = "";
    private ImageFile oImage = new ImageFile();
    private long id = 0;
    private Boolean bIsInDatabase = false;

    public FancyPlace() {
    }

    public FancyPlace(Parcel in) {
        sTitle = in.readString();
        sNotes = in.readString();
        sLocation_lat = in.readString();
        sLocation_long = in.readString();
        oImage = in.readParcelable(ImageFile.class.getClassLoader());
        id = in.readLong();
        bIsInDatabase = (boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public FancyPlace(FancyPlace other) {
        this(other.getTitle(), other.getNotes(), other.getLocationLat(), other.getLocationLong(), other.getImage(), other.getId(), other.isInDatabase());
    }

    public FancyPlace(String title, String notes, String location_lat, String location_long, ImageFile image, long dataBaseID, Boolean isInDatabase) {
        sTitle = title;
        sNotes = notes;
        sLocation_lat = location_lat;
        sLocation_long = location_long;
        oImage = image;
        bIsInDatabase = isInDatabase;
        id = dataBaseID;
    }

    public static FancyPlace loadFromFile(ContentResolver resolver, Uri uri) {
        FancyPlace result = null;
        try {
            InputStream is = resolver.openInputStream(uri);
            ObjectInputStream ois = new ObjectInputStream(is);

            result = (FancyPlace) ois.readObject();
            result.bIsInDatabase = false;
            result.id = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sTitle);
        dest.writeString(sNotes);
        dest.writeString(sLocation_lat);
        dest.writeString(sLocation_long);
        dest.writeParcelable(oImage, flags);
        dest.writeLong(id);
        dest.writeValue(bIsInDatabase);

    }

    public String getTitle() {
        return sTitle;
    }

    public void setTitle(String sTitle) {
        this.sTitle = sTitle;
    }

    public String getNotes() {
        return sNotes;
    }

    public void setNotes(String sNotes) {
        this.sNotes = sNotes;
    }

    public String getLocationLat() {
        return sLocation_lat;
    }

    public void setLocationLat(String sLocation_lat) {
        this.sLocation_lat = sLocation_lat;
    }

    public String getLocationLong() {
        return sLocation_long;
    }

    public void setLocationLong(String sLocation_long) {
        this.sLocation_long = sLocation_long;
    }

    public ImageFile getImage() {
        return oImage;
    }

    public void setImage(ImageFile imageFile) {
        oImage = imageFile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return sTitle;
    }

    public FancyPlace clone() {
        return new FancyPlace(this);
    }

    public Boolean isInDatabase() {
        return bIsInDatabase;
    }

    public void setIsInDatabase(Boolean isInDatabase) {
        bIsInDatabase = isInDatabase;
    }

    public Boolean isLocationSet() {
        return (!getLocationLong().equals("") && !getLocationLat().equals(""));
    }

    public Boolean isTitleSet() {
        return !sTitle.equals("");
    }

    public Boolean isValid() {
        return isTitleSet() && isLocationSet();
    }

    public String saveToFile(String directory) {
        String fileName = directory + File.separator + getTitle().replaceAll(" ", "_").toLowerCase() + ".fancyplace";

        try {
            File cacheFile = new File(fileName);
            cacheFile.createNewFile();

            FileOutputStream fis = new FileOutputStream(cacheFile);
            ObjectOutputStream oos = new ObjectOutputStream(fis);

            oos.writeObject(this);
            oos.close();
            fis.close();
        } catch (Exception e) {
            fileName = "";
            e.printStackTrace();
        }
        return fileName;
    }


}
