package im.r_c.android.clearweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import im.r_c.android.clearweather.model.Consts;

/**
 * ClearWeather
 * Created by richard on 16/5/2.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_COUNTY = "CREATE TABLE IF NOT EXISTS " + Consts.DATABASE_TABLE_COUNTY + " (\n" +
            "  id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "  name     TEXT,\n" +
            "  name_en  TEXT,\n" +
            "  city     TEXT,\n" +
            "  province TEXT,\n" +
            "  code     TEXT\n" +
            ");\n";
    private static final String SQL_CREATE_WEATHER_INFO = "CREATE TABLE IF NOT EXISTS " + Consts.DATABASE_TABLE_WEATHER_INFO + " (\n" +
            "  id          INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "  county_code TEXT UNIQUE,\n" +
            "  updated     INTEGER,\n" +
            "  json        TEXT\n" +
            ");";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_COUNTY);
        db.execSQL(SQL_CREATE_WEATHER_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
