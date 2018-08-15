package com.luk.timetable2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by luk on 9/28/15.
 */
public class DatabaseHandler {
    private static DatabaseHandler sInstance;
    private static String sDbFile = "db";

    public SQLiteDatabase getDB(Context context) {
        File dbFile = context.getDatabasePath(sDbFile);

        return context.openOrCreateDatabase(dbFile.getAbsolutePath(), Context.MODE_PRIVATE, null);
    }

    public void createTables(Context context) {
        SQLiteDatabase sqLiteDatabase = getDB(context);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS lessons;");
        sqLiteDatabase.execSQL("CREATE TABLE lessons ( _id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " day INTEGER, lesson TEXT, room TEXT, time TEXT, hidden TEXT );");
    }

    public static DatabaseHandler getInstance() {
        if (sInstance == null) {
            sInstance = new DatabaseHandler();
        }

        return sInstance;
    }
}