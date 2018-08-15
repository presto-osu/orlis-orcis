package org.itishka.pointim.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by Tishka17 on 11.07.2015.
 */
public class FixedViewPager extends ViewPager {
    public FixedViewPager(Context context) {
        super(context);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        //super.offsetTopAndBottom(offset);
        super.setTranslationY(offset);
    }

    public FixedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
