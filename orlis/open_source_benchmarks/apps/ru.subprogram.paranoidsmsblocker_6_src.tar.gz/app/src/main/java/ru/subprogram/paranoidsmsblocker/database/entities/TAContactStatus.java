package ru.subprogram.paranoidsmsblocker.database.entities;

public enum TAContactStatus {
	EWhiteList(1),
	EBlackList(2),
	;
	
	private final int mValue;
	
	TAContactStatus(int value) {
		mValue = value;
	}
	
	public int getValue() {
		return  mValue;
	}	
	
	public static TAContactStatus getEnum(int value){
		TAContactStatus[] values = TAContactStatus.values();

		for (TAContactStatus v : values) {
			if (v.getValue() == value) {
				return v;
			}
		}
		
		return null;
	}
	
}
