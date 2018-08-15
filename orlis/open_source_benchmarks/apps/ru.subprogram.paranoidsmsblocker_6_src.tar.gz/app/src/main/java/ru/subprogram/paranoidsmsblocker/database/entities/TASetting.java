package ru.subprogram.paranoidsmsblocker.database.entities;

public enum TASetting {
	EDbVersion(1),
	;
	
	private final int mValue;
	
	TASetting(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return  mValue;
	}	
	
	public static TASetting getEnum(int value){
		TASetting[] values = TASetting.values();

		for (TASetting v : values) {
			if (v.getValue() == value) {
				return v;
			}
		}
		
		return null;
	}
	
}
