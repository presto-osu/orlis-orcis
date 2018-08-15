package org.itishka.pointim.widgets;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

/**
 * Created by Tishka17 on 11.07.2015.
 */
public class PatchedScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior {
    public PatchedScrollingViewBehavior() {
        super();
    }

    public PatchedScrollingViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private AppBarLayout mAppBarLayout;

    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        if (child.getLayoutParams().height == -1) {
            List<View> dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty()) {
                return false;
            }
            if (mAppBarLayout == null)
                mAppBarLayout = findFirstAppBarLayout(dependencies);
            if (mAppBarLayout != null && ViewCompat.isLaidOut(mAppBarLayout)) {
                if (ViewCompat.getFitsSystemWindows(mAppBarLayout)) {
                    ViewCompat.setFitsSystemWindows(child, true);
                }

                int scrollRange = mAppBarLayout.getTotalScrollRange();
//                int height = parent.getHeight() - appBar.getMeasuredHeight() + scrollRange;
                int parentHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                int height = parentHeight - mAppBarLayout.getMeasuredHeight() + scrollRange;
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                return true;
            }
        }

        return false;
    }


    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
        int i = 0;

        for (int z = views.size(); i < z; ++i) {
            View view = views.get(i);
            if (view instanceof AppBarLayout) {
                return (AppBarLayout) view;
            }
        }

        return null;
    }
}