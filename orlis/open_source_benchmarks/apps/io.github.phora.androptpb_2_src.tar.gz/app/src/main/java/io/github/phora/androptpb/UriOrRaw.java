package io.github.phora.androptpb;

import android.net.Uri;

/**
 * Created by phora on 9/3/15.
 */
public class UriOrRaw {
    boolean hasUri;
    boolean hasRaw;

    Uri uri;
    byte[] rawData;

    public UriOrRaw(Uri uri) {
        this.uri = uri;
        hasUri = (uri != null);
    }

    public UriOrRaw(byte[] rawData) {
        this.rawData = rawData;
        hasRaw = (rawData != null);
    }

    public boolean hasUri() {
        return hasUri;
    }

    public boolean hasRaw() {
        return hasRaw;
    }

    public Uri getUri() {
        return uri;
    }

    public byte[] getRawData() {
        return rawData;
    }
}
