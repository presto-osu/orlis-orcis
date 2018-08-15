package com.jparkie.aizoban.utils.wrappers;

import android.database.AbstractCursor;
import android.database.Cursor;

import com.jparkie.aizoban.controllers.databases.ApplicationContract;

import java.util.HashMap;
import java.util.List;

public class DownloadChapterSortCursorWrapper extends AbstractCursor {
    private final Cursor mCursor;

    private final int[] mPosition;

    public DownloadChapterSortCursorWrapper(Cursor downloadChapterCursor, List<String> sortedChapterUrls) {
        mCursor = downloadChapterCursor;

        final int count = downloadChapterCursor.getCount();
        mPosition = new int[count];

        final HashMap<String, Integer> unorderedDownloadChaptersUrlToIndexMap = new HashMap<String, Integer>(count);
        for (int index = 0; index < downloadChapterCursor.getCount(); index++) {
            if (downloadChapterCursor.moveToPosition(index)) {
                final String chapterUrl = downloadChapterCursor.getString(downloadChapterCursor.getColumnIndex(ApplicationContract.DownloadChapter.COLUMN_URL));

                unorderedDownloadChaptersUrlToIndexMap.put(chapterUrl, index);
            }
        }

        int globalIndex = 0;
        for (int localIndex = 0; localIndex < sortedChapterUrls.size() && globalIndex < unorderedDownloadChaptersUrlToIndexMap.size(); localIndex++) {
            String currentUrl = sortedChapterUrls.get(localIndex);

            if (unorderedDownloadChaptersUrlToIndexMap.containsKey(currentUrl)) {
                mPosition[globalIndex++] = unorderedDownloadChaptersUrlToIndexMap.get(currentUrl);
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
        return mCursor.getCount();
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
