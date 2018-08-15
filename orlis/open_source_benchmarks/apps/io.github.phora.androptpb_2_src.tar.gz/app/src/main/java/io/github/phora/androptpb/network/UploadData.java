package io.github.phora.androptpb.network;

/**
 * Created by phora on 8/24/15.
 */
public class UploadData {
    private String serverUrl;
    private String token;
    private String vanity;
    private String uuid;
    private String sha1sum;
    private boolean isPrivate;
    private Long sunset;
    private String preferredHint;

    public UploadData(String serverUrl, String token, String vanity, String uuid, String sha1sum, boolean isPrivate, Long sunset) {
        this.serverUrl = serverUrl;
        this.token = token;
        this.uuid = uuid;
        this.sha1sum = sha1sum;
        this.isPrivate = isPrivate;
        this.sunset = sunset;
        this.vanity = vanity;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String server_url) {
        this.serverUrl = server_url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String fpath) {
        this.uuid = fpath;
    }

    public String getSha1sum() {
        return sha1sum;
    }

    public void setSha1sum(String sha1sum) {
        this.sha1sum = sha1sum;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean is_private) {
        this.isPrivate = is_private;
    }

    public Long getSunset() {
        return sunset;
    }

    public void setSunset(Long sunset) {
        this.sunset = sunset;
    }

    public String getVanity() {
        return vanity;
    }

    public void setVanity(String vanity) {
        this.vanity = vanity;
    }

    public String getPreferredHint() {
        return preferredHint;
    }

    public void setPreferredHint(String preferred_hint) {
        this.preferredHint = preferred_hint;
    }
}
