package ru.subprogram.paranoidsmsblocker.fragments;

import ru.subprogram.paranoidsmsblocker.activities.IAMainActivityFragmentObserver;
import android.app.Activity;
import android.support.v4.app.Fragment;

public abstract class CAAbstractFragment extends Fragment {

	protected IAMainActivityFragmentObserver mObserver;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		mObserver = (IAMainActivityFragmentObserver)activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mObserver = null;
	}

	public abstract void updateContent();
	
}
