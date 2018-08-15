package com.claha.showtimeremote.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class CircularPagerAdapter<T> extends PagerAdapter implements ViewPager.OnPageChangeListener {

    private final ViewPager viewPager;
    private final List<T> data;
    private int currentPage = 0;

    public CircularPagerAdapter(ViewPager viewPager, List<T> data) {
        this.data = new ArrayList<>();
        this.data.addAll(data);

        if (data.size() > 1) {
            this.data.add(data.get(0));
            this.data.add(0, data.get(data.size() - 1));
        }

        this.viewPager = viewPager;
        this.viewPager.setOnPageChangeListener(this);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    T getItem(int position) {
        return data.get(position);
    }

    protected int getOriginalPosition(int position) {
        if (position == 0) {
            return getCount() - 3;
        } else if (position == getCount() - 1) {
            return 0;
        } else {
            return position - 1;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = instantiateView(container.getContext(), getItem(position));
        container.addView(view);
        return view;
    }

    protected abstract View instantiateView(Context context, T item);

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPage = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        int count = getCount();
        if (state == ViewPager.SCROLL_STATE_IDLE && count > 1) {
            if (currentPage == 0) {
                viewPager.setCurrentItem(count - 2, false);
            } else if (currentPage == count - 1) {
                viewPager.setCurrentItem(1, false);
            }
        }
    }
}
