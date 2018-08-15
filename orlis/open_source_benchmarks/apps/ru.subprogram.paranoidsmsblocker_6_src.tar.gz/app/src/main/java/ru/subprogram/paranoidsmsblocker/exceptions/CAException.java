package ru.subprogram.paranoidsmsblocker.exceptions;

public class CAException extends Exception {

	private int mCode = CAError.NO_ERROR;
	
	private Exception mOriginalException;
	
	public CAException(int error_code) {
		super();
		mCode = error_code;
	}
	
	public CAException(int error_code, Exception e) {
		super();
		mCode = error_code;
		mOriginalException = e;
	}

	public int getErrorCode() {
		return mCode;
	}

	@Override
	public String getMessage() {
		return "CAException " + mCode
			+ (mOriginalException!=null ? ", "+mOriginalException.getMessage() : "");
	}
	
	public Exception getOriginalException () {
		return mOriginalException;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof CAException))
			return false;
		
		CAException e = (CAException) o;

		return mCode == e.getErrorCode() && 
				((mOriginalException == null && e.getOriginalException() == null)
					|| (mOriginalException != null
						&& e.getOriginalException() != null
						&& mOriginalException.equals(e.getOriginalException())));
	}
	
}
