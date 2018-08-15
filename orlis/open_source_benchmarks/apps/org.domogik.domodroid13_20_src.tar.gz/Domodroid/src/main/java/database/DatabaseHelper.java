package database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.orhanobut.logger.Logger;


class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;

    private static final String CREATE_TABLE_AREA = "CREATE TABLE table_area (description TEXT, id INTEGER, name TEXT);";
    private static final String CREATE_TABLE_ROOM = "CREATE TABLE table_room (area_id INTEGER, description TEXT, id INTEGER, name TEXT);";
    private static final String CREATE_TABLE_ICON = "CREATE TABLE table_icon (name TEXT, value TEXT, reference INTEGER);";

    private static final String CREATE_TABLE_FEATURE = "CREATE TABLE table_feature (device_feature_model_id TEXT, id INTEGER, device_id INTEGER, device_usage_id TEXT, address TEXT, device_type_id TEXT, description TEXT, name TEXT, state_key TEXT, parameters TEXT, value_type TEXT);";
    private static final String CREATE_TABLE_FEATURE_ASSOCIATION = "CREATE TABLE table_feature_association (place_id INTEGER, place_type TEXT, device_feature_id INTEGER, id INTEGER, device_feature TEXT );";
    private static final String CREATE_TABLE_FEATURE_STATE = "CREATE TABLE table_feature_state (device_id INTEGER, key TEXT, value TEXT);";
    private static final String CREATE_TABLE_FEATURE_MAP = "CREATE TABLE table_feature_map (id, posx INTEGER, posy INTEGER, map TEXT);";

    private static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/domodroid/.conf/domodroid.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //com.orhanobut.logger.Logger.init("DatabaseHelper").methodCount(0);
        //Logger.i("DATABASE_NAME = " + DATABASE_NAME);
        Log.i("DatabaseHelper","DATABASE_NAME = " + DATABASE_NAME);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        db.execSQL(CREATE_TABLE_AREA);
        db.execSQL(CREATE_TABLE_ROOM);
        db.execSQL(CREATE_TABLE_ICON);
        db.execSQL(CREATE_TABLE_FEATURE);
        db.execSQL(CREATE_TABLE_FEATURE_MAP);
        db.execSQL(CREATE_TABLE_FEATURE_ASSOCIATION);
        db.execSQL(CREATE_TABLE_FEATURE_STATE);
    }


    @Override
    protected void finalize() throws Throwable {
        if (db != null)
            db.close();
        super.finalize();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.w("Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS table_area");
        db.execSQL("DROP TABLE IF EXISTS table_room");
        db.execSQL("DROP TABLE IF EXISTS table_icon");
        db.execSQL("DROP TABLE IF EXISTS table_feature");
        db.execSQL("DROP TABLE IF EXISTS table_feature_association");
        db.execSQL("DROP TABLE IF EXISTS table_feature_state");
        db.execSQL("DROP TABLE IF EXISTS table_feature_map");
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.w("Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS table_area");
        db.execSQL("DROP TABLE IF EXISTS table_room");
        db.execSQL("DROP TABLE IF EXISTS table_icon");
        db.execSQL("DROP TABLE IF EXISTS table_feature");
        db.execSQL("DROP TABLE IF EXISTS table_feature_association");
        db.execSQL("DROP TABLE IF EXISTS table_feature_state");
        db.execSQL("DROP TABLE IF EXISTS table_feature_map");
        onCreate(db);
    }


}
