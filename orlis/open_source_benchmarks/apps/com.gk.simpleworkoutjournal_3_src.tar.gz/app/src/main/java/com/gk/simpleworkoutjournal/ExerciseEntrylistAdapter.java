package com.gk.simpleworkoutjournal;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import com.gk.datacontrol.DBClass;

public class ExerciseEntrylistAdapter extends SimpleCursorAdapter {
    DBClass mDbmediator;

    ExerciseEntrylistAdapter( Context ctx, DBClass dbmediator ) {
        super(ctx , R.layout.autocomplete_entry, dbmediator.fetchExerciseNames( "" ), new String[] { DBClass.KEY_ID }, new int[] { android.R.id.text1 }, 0 );
        mDbmediator = dbmediator;

        CursorToStringConverter converter = new CursorToStringConverter() {

            @Override
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString( cursor.getColumnIndex( DBClass.KEY_ID )  );
            }
        };

        this.setCursorToStringConverter( converter );
     }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String args = "";

        if (constraint != null)
        {
            args = constraint.toString();
        }
        return mDbmediator.fetchExerciseNames( args );
    }
}
