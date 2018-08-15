package com.jparkie.aizoban.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Manga implements Parcelable {
    public static final String TAG = Manga.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Creator<Manga> CREATOR = new Creator<Manga>() {
        @Override
        public Manga createFromParcel(Parcel inputParcel) {
            return new Manga(inputParcel);
        }

        @Override
        public Manga[] newArray(int size) {
            return new Manga[size];
        }
    };

    private Long _id;

    private String Source;
    private String Url;

    private String Artist;
    private String Author;
    private String Description;
    private String Genre;
    private String Name;
    private boolean Completed;
    private String ThumbnailUrl;

    private int Rank;
    private long Updated;
    private int UpdateCount;

    private boolean Initialized;

    public Manga() {
    }

    private Manga(Parcel inputParcel) {
        _id = inputParcel.readLong();
        if (_id < 0) {
            _id = null;
        }

        Source = inputParcel.readString();
        Url = inputParcel.readString();

        Artist = inputParcel.readString();
        Author = inputParcel.readString();
        Description = inputParcel.readString();
        Genre = inputParcel.readString();
        Name = inputParcel.readString();
        Completed = inputParcel.readByte() != 0;
        ThumbnailUrl = inputParcel.readString();

        Rank = inputParcel.readInt();
        Updated = inputParcel.readLong();
        UpdateCount = inputParcel.readInt();

        Initialized = inputParcel.readByte() != 0;
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

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isCompleted() {
        return Completed;
    }

    public void setCompleted(boolean isCompleted) {
        Completed = isCompleted;
    }

    public String getThumbnailUrl() {
        return ThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        ThumbnailUrl = thumbnailUrl;
    }

    public int getRank() {
        return Rank;
    }

    public void setRank(int rank) {
        Rank = rank;
    }

    public long getUpdated() {
        return Updated;
    }

    public void setUpdated(long updated) {
        Updated = updated;
    }

    public int getUpdateCount() {
        return UpdateCount;
    }

    public void setUpdateCount(int updateCount) {
        UpdateCount = updateCount;
    }

    public boolean isInitialized() {
        return Initialized;
    }

    public void setInitialized(boolean initialized) {
        Initialized = initialized;
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

        outputParcel.writeString(Artist);
        outputParcel.writeString(Author);
        outputParcel.writeString(Description);
        outputParcel.writeString(Genre);
        outputParcel.writeString(Name);
        outputParcel.writeByte((byte) (Completed ? 1 : 0));
        outputParcel.writeString(ThumbnailUrl);

        outputParcel.writeInt(Rank);
        outputParcel.writeLong(Updated);
        outputParcel.writeInt(UpdateCount);

        outputParcel.writeByte((byte) (Initialized ? 1 : 0));
    }
}