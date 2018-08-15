package net.sf.widget;

import android.content.Context;
import android.util.AttributeSet;

public class TabHost extends android.widget.TabHost {

    public TabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabHost(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Workaround for Issue 2516: TabHost/TabWidget steals focus from EditText
        this.getViewTreeObserver().removeOnTouchModeChangeListener(this);
    }
}
