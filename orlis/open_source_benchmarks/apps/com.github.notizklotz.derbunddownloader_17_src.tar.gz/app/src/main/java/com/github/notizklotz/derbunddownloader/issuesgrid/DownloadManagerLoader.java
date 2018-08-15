/*
 * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
 * Copyright (C) 2013 Adrian Gygax
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see {http://www.gnu.org/licenses/}.
 */

package com.github.notizklotz.derbunddownloader.issuesgrid;

import android.app.DownloadManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;

import java.io.FileDescriptor;
import java.io.PrintWriter;

class DownloadManagerLoader extends AsyncTaskLoader<Cursor> {
    private final Loader.ForceLoadContentObserver mObserver;
    private final DownloadManager.Query mQuery;
    private final DownloadManager downloadManager;

    private Cursor mCursor;

    public DownloadManagerLoader(Context context, DownloadManager.Query query) {
        super(context);
        mObserver = new Loader.ForceLoadContentObserver();
        this.mQuery = query;
        downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        Cursor cursor = downloadManager.query(mQuery);
        if (cursor != null) {
            try {
                // Ensure the cursor window is filled.
                cursor.getCount();
                cursor.registerContentObserver(mObserver);
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            }
        }
        return cursor;
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async mQuery came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, @SuppressWarnings("NullableProblems") PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mQuery=");
        writer.println(mQuery);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
    }
}
