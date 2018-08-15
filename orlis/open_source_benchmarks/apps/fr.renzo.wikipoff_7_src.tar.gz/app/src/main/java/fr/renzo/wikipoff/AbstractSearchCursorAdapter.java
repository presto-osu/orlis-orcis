/*

Copyright 2014 "Renzokuken" (pseudonym, first committer of WikipOff project) at
https://github.com/conchyliculture/wikipoff

This file is part of WikipOff.

    WikipOff is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    WikipOff is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with WikipOff.  If not, see <http://www.gnu.org/licenses/>.

*/
package fr.renzo.wikipoff;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import fr.renzo.wikipoff.Database.DatabaseException;

abstract class AbstractSearchCursorAdapter extends CursorAdapter {
    private static final String TAG = "SearchCursorAdapter" ;
    private Database dbh;
    private Context context;

    @SuppressWarnings("deprecation")
    public AbstractSearchCursorAdapter(Context context, Cursor c, Database dbh) {
        super(context, c);
        this.context = context;
        this.dbh = dbh;
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (constraint == null)
            return null;
        try {
            String searchQuery = "*" + constraint + "*";
            Database.MyMergeCursor c = (Database.MyMergeCursor) dbh.myRawQuery("SELECT _id,title FROM searchTitles WHERE title MATCH ? ORDER BY length(title), title limit 500 ", searchQuery);

            MatrixCursor extras = new MatrixCursor(new String[]{"_id", "title", "wikiname"});
            extras.addRow(new String[]{"-1", (String) constraint, ""});

            if (c.moveToFirst()) {
                while (c.moveToNext()) {
                    extras.addRow(new String[]{String.valueOf(c.getInt(0)), c.getString(1), c.getWikiCursor().getName()});
                }
            }

            return (extras);
        } catch (DatabaseException e) {
            e.alertUser(context);
        }
        return null;
    }
}

