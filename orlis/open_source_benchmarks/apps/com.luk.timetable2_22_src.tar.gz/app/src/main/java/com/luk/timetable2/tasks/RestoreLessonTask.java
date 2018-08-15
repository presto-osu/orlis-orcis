package com.luk.timetable2.tasks;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;

import com.luk.timetable2.DatabaseHandler;

/**
 * Created by LuK on 2015-05-01.
 */
public class RestoreLessonTask extends AsyncTask<Integer, Integer, Integer> {
    private Activity mActivity;
    private String mId;

    public RestoreLessonTask(Activity activity, String id) {
        mActivity = activity;
        mId = id;
    }

    @Override
    protected Integer doInBackground(Integer... strings) {
        SQLiteDatabase sqLiteDatabase = DatabaseHandler.getInstance().getDB(mActivity);

        SQLiteStatement stmt =
                sqLiteDatabase.compileStatement("UPDATE `lessons` SET hidden = '0' WHERE _id = ?");
        stmt.bindString(1, mId);
        stmt.execute();

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
    }
}