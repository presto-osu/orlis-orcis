package com.gk.datacontrol;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.content.CursorLoader;

public class ExerciseDataCursorLoader extends CursorLoader {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    DBClass db;

    public ExerciseDataCursorLoader(Context context, DBClass db) {
        super( context );

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ExerciseDataCursorLoader :: ExerciseDataCursorLoader");
        this.db = db;
    }

    @Override
    public Cursor loadInBackground() {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ExerciseDataCursorLoader :: loadInBackground :: id "+this.getId() );

        Cursor cursor = db.fetchExerciseHistory();
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "ExerciseDataCursorLoader :: updating cursor ");

        return cursor;
    }
}