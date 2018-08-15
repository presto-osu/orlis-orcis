package com.claha.showtimeremote.base;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.claha.showtimeremote.R;

public class BaseViewPagerIndicator extends RadioGroup implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener {

    private ViewPager viewPager;

    public BaseViewPagerIndicator(Context context) {
        super(context, null);
    }

    public BaseViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        setOnCheckedChangeListener(this);
    }

    private void createIndicators() {
        removeAllViews();
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            View indicator = inflate(getContext(), R.layout.indicator, null);
            addView(indicator);
        }
        setChecked(viewPager.getCurrentItem());
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        this.viewPager.setOnPageChangeListener(this);
        createIndicators();

        this.viewPager.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                createIndicators();
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (viewPager != null) {
            int index = group.indexOfChild(group.findViewById(checkedId));
            viewPager.setCurrentItem(index);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setChecked(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void setChecked(int position) {
        check(getChildAt(position).getId());
    }
}
