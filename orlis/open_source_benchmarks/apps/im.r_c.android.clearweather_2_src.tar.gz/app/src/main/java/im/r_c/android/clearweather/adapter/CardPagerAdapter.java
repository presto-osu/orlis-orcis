package im.r_c.android.clearweather.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import im.r_c.android.clearweather.fragment.CardFragment;
import im.r_c.android.clearweather.model.County;

/**
 * ClearWeather
 * Created by richard on 16/4/29.
 */
public class CardPagerAdapter extends FragmentStatePagerAdapter {
    private List<County> mDataList;

    public CardPagerAdapter(FragmentManager fm, List<County> dataList) {
        super(fm);
        mDataList = dataList;
    }

    @Override
    public Fragment getItem(int position) {
        return CardFragment.newInstance(mDataList.get(position));
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        // Override this method to force the adapter to destroy deleted page
        return POSITION_NONE;
    }
}
