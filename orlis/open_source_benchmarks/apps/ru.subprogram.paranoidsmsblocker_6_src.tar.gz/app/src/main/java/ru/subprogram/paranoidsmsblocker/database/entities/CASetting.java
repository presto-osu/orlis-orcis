package ru.subprogram.paranoidsmsblocker.database.entities;

public class CASetting {

	private TASetting mId;
	private String mValue;
	
	public CASetting(){
		mValue = ""; 
	}
	
	public TASetting getId() {
		return mId;
	}

	public void setId(TASetting id) {
		mId = id;
	}

	public String getValStr() {
		return mValue;
	}

	public void setValStr(String value) {
		mValue = value;
	}

	public int getValInt() {
		return Integer.parseInt(mValue);
	}

	public void setValInt(int value) {
		mValue = Integer.toString(value);
	}

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
	 
		if(obj == null || obj.getClass() != this.getClass()){
			return false;
		}
		
		CASetting co = (CASetting)obj;
		
		if ((this.mId != co.mId)
			||(this.mValue == null && co.mValue != null)
			||(this.mValue != null && !this.mValue.equals(co.mValue))
			){
			return false;
		}
		
		return true;
	 }		
}
