package im.r_c.android.clearweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import im.r_c.android.clearweather.model.Consts;
import im.r_c.android.clearweather.model.County;
import im.r_c.android.clearweather.model.WeatherInfo;
import im.r_c.android.clearweather.util.WeatherInfoFetcher;

/**
 * ClearWeather
 * Created by richard on 16/5/4.
 */
public class WeatherInfoDAO {
    private DatabaseHelper mHelper;
    private SQLiteDatabase mDatabase;

    public WeatherInfoDAO(Context context) {
        mHelper = new DatabaseHelper(context, Consts.DATABASE_FILE_NAME, null, Consts.DATABASE_VERSION);
        mDatabase = mHelper.getWritableDatabase();
    }

    public long insert(WeatherInfo info) {
        ContentValues values = new ContentValues();
        values.put(WeatherInfo.KEY_COUNTY_CODE, info.getCounty().getCode());
        values.put(WeatherInfo.KEY_JSON, info.getRawJSONString());
        values.put(WeatherInfo.KEY_UPDATED, info.getUpdateTimestamp());
        return mDatabase.insert(Consts.DATABASE_TABLE_WEATHER_INFO, null, values);
    }

    public int update(WeatherInfo info) {
        ContentValues values = new ContentValues();
        values.put(WeatherInfo.KEY_JSON, info.getRawJSONString());
        values.put(WeatherInfo.KEY_UPDATED, info.getUpdateTimestamp());
        return mDatabase.update(
                Consts.DATABASE_TABLE_WEATHER_INFO,
                values,
                WeatherInfo.KEY_COUNTY_CODE + " = ?",
                new String[]{info.getCounty().getCode()});
    }

    public boolean save(WeatherInfo info) {
        return update(info) > 0 || insert(info) != -1;
    }

    public int delete(County county) {
        return mDatabase.delete(
                Consts.DATABASE_TABLE_WEATHER_INFO,
                WeatherInfo.KEY_COUNTY_CODE + " = ?",
                new String[]{county.getCode()});
    }

    public WeatherInfo query(County county) {
        Cursor cursor = mDatabase.query(
                Consts.DATABASE_TABLE_WEATHER_INFO,
                new String[]{WeatherInfo.KEY_UPDATED, WeatherInfo.KEY_JSON},
                WeatherInfo.KEY_COUNTY_CODE + " = ?",
                new String[]{county.getCode()},
                null, null, null);
        WeatherInfo info = null;
        if (cursor.moveToFirst()) {
            info = new WeatherInfo();
            info.setCounty(county);
            info.setRawJSONString(cursor.getString(cursor.getColumnIndex(WeatherInfo.KEY_JSON)));
            info.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(WeatherInfo.KEY_UPDATED)));
        }
        cursor.close();
        return WeatherInfoFetcher.parse(info);
    }

    public void close() {
        mDatabase.close();
        mHelper.close();
    }
}
