package ru.subprogram.paranoidsmsblocker.database.entities;

import java.io.Serializable;

public class CASms implements Serializable {

	private static final long serialVersionUID = 3117337919561484548L;
	
	private long mId;
	private String mAddress;
	private String mText;
	private final long mDate;
	
	public CASms(String address, String text, long date) {
		mId = -1;
		mAddress = address;
		mText = text;
		mDate = date; 
	}

	public void setId(long id) {
		mId = id;
	}

	public long getId() {
		return mId;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}
	
	public String getText() {
		return mText;
	}

	public void setText(String text) {
		mText = text;
	}
	
	public long getDate() {
		return mDate;
	}

	@Override
	public boolean equals(Object o) {
		if(o!=null && o instanceof CASms) {
			CASms item = (CASms)o;
			return mId==item.mId
				&& mAddress.equals(item.mAddress)
				&& mText.equals(item.mText);
		}
		return false;
	}
}
