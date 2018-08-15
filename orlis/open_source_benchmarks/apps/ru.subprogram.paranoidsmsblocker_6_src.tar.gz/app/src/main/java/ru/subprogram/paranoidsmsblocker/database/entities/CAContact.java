package ru.subprogram.paranoidsmsblocker.database.entities;

import java.io.Serializable;

public class CAContact implements Serializable {

	private static final long serialVersionUID = -6005381755143836060L;
	
	private long mId;
	private final String mAddress;
	private TAContactStatus mStatus;
	
	public CAContact(TAContactStatus status, String address) {
		mId = -1;
		mStatus = status;
		mAddress = address;
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

	@Override
	public boolean equals(Object o) {
		if(o!=null && o instanceof CAContact) {
			CAContact item = (CAContact)o;
			return mId==item.mId && mAddress.equals(item.mAddress);
		}
		return false;
	}

	public TAContactStatus getStatus() {
		return mStatus;
	}

	public void setStatus(TAContactStatus status) {
		mStatus = status;
	}
}
