package com.jparkie.aizoban.models.databases;

import android.os.Parcel;
import android.os.Parcelable;

public class FavouriteManga implements Parcelable {
    public static final String TAG = FavouriteManga.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Creator<FavouriteManga> CREATOR = new Creator<FavouriteManga>() {
        @Override
        public FavouriteManga createFromParcel(Parcel inputParcel) {
            return new FavouriteManga(inputParcel);
        }

        @Override
        public FavouriteManga[] newArray(int size) {
            return new FavouriteManga[size];
        }
    };

    private Long _id;

    private String Source;
    private String Url;

    private String Name;
    private String ThumbnailUrl;

    public FavouriteManga() {
    }

    private FavouriteManga(Parcel inputParcel) {
        _id = inputParcel.readLong();
        if (_id < 0) {
            _id = null;
        }

        Source = inputParcel.readString();
        Url = inputParcel.readString();

        Name = inputParcel.readString();
        ThumbnailUrl = inputParcel.readString();
    }

    public Long getId() {
        return _id;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getThumbnailUrl() {
        return ThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        ThumbnailUrl = thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outputParcel, int flags) {
        if (_id != null) {
            outputParcel.writeLong(_id);
        } else {
            outputParcel.writeLong(-1);
        }

        outputParcel.writeString(Source);
        outputParcel.writeString(Url);

        outputParcel.writeString(Name);
        outputParcel.writeString(ThumbnailUrl);
    }
}
