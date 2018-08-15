/*
 * Copyright (C) 2015 Matthias Gabriel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gabm.fancyplaces.functional;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gabm.fancyplaces.FancyPlacesApplication;
import com.gabm.fancyplaces.data.FancyPlace;
import com.gabm.fancyplaces.data.ImageFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabm on 19/05/15.
 */
public class FancyPlacesDatabase extends SQLiteOpenHelper {

    public static final String TABLE_PLACES = "places";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_LOCATION_LAT = "location_lat";
    public static final String COLUMN_LOCATION_LONG = "location_long";
    public static final String COLUMN_IMAGE_LOCATION = "image_location";
    public static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "fancyplaces.db";
    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PLACES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_NOTES + " text not null, "
            + COLUMN_LOCATION_LAT + " text not null, "
            + COLUMN_LOCATION_LONG + " text not null, "
            + COLUMN_IMAGE_LOCATION + " text not null"
            + ");";
    private static FancyPlacesApplication currentAppContext = null;
    public String[] allColumns = {
            COLUMN_ID,
            COLUMN_TITLE,
            COLUMN_NOTES,
            COLUMN_LOCATION_LAT,
            COLUMN_LOCATION_LONG,
            COLUMN_IMAGE_LOCATION};
    private SQLiteDatabase database = null;

    public FancyPlacesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        currentAppContext = (FancyPlacesApplication) context;
    }

    public void open() throws SQLException {
        database = getWritableDatabase();
    }

    private ContentValues makeContentValues(FancyPlace fancyPlace) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, fancyPlace.getTitle());
        values.put(COLUMN_NOTES, fancyPlace.getNotes());
        values.put(COLUMN_LOCATION_LAT, fancyPlace.getLocationLat());
        values.put(COLUMN_LOCATION_LONG, fancyPlace.getLocationLong());
        values.put(COLUMN_IMAGE_LOCATION, fancyPlace.getImage().getFileName());
        return values;
    }

    public FancyPlace createFancyPlace(FancyPlace fancyPlace) {

        long insertId = database.insert(TABLE_PLACES, null,
                makeContentValues(fancyPlace));
        Cursor cursor = database.query(TABLE_PLACES,
                allColumns, COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        FancyPlace newPlace = cursorToFancyPlace(cursor);
        cursor.close();
        return newPlace;
    }

    public void updateFancyPlace(FancyPlace fancyPlaceFrom) {
        database.update(TABLE_PLACES,
                makeContentValues(fancyPlaceFrom),
                COLUMN_ID + " = " + String.valueOf(fancyPlaceFrom.getId()),
                null);
    }

    public void deleteFancyPlace(FancyPlace fancyPlace, Boolean deleteImageFile) {
        long id = fancyPlace.getId();
        database.delete(TABLE_PLACES, COLUMN_ID
                + " = " + id, null);

        if (deleteImageFile)
            fancyPlace.getImage().delete();
    }

    public List<FancyPlace> getAllFancyPlaces() {
        List<FancyPlace> fancyPlacesFromSQL = new ArrayList<FancyPlace>();

        Cursor cursor = database.query(TABLE_PLACES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            FancyPlace fpFromSQL = cursorToFancyPlace(cursor);
            fancyPlacesFromSQL.add(fpFromSQL);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return fancyPlacesFromSQL;
    }

    private FancyPlace cursorToFancyPlace(Cursor cursor) {
        FancyPlace fancyPlace = new FancyPlace();
        fancyPlace.setId(cursor.getLong(0));
        fancyPlace.setTitle(cursor.getString(1));
        fancyPlace.setNotes(cursor.getString(2));
        fancyPlace.setLocationLat(cursor.getString(3));
        fancyPlace.setLocationLong(cursor.getString(4));
        fancyPlace.setImage(new ImageFile(cursor.getString(5)));
        fancyPlace.setIsInDatabase(true);
        return fancyPlace;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FancyPlacesDatabase.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        if (oldVersion == 4 && newVersion == 5) {
            Log.w(FancyPlacesDatabase.class.getName(), "Upgrading database version from 4 to 5, resizing images!");

            Cursor cursor = db.query(TABLE_PLACES, allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                FancyPlace fpFromSQL = cursorToFancyPlace(cursor);

                fpFromSQL.getImage().scaleDown(com.gabm.fancyplaces.FancyPlacesApplication.TARGET_PIX_SIZE);

                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
            onCreate(db);
        }

    }


}
