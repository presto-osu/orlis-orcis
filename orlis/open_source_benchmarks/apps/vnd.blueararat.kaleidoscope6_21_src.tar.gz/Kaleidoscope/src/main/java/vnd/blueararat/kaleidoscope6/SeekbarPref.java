package vnd.blueararat.kaleidoscope6;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekbarPref extends Preference implements OnSeekBarChangeListener {

	private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
	private static final String ADDITIONAL = "vnd.blueararat.kaleidoscope";

	// private final String TAG = getClass().getName();
	private int mMin;
	private int mMax;// R.string.nom_max;
	private int mValue, mDefault;
	// private int mInterval = 1;
	// private int mCurrentValue;
	private SeekBar mSeekBar;
	private TextView mTextView;

	public SeekbarPref(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefault = attrs.getAttributeIntValue(ANDROIDNS, "defaultValue", 4);
		mMax = attrs.getAttributeIntValue(ANDROIDNS, "max", 25);
		mMin = attrs.getAttributeIntValue(ADDITIONAL, "min", 2);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {

		LinearLayout layout = null;

		try {
			LayoutInflater mInflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			layout = (LinearLayout) mInflater.inflate(R.layout.seekbarpref,
					parent, false);
		} catch (Exception e) {
			// Log.e(TAG, "Error creating seek bar preference", e);
		}

		return layout;

	}

	@Override
	public void onBindView(View view) {
		super.onBindView(view);

		try {
			LinearLayout layout = (LinearLayout) view;
			mSeekBar = (SeekBar) layout.findViewById(R.id.seekBar1);
			mTextView = (TextView) layout.findViewById(R.id.Value);
			mSeekBar.setMax(mMax);
			if (shouldPersist())
				mValue = getPersistedInt(mDefault);
			mSeekBar.setProgress(mValue);
			mTextView.setText(String.valueOf(mValue + mMin));
			mSeekBar.setOnSeekBarChangeListener(this);
			// mTextView.setText(String.valueOf(mCurrentValue));
		} catch (Exception e) {
			// Log.e(TAG, "Error updating seek bar preference", e);
		}

	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {
		mValue = progress;
		mTextView.setText(String.valueOf(mValue + mMin));
		if (!fromTouch) {
			if (shouldPersist())
				persistInt(mValue);
			callChangeListener(Integer.valueOf(mValue));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		if (shouldPersist())
			persistInt(mValue);
		callChangeListener(Integer.valueOf(mValue));
	}

	public void setProgressValue(int value) {
		mSeekBar.setProgress(value);
		// mTextView.setText(value);
		// mValue = value;
		// if (shouldPersist())
		// persistInt(mValue);
		// callChangeListener(new Integer(mValue));
	}
}
