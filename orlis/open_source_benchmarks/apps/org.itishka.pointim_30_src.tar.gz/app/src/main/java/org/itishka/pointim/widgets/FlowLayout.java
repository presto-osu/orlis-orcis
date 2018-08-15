package org.itishka.pointim.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * A view container with layout behavior like that of the Swing FlowLayout.
 * Originally from
 * http://nishantvnair.wordpress.com/2010/09/28/flowlayout-in-android/
 *
 * @author Melinda Green
 */
public class FlowLayout extends ViewGroup {
    private final static int PAD_H = 8, PAD_V = 8; // Space between child views.
    private int mHeight;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        final int count = getChildCount();
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST)
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        else
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        mHeight = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    ypos = mHeight;
                }
                xpos += childw + PAD_H;
                mHeight = Math.max(mHeight, ypos + childh + PAD_V);
            }
        }
        if (mHeight > getPaddingTop()) mHeight -= PAD_V;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = mHeight + getPaddingBottom();
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + mHeight < height) {
                height = mHeight + getPaddingBottom();
            }
        }
        setMeasuredDimension(width, height);
    } // end onMeasure()

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int height = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                if (xpos + childw > width) {
                    xpos = getPaddingLeft();
                    ypos = height;
                }
                height = Math.max(height, ypos + childh + PAD_V);
                child.layout(xpos, ypos, xpos + childw, ypos + childh);
                xpos += childw + PAD_H;
            }
        }
    } // end onLayout()

}