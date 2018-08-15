package com.jparkie.aizoban.utils.wrappers;

import android.database.AbstractCursor;
import android.database.Cursor;

import com.jparkie.aizoban.controllers.databases.ApplicationContract;

import java.util.List;

public class RecentOfflineChapterFilteringCursorWrapper extends AbstractCursor {
    private final Cursor mCursor;

    private final int[] mPosition;
    private int mCount;

    public RecentOfflineChapterFilteringCursorWrapper(Cursor downloadChapterCursor, List<String> recentChapterUrls) {
        mCursor = downloadChapterCursor;

        final int count = downloadChapterCursor.getCount();
        mPosition = new int[count];

        downloadChapterCursor.moveToPosition(-1);
        while (downloadChapterCursor.moveToNext()) {
            final String chapterUrl = downloadChapterCursor.getString(downloadChapterCursor.getColumnIndex(ApplicationContract.Chapter.COLUMN_URL));

            if (!recentChapterUrls.remove(chapterUrl)) {
                mPosition[mCount++] = downloadChapterCursor.getPosition();
            }
        }
    }

    @Override
    public void close() {
        super.close();
        mCursor.close();
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return mCursor.moveToPosition(mPosition[newPosition]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public String[] getColumnNames() {
        return mCursor.getColumnNames();
    }

    @Override
    public String getString(int column) {
        return mCursor.getString(column);
    }

    @Override
    public short getShort(int column) {
        return mCursor.getShort(column);
    }

    @Override
    public int getInt(int column) {
        return mCursor.getInt(column);
    }

    @Override
    public long getLong(int column) {
        return mCursor.getLong(column);
    }

    @Override
    public float getFloat(int column) {
        return mCursor.getFloat(column);
    }

    @Override
    public double getDouble(int column) {
        return mCursor.getDouble(column);
    }

    @Override
    public boolean isNull(int column) {
        return mCursor.isNull(column);
    }
}
