package io.github.phora.androptpb;

/**
 * Created by phora on 8/21/15.
 */
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

/*
 * This class is useful for using inside of ListView that needs to have checkable items.
 * source:
 * https://github.com/tokudu/begemot/blob/c920417cf5f8d7d2392b3b7c4b29c57d9ecbe8fe/src/com/tokudu/begemot/widgets/CheckableLinearLayout.java
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private Checkable _checkbox;

    private static final String LOG_TAG = "CheckableLinearLayout";

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // find checked text view
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View v = getChildAt(i);
            if (v instanceof Checkable) {
                _checkbox = (Checkable)v;
            }
        }
        //Log.d(LOG_TAG, "finished inflating "+(_checkbox != null));
    }

    @Override
    public boolean isChecked() {
        return _checkbox != null && _checkbox.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        Log.d(LOG_TAG, "i was told to "+checked);
        if (_checkbox != null) {
            _checkbox.setChecked(checked);
        }
    }

    @Override
    public void toggle() {
        if (_checkbox != null) {
            _checkbox.toggle();
        }
    }
}
