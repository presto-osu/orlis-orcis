package ru.subprogram.paranoidsmsblocker.adapters;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import ru.subprogram.paranoidsmsblocker.R;
import ru.subprogram.paranoidsmsblocker.fragments.CAAbstractFragment;
import ru.subprogram.paranoidsmsblocker.fragments.CABlackListFragment;
import ru.subprogram.paranoidsmsblocker.fragments.CASmsListFragment;
import ru.subprogram.paranoidsmsblocker.fragments.CAWhiteListFragment;

public class CATabCollectionPagerAdapter extends FragmentStatePagerAdapter {

	private final AppCompatActivity mActivity;

	private CABlackListFragment mBlackListFragment;
	private CAWhiteListFragment mWhiteListFragment;
	private CASmsListFragment mBlockedSmsFragment;
	
    public CATabCollectionPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
		mActivity = activity;
    }

    @Override
    public CAAbstractFragment getItem(int position) {
    	switch (position) {
		case 0:
			if(mBlackListFragment==null)
				mBlackListFragment = new CABlackListFragment();
			return mBlackListFragment;
		case 1:
			if(mWhiteListFragment==null)
				mWhiteListFragment = new CAWhiteListFragment();
			return mWhiteListFragment;
		case 2:
			if(mBlockedSmsFragment==null)
				mBlockedSmsFragment = new CASmsListFragment();
			return mBlockedSmsFragment;

		default:
			return null;
		}
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
		switch (position) {
			case 0:
				return mActivity.getString(R.string.black_list_tab);
			case 1:
				return mActivity.getString(R.string.white_list_tab);
			case 2:
				return mActivity.getString(R.string.blocked_sms_list_tab);

			default:
				return null;
		}
    }

}