package net.sf.times;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.widget.RemoteViews;

import net.sf.times.ZmanimAdapter.ZmanimItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Clock widget with hour and title underneath.<br>
 * Based on the default Android digital clock widget.
 *
 * @author Moshe
 */
public class ClockWidget extends ZmanimWidget {

    private DateFormat timeFormat;

    /**
     * Constructs a new widget.
     */
    public ClockWidget() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.clock_widget;
    }

    @Override
    protected int getIntentViewId() {
        return R.id.date_gregorian;
    }

    @Override
    protected void bindViews(RemoteViews list, ZmanimAdapter adapter) {
        final int count = adapter.getCount();
        ZmanimItem item;

        for (int position = 0; position < count; position++) {
            item = adapter.getItem(position);
            if (item.elapsed || (item.time == ZmanimAdapter.NEVER) || (item.timeLabel == null))
                continue;
            bindView(list, position, item);
            break;
        }
    }

    @Override
    protected void bindView(RemoteViews list, int position, ZmanimItem item) {
        if (timeFormat == null) {
            Context context = this.context;
            boolean time24 = android.text.format.DateFormat.is24HourFormat(context);
            String pattern = context.getString(time24 ? R.string.clock_24_hours_format : R.string.clock_12_hours_format);
            timeFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        }

        CharSequence label = timeFormat.format(item.time);
        SpannableStringBuilder spans = new SpannableStringBuilder(label);
        int indexMinutes = TextUtils.indexOf(label, ':');
        spans.setSpan(new TypefaceSpan("sans-serif"), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spans.setSpan(new StyleSpan(Typeface.BOLD), 0, indexMinutes, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        list.setTextViewText(R.id.time, spans);
        list.setTextViewText(android.R.id.title, context.getText(item.titleId));
    }

    @Override
    protected boolean isRemoteList() {
        return false;
    }

    @Override
    protected void notifyAppWidgetViewDataChanged(Context context) {
        timeFormat = null;
        super.notifyAppWidgetViewDataChanged(context);
    }
}
