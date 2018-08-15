package com.jparkie.aizoban.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Chapter implements Parcelable {
    public static final String TAG = Chapter.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel inputParcel) {
            return new Chapter(inputParcel);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    private Long _id;

    private String Source;
    private String Url;
    private String ParentUrl;

    private String Name;
    private boolean New;
    private long Date;

    private int Number;

    public Chapter() {
    }

    private Chapter(Parcel inputParcel) {
        _id = inputParcel.readLong();
        if (_id < 0) {
            _id = null;
        }

        Source = inputParcel.readString();
        Url = inputParcel.readString();
        ParentUrl = inputParcel.readString();

        Name = inputParcel.readString();
        New = inputParcel.readByte() != 0;
        Date = inputParcel.readLong();

        Number = inputParcel.readInt();
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

    public String getParentUrl() {
        return ParentUrl;
    }

    public void setParentUrl(String parentUrl) {
        ParentUrl = parentUrl;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isNew() {
        return New;
    }

    public void setNew(boolean isNew) {
        New = isNew;
    }

    public long getDate() {
        return Date;
    }

    public void setDate(long date) {
        Date = date;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
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
        outputParcel.writeString(ParentUrl);

        outputParcel.writeString(Name);
        outputParcel.writeByte((byte) (New ? 1 : 0));
        outputParcel.writeLong(Date);

        outputParcel.writeInt(Number);
    }
}
