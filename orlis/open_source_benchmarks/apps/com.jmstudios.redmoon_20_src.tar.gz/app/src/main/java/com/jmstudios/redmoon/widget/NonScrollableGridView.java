/* Copyright (c) 2015 Chris Nguyen
**
** Permission to use, copy, modify, and/or distribute this software for
** any purpose with or without fee is hereby granted, provided that the
** above copyright notice and this permission notice appear in all copies.
**
** THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
** WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
** WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
** BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
** OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
** WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
** ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
** SOFTWARE.
*/
package com.jmstudios.redmoon.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Taken from http://stackoverflow.com/a/12931731
 *
 * <p>This class is a normal {@link GridView}, but is intended to never scroll. This allows
 * NonScrollableGridView to exist as a child of a scrollable container, such as
 * {@link android.widget.ListView}, without causing layout or scrolling issues. Ideally, users should
 * prefer {@link android.widget.GridLayout} instead; NonScrollableGridView is only useful to maintain
 * the separation between the view and underlying data, which BaseAdapter offers (for GridView). The
 * adapter interface and underlying data store must be implemented manually if GridLayout is used.
 *
 * <p>One of the aforementioned layout issues with placing a GridView inside a ListView is that
 * setting wrap_content to the layout_height attribute of the GridView will not take effect, and a
 * explicit height dimen must be used. NonScrollableGridView's wrap_content will work in a ListView.
 */
/*
 * This class is used by {@link ColorPicker}, which is intended to be displayed in a
 * {@link android.preference.PreferenceFragment}. PreferenceFragments internally use a ListView to
 * display each Preference in a vertical list. However, this causes layout issues such as
 * wrap_content not taking effect. Because we want to maintain the clean separation between our
 * ColorPicker (GridView) and underlying data by using BaseAdapter, we extend NonScrollableGridView
 * to address this layout issue (so wrap_content will work within ListView), and continue to use
 * BaseAdapter.
 *
 * Alternatively, we can have ColorPicker extend GridLayout instead, but the BaseAdapter
 * responsibilities (e.g., notifyDataSetChanged(), getView()) need to be implemented manually.
 */
public class NonScrollableGridView extends GridView {
    public NonScrollableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Do not use the highest two bits of Integer.MAX_VALUE because they are
        // reserved for the MeasureSpec mode
        int heightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightSpec);
        getLayoutParams().height = getMeasuredHeight();
    }
}
