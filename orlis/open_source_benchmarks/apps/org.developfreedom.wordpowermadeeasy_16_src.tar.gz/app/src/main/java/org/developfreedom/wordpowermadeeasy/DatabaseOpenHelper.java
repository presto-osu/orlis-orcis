/**
 *   Word Power Made Easy - A vocabulary building application
 *
 *   This file is part of LogMeIn.
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *   Copyright (c) 2014 Tanjot Kaur <tanjot28@gmail.com>
 *   Copyright (c) 2014 Vivek Aggarwal <vivekaggarwal92@gmail.com>
 *
 *   This file is now also a part of WordPowerMadeEasy.
 *   Copyright (c) 2014 Shubham Chaudhary <me@shubhamchaudhary.in>
 *
 *   WordPowerMadeEasy is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   WordPowerMadeEasy is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with WordPowerMadeEasy.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.developfreedom.wordpowermadeeasy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//import android.provider.BaseColumns;


public class DatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "wordManager";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_WORDLIST = "wordList";
    public static final String KEY_INDEX = "index";
    public static final String KEY_WORD = "word";
    public static final String KEY_MEANING = "meaning";
    public static final String KEY_SCORE = "score";
    private static final String TAG = DatabaseOpenHelper.class.getSimpleName();

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creating tables
        //TODO: This query gives error near KEY_INDEX
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_WORDLIST + " ("
                + KEY_INDEX + " INTEGER PRIMARY KEY," + KEY_WORD + " TEXT,"
                + KEY_MEANING + " TEXT," + KEY_SCORE + " INTEGER" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //called when database is upgraded like modifying the table structure,
        //adding constraints to database etc
        if (oldVersion == 1) {
            //alter current table or schema and execute the sql command using db.execSQL(sql)
            this.onUpgrade(db, ++oldVersion, newVersion);
        }
        /* Alternative way to handle this
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
         */
    }
}

// vim: set ts=4 sw=4 tw=0 et :
