package com.luk.timetable2.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by LuK on 2015-10-03.
 */
public class MainActivityAdapter extends FragmentStatePagerAdapter {
    public MainActivityAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt(MainActivityFragment.ARG_DAY, position);

        Fragment fragment = new MainActivityFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }
}
