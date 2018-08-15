package com.claha.showtimeremote.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

public class BaseFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final List<Class<? extends BaseFragment>> fragments;

    public BaseFragmentPagerAdapter(FragmentManager fragmentManager, List<Class<? extends BaseFragment>> fragments) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        try {
            return fragments.get(position).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

}
