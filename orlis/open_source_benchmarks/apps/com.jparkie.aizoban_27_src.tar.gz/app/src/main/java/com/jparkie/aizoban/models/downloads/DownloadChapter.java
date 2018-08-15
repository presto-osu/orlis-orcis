package com.jparkie.aizoban.models.downloads;

import android.os.Parcel;
import android.os.Parcelable;

public class DownloadChapter implements Parcelable {
    public static final String TAG = DownloadChapter.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Creator<DownloadChapter> CREATOR = new Creator<DownloadChapter>() {
        @Override
        public DownloadChapter createFromParcel(Parcel inputParcel) {
            return new DownloadChapter(inputParcel);
        }

        @Override
        public DownloadChapter[] newArray(int size) {
            return new DownloadChapter[size];
        }
    };

    private Long _id;

    private String Source;
    private String Url;
    private String ParentUrl;

    private String Name;

    private String Directory;

    private int CurrentPage;
    private int TotalPages;
    private int Flag;

    public DownloadChapter() {}

    private DownloadChapter(Parcel inputParcel) {
        _id = inputParcel.readLong();
        if (_id < 0) {
            _id = null;
        }

        Source = inputParcel.readString();
        Url = inputParcel.readString();
        ParentUrl = inputParcel.readString();

        Name = inputParcel.readString();

        Directory = inputParcel.readString();

        CurrentPage = inputParcel.readInt();
        TotalPages = inputParcel.readInt();
        Flag = inputParcel.readInt();
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

    public String getDirectory() {
        return Directory;
    }

    public void setDirectory(String directory) {
        Directory = directory;
    }

    public int getCurrentPage() {
        return CurrentPage;
    }

    public void setCurrentPage(int currentPage) {
        CurrentPage = currentPage;
    }

    public int getTotalPages() {
        return TotalPages;
    }

    public void setTotalPages(int totalPages) {
        TotalPages = totalPages;
    }

    public int getFlag() {
        return Flag;
    }

    public void setFlag(int flag) {
        Flag = flag;
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

        outputParcel.writeString(Directory);

        outputParcel.writeInt(CurrentPage);
        outputParcel.writeInt(TotalPages);
        outputParcel.writeInt(Flag);
    }
}
