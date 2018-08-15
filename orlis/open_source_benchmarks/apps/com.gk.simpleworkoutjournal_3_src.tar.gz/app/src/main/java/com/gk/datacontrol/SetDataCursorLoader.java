package com.gk.datacontrol;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.util.Log;

public class SetDataCursorLoader extends CursorLoader {
    private static final String APP_NAME = "SWJournal";
    private static final boolean DEBUG_FLAG = false;
    DBClass db;
    String targetEx;

    public SetDataCursorLoader(Context context, DBClass db, Cursor currCursor) {
        super( context );

        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SetDataCursorLoader :: SetDataCursorLoader");
        renewTargetEx( currCursor );
        this.db = db;
    }

    public void renewTargetEx(Cursor exCursor) {
        if  ( exCursor != null && exCursor.getCount() != 0) {
            this.targetEx = exCursor.getString(exCursor.getColumnIndex(DBClass.KEY_EX_NAME));
        }
    }

    public void renewTargetEx(String exName) {
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SetDataCursorLoader :: loadInBackground :: new target exercise : \""+exName+"\"" );
        this.targetEx = exName;
    }

    @Override
    public Cursor loadInBackground() {
        //get sets/exercises
        if ( DEBUG_FLAG ) Log.v(APP_NAME, "SetDataCursorLoader :: loadInBackground :: id "+this.getId()+" exercise: \""+targetEx+"\"" );
        return db.fetchSetsForExercise( targetEx );
    }
}
