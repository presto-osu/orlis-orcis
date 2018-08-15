package vnd.blueararat.kaleidoscope6;

import android.content.Context;
import android.os.Environment;
import android.preference.Preference;
import android.util.AttributeSet;

public class FolderPref extends Preference {

	private String mString, mDefault;

	public FolderPref(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefault = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES).toString();
		setDefaultValue(mDefault);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mString = getPersistedString(mDefault);
		else
			mString = (String) defaultValue;
		setSummary(mString);
	}

	void reset() {
		mString = mDefault;
		setSummary(mString);
		persistString(mString);
	}

	void setString(String s) {
		mString = s;
		setSummary(s);
		persistString(s);
	}

	String getString() {
		return mString;
	}

	// @Override
	// protected void onClick() {
	// String newValue = mString + "3";
	// if (!callChangeListener(newValue)) {
	// return;
	// }
	// mString = newValue;
	// persistString(mString);
	// setSummary(mString);
	// notifyChanged();
	// }
}
