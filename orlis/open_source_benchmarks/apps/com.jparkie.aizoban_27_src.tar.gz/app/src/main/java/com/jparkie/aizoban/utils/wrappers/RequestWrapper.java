package com.jparkie.aizoban.utils.wrappers;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestWrapper implements Parcelable {
    public static final String TAG = RequestWrapper.class.getSimpleName();

    public static final String PARCELABLE_KEY = TAG + ":" + "ParcelableKey";
    public static final Parcelable.Creator<RequestWrapper> CREATOR = new Parcelable.Creator<RequestWrapper>() {
        @Override
        public RequestWrapper createFromParcel(Parcel inputParcel) {
            return new RequestWrapper(inputParcel);
        }

        @Override
        public RequestWrapper[] newArray(int size) {
            return new RequestWrapper[size];
        }
    };

    private String mSource;
    private String mUrl;

    public RequestWrapper(String source, String url) {
        mSource = source;
        mUrl = url;
    }

    private RequestWrapper(Parcel inputParcel) {
        mSource = inputParcel.readString();
        mUrl = inputParcel.readString();
    }

    public String getSource() {
        return mSource;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outputParcel, int flags) {
        outputParcel.writeString(mSource);
        outputParcel.writeString(mUrl);
    }
}
