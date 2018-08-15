package io.github.phora.androptpb.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorTreeAdapter;

import io.github.phora.androptpb.DBHelper;

/**
 * Created by phora on 1/6/16.
 */
public class FormatsCursorAdapter extends SimpleCursorTreeAdapter {
    private Context mContext;

    public FormatsCursorAdapter (Context context, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo, int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        mContext = context;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor cursor) {
        DBHelper sqlhelper = DBHelper.getInstance(mContext);
        long serverId = cursor.getLong(cursor.getColumnIndex(DBHelper.PASTE_HINTS_SID));
        long groupId = cursor.getLong(cursor.getColumnIndex(DBHelper.PASTE_HINTS_GID));

        Cursor children = sqlhelper.getFormatterChildren(serverId, groupId);
        return children;
    }
}
