package com.jparkie.aizoban.utils.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SearchCatalogueWrapper implements Parcelable {
    public static final String TAG = SearchCatalogueWrapper.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Creator<SearchCatalogueWrapper> CREATOR = new Creator<SearchCatalogueWrapper>() {
        @Override
        public SearchCatalogueWrapper createFromParcel(Parcel inputParcel) {
            return new SearchCatalogueWrapper(inputParcel);
        }

        @Override
        public SearchCatalogueWrapper[] newArray(int size) {
            return new SearchCatalogueWrapper[size];
        }
    };

    private String mNameArgs;
    private String mStatusArgs;
    private String mOrderByArgs;
    private List<String> mGenresArgs;
    private int mOffsetArgs;

    public SearchCatalogueWrapper() {
    }

    private SearchCatalogueWrapper(Parcel inputParcel) {
        mNameArgs = inputParcel.readString();
        mStatusArgs = inputParcel.readString();
        mOrderByArgs = inputParcel.readString();

        mGenresArgs = new ArrayList<String>();
        inputParcel.readStringList(mGenresArgs);

        mOffsetArgs = inputParcel.readInt();
    }

    public String getNameArgs() {
        return mNameArgs;
    }

    public void setNameArgs(String nameArgs) {
        mNameArgs = nameArgs;
    }

    public String getStatusArgs() {
        return mStatusArgs;
    }

    public void setStatusArgs(String statusArgs) {
        mStatusArgs = statusArgs;
    }

    public String getOrderByArgs() {
        return mOrderByArgs;
    }

    public void setOrderByArgs(String orderByArgs) {
        mOrderByArgs = orderByArgs;
    }

    public List<String> getGenresArgs() {
        return mGenresArgs;
    }

    public void setGenresArgs(List<String> genresArgs) {
        mGenresArgs = genresArgs;
    }

    public int getOffsetArgs() {
        return mOffsetArgs;
    }

    public void setOffsetArgs(int offsetArgs) {
        mOffsetArgs = offsetArgs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outputParcel, int flags) {
        outputParcel.writeString(mNameArgs);
        outputParcel.writeString(mStatusArgs);
        outputParcel.writeString(mOrderByArgs);
        outputParcel.writeStringList(mGenresArgs);
        outputParcel.writeInt(mOffsetArgs);
    }
}
