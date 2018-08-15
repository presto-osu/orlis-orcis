package com.jparkie.aizoban.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.jparkie.aizoban.views.fragments.PageFragment;

import java.util.List;

public class PagesAdapter extends BaseFragmentStatePagerAdapter {
    private List<String> mImageUrls;

    private boolean mIsRightToLeftDirection;

    public PagesAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        if (mImageUrls != null) {
            return mImageUrls.size();
        } else {
            return 0;
        }
    }

    @Override
    public String getTag(int position) {
        return PageFragment.TAG + ":" + position + ":" + (mIsRightToLeftDirection ? "RTL" : "LTR");
    }

    @Override
    public Fragment getItem(int position) {
        if (mImageUrls == null) {
            throw new IllegalStateException("Null Image Urls");
        }

        return PageFragment.newInstance(mImageUrls.get(position), position);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public List<String> getImageUrls() {
        return mImageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        mImageUrls = imageUrls;

        notifyDataSetChanged();
    }

    public boolean getIsRightToLeftDirection() {
        return mIsRightToLeftDirection;
    }

    public void setIsRightToLeftDirection(boolean isRightToLeftDirection) {
        mIsRightToLeftDirection = isRightToLeftDirection;
    }
}
