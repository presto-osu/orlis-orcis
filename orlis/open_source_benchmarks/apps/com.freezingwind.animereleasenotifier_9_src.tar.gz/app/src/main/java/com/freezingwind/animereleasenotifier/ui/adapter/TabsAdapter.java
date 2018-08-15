package com.freezingwind.animereleasenotifier.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.freezingwind.animereleasenotifier.ui.animelist.AnimeListFragment;
import com.freezingwind.animereleasenotifier.ui.animelist.CompletedListFragment;

public class TabsAdapter extends FragmentPagerAdapter {
	public TabsAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
			case 0:
				// Watching
				return new AnimeListFragment();
			case 1:
				// Completed
				return new CompletedListFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}
}
