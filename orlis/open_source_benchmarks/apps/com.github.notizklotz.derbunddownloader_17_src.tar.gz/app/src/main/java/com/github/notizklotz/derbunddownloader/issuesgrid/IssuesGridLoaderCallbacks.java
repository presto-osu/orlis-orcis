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
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

class IssuesGridLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

    private final Context context;
    private final SimpleCursorAdapter issueListAdapter;

    public IssuesGridLoaderCallbacks(Context context, SimpleCursorAdapter issueListAdapter) {
        this.context = context;
        this.issueListAdapter = issueListAdapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new DownloadManagerLoader(context, new DownloadManager.Query());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        issueListAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        issueListAdapter.changeCursor(null);
    }
}
