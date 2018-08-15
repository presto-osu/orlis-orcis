package io.github.phora.androptpb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorTreeAdapter;

import io.github.phora.androptpb.DBHelper;

/**
 * Created by phora on 9/15/15.
 */
public class PasteHintsCursorAdapter extends SimpleCursorTreeAdapter {

    private Context mContext;
    private String mFilter = null;

    public PasteHintsCursorAdapter(Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        mContext = context;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        DBHelper sqlhelper = DBHelper.getInstance(mContext);
        long serverId = cursor.getLong(cursor.getColumnIndex(DBHelper.PASTE_HINTS_SID));
        long groupId = cursor.getLong(cursor.getColumnIndex(DBHelper.PASTE_HINTS_GID));

        Cursor children;
        if (mFilter == null) {
            children = sqlhelper.getHintGroupChildren(serverId, groupId);
        }
        else {
            children = sqlhelper.getHintGroupChildren(serverId, groupId, mFilter);
        }
        return children;
    }

    public Context getContext() {
        return mContext;
    }

    public String getFilterString() {
        return mFilter;
    }

    public void setFilterString(String filter) {
        this.mFilter = filter;
    }
}
