package com.jparkie.aizoban.views.adapters;

import android.content.Context;
import android.database.Cursor;
import android.widget.BaseAdapter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public abstract class BaseCursorAdapter extends BaseAdapter {
    protected Context mContext;

    protected Class<?> mClassType;
    protected Cursor mCursor;

    public BaseCursorAdapter(Context context, Class<?> classType) {
        mContext = context;

        mClassType = classType;
        mCursor = null;
    }

    @Override
    public int getCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mCursor != null) {
            mCursor.moveToPosition(position);

            if (mClassType != null) {
                return cupboard().withCursor(mCursor).get(mClassType);
            }
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return;
        }

        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;
        if (mCursor != null) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }
}
