/*
 * Source file of the Halachic Times project.
 * Copyright (c) 2012. All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/2.0
 *
 * Contributors can be contacted by electronic mail via the project Web pages:
 * 
 * http://sourceforge.net/projects/halachictimes
 * 
 * http://halachictimes.sourceforge.net
 *
 * Contributor(s):
 *   Moshe Waisberg
 * 
 */
package net.sf.times.location;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

import net.sf.times.R;

/**
 * A helper class to manage database creation and version management for
 * addresses and elevations.
 *
 * @author Moshe Waisberg
 */
public class AddressOpenHelper extends SQLiteOpenHelper {

    /** Database name for times. */
    private static final String DB_NAME = "times";
    /** Database version. */
    private static final int DB_VERSION = 4;
    /** Database table for addresses. */
    public static final String TABLE_ADDRESSES = "addresses";
    /** Database table for elevations. */
    public static final String TABLE_ELEVATIONS = "elevations";
    /** Database table for cities. */
    public static final String TABLE_CITIES = "cities";

    private final Context context;

    /**
     * Constructs a new helper.
     *
     * @param context
     *         the context.
     */
    public AddressOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_ADDRESSES).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append(AddressColumns.LOCATION_LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LOCATION_LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(AddressColumns.ADDRESS).append(" TEXT NOT NULL,");
        sql.append(AddressColumns.LANGUAGE).append(" TEXT,");
        sql.append(AddressColumns.TIMESTAMP).append(" INTEGER NOT NULL,");
        sql.append(AddressColumns.FAVORITE).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());

        sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_ELEVATIONS).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append(ElevationColumns.LATITUDE).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.LONGITUDE).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.ELEVATION).append(" DOUBLE NOT NULL,");
        sql.append(ElevationColumns.TIMESTAMP).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());

        sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(TABLE_CITIES).append('(');
        sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY,");
        sql.append(CitiesColumns.TIMESTAMP).append(" INTEGER NOT NULL,");
        sql.append(CitiesColumns.FAVORITE).append(" INTEGER NOT NULL");
        sql.append(");");
        db.execSQL(sql.toString());

        fillCities(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_ADDRESSES + ";");
        if (oldVersion >= 3) {
            db.execSQL("DROP TABLE " + TABLE_ELEVATIONS + ";");
            if (oldVersion >= 4) {
                db.execSQL("DROP TABLE " + TABLE_CITIES + ";");
            }
        }
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // Delete stale records older than 1 year.
        String whereClause = "(" + AddressColumns.TIMESTAMP + " < " + (System.currentTimeMillis() - DateUtils.YEAR_IN_MILLIS) + ")";
        db.delete(TABLE_ADDRESSES, whereClause, null);
    }

    /**
     * Fill the cities table with empty rows.
     *
     * @param db
     *         the database.
     */
    private void fillCities(SQLiteDatabase db) {
        Resources res = context.getResources();
        String[] citiesCountries = res.getStringArray(R.array.cities_countries);
        int citiesCount = citiesCountries.length;

        ContentValues values = new ContentValues();
        values.put(CitiesColumns.TIMESTAMP, System.currentTimeMillis());
        values.put(CitiesColumns.FAVORITE, 0);

        for (int i = 0, j = 1; i < citiesCount; i++, j++) {
            values.put(BaseColumns._ID, j);
            db.insert(TABLE_CITIES, null, values);
        }
    }
}
