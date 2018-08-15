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
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.github.notizklotz.derbunddownloader.R;

class IssuesGridViewBinder implements SimpleCursorAdapter.ViewBinder {
    private final Context context;

    public IssuesGridViewBinder(Context context) {
        this.context = context;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (view.getId() == R.id.stateTextView) {
            String statusText = context.getString(R.string.download_state_unknown);
            int status = cursor.getInt(columnIndex);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    statusText = context.getString(R.string.download_state_successful);
                    break;
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                    statusText = context.getString(R.string.download_state_pending);
                    break;
                case DownloadManager.STATUS_RUNNING:
                    statusText = context.getString(R.string.download_state_running);
                    break;
                case DownloadManager.STATUS_FAILED:
                    statusText = context.getString(R.string.download_state_failed);
                    break;
            }
            ((TextView) view).setText(statusText);
            return true;
        }

        return false;
    }
}
