package io.github.phora.androptpb.network;

import io.github.phora.androptpb.UriOrRaw;

/**
 * Created by phora on 9/13/15.
 */
public class UUIDLocalIDPair {
    private String server;
    private String uuid;
    private long localId;

    private UriOrRaw optData;
    private Boolean optPrivate = null;

    public UUIDLocalIDPair(String server, String uuid, long localId) {
        this.server = server;
        this.uuid = uuid;
        this.localId = localId;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public UriOrRaw getOptData() {
        return optData;
    }

    public void setOptData(UriOrRaw optData) {
        this.optData = optData;
    }

    public Boolean getOptPrivate() {
        return optPrivate;
    }

    public void setOptPrivate(Boolean optPrivate) {
        this.optPrivate = optPrivate;
    }
}
