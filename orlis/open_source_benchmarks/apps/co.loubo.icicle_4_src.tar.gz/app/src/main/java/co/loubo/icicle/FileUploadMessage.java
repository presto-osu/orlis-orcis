package co.loubo.icicle;

import android.net.Uri;

public class FileUploadMessage {
	
	private Uri uri;
	private String mimeType;
	private String key;
    private String name;
    private long size;
	public Uri getUri() {
		return uri;
	}
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }
}