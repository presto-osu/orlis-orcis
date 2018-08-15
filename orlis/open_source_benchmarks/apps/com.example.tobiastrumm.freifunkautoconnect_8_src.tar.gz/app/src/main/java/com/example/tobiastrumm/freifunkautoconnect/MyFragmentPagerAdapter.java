package com.example.tobiastrumm.freifunkautoconnect;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private final static int PAGE_COUNT = 2;
    private String tabTitles[];

    public AddRemoveNetworksFragment addRemoveNetworksFragment;
    public NearestNodesFragment nearestNodesFragment;

    public MyFragmentPagerAdapter(FragmentManager fm, String tabTitles[]){
        super(fm);
        this.tabTitles = tabTitles;
    }
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return NearestNodesFragment.newInstance();
            case 1:
                return AddRemoveNetworksFragment.newInstance();

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    /**
     * Overwriting is necessary to get a reference to the Fragments created by the FragmentPageAdapter.
     * See http://stackoverflow.com/a/29269509/5354001 for more information.
     * @param container
     * @param position
     * @return
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        switch(position){
            case 0:
                nearestNodesFragment = (NearestNodesFragment) createdFragment;
                break;
            case 1:
                addRemoveNetworksFragment = (AddRemoveNetworksFragment) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position<tabTitles.length){
            return tabTitles[position];
        }
        return "";
    }
}
