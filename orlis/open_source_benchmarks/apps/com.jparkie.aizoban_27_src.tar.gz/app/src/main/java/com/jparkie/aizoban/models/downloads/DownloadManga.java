package com.jparkie.aizoban.models.downloads;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadManga implements Parcelable {
    public static final String TAG = DownloadManga.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Parcelable.Creator<DownloadManga> CREATOR = new Parcelable.Creator<DownloadManga>() {
        @Override
        public DownloadManga createFromParcel(Parcel inputParcel) {
            return new DownloadManga(inputParcel);
        }

        @Override
        public DownloadManga[] newArray(int size) {
            return new DownloadManga[size];
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

    public DownloadManga() {}

    private DownloadManga(Parcel inputParcel) {
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
    }
}
