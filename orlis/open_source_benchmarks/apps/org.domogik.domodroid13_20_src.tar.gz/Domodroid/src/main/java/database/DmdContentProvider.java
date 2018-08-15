package database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import misc.tracerengine;

public class DmdContentProvider extends ContentProvider {
    private final String mytag = this.getClass().getName();
    private DatabaseHelper mDB;
    private SQLiteDatabase bdd;

    private tracerengine Tracer = null;

    private static final String AUTHORITY = "database.DmdContentProvider";
    private static final int REQUEST_AREA = 100;
    private static final int REQUEST_ROOM = 110;
    private static final int REQUEST_ICON = 120;
    private static final int REQUEST_FEATURE_ALL = 130;
    private static final int REQUEST_FEATURE_BY_ID = 131;
    private static final int REQUEST_FEATURE_MAP = 140;
    private static final int REQUEST_MAP_SWITCHES = 141;
    private static final int REQUEST_FEATURE_ID = 150;
    private static final int REQUEST_FEATURE_STATE = 160;
    private static final int REQUEST_FEATURE_ASSOCIATION = 161;
    private static final int REQUEST_FEATURE_ASSOCIATION_ALL = 162;
    private static final int REQUEST_FEATURE_MAP_ALL = 163;

    private static final int INSERT_AREA = 200;
    private static final int CLEAR_AREA = 201;
    private static final int CLEAR_one_AREA = 202;
    private static final int INSERT_ROOM = 210;
    private static final int CLEAR_ROOM = 211;
    private static final int CLEAR_one_ROOM = 212;
    private static final int INSERT_ICON = 220;
    private static final int CLEAR_ICON = 221;
    private static final int CLEAR_one_ICON = 222;
    private static final int INSERT_FEATURE = 230;
    private static final int CLEAR_FEATURE = 231;
    private static final int CLEAR_one_FEATURE = 232;
    private static final int INSERT_FEATURE_ASSOCIATION = 240;
    private static final int CLEAR_FEATURE_ASSOCIATION = 241;
    private static final int CLEAR_one_FEATURE_ASSOCIATION = 242;
    private static final int CLEAR_one_unique_FEATURE_ASSOCIATION = 243;
    private static final int CLEAR_one_place_type_in_FEATURE_ASSOCIATION = 244;
    private static final int INSERT_FEATURE_MAP = 250;
    private static final int CLEAR_FEATURE_MAP = 251;
    private static final int CLEAR_one_FEATURE_MAP = 252;
    private static final int CLEAR_one_feature_in_FEATURE_MAP = 253;
    private static final int INSERT_FEATURE_STATE = 260;
    private static final int CLEAR_FEATURE_STATE = 261;
    private static final int CLEAR_one_FEATURE_STATE = 262;

    private static final int UPDATE_FEATURE_STATE = 300;
    private static final int UPDATE_FEATURE_NAME = 301;
    private static final int UPDATE_FEATURE_POSITION_ID = 302;
    private static final int UPDATE_AREA_NAME = 303;
    private static final int UPDATE_ROOM_NAME = 304;
    private static final int UPDATE_ICON_NAME = 305;
    private static final int UPDATE_AREA_POSITION_ID = 307;
    private static final int UPDATE_ROOM_POSITION_ID = 308;

    private static final int UPGRADE_FEATURE_STATE = 400;

    private static final String DOMODROID_BASE_PATH = "domodroid";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH);
    public static final Uri CONTENT_URI_REQUEST_AREA = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_AREA");
    public static final Uri CONTENT_URI_REQUEST_ROOM = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_ROOM");
    public static final Uri CONTENT_URI_REQUEST_ICON = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_ICON");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_ALL = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ALL");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_BY_ID = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_BY_ID");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_MAP = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP");
    public static final Uri CONTENT_URI_REQUEST_MAP_SWITCHES = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_MAP_SWITCHES");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_ID = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ID");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_STATE");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ASSOCIATION");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_ASSOCIATION_ALL = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ASSOCIATION_ALL");
    public static final Uri CONTENT_URI_REQUEST_FEATURE_MAP_ALL = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP_ALL");

    public static final Uri CONTENT_URI_INSERT_AREA = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_AREA");
    public static final Uri CONTENT_URI_INSERT_ROOM = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_ROOM");
    public static final Uri CONTENT_URI_INSERT_ICON = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_ICON");
    public static final Uri CONTENT_URI_INSERT_FEATURE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE");
    public static final Uri CONTENT_URI_INSERT_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_ASSOCIATION");
    public static final Uri CONTENT_URI_INSERT_FEATURE_MAP = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_MAP");
    public static final Uri CONTENT_URI_INSERT_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_STATE");

    public static final Uri CONTENT_URI_CLEAR_AREA = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_AREA");
    public static final Uri CONTENT_URI_CLEAR_ROOM = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_ROOM");
    public static final Uri CONTENT_URI_CLEAR_ICON = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_ICON");
    public static final Uri CONTENT_URI_CLEAR_FEATURE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_FEATURE");
    public static final Uri CONTENT_URI_CLEAR_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_FEATURE_ASSOCIATION");
    public static final Uri CONTENT_URI_CLEAR_FEATURE_MAP = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_FEATURE_MAP");
    public static final Uri CONTENT_URI_CLEAR_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_FEATURE_STATE");

    public static final Uri CONTENT_URI_CLEAR_one_AREA = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_AREA");
    public static final Uri CONTENT_URI_CLEAR_one_ROOM = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_ROOM");
    public static final Uri CONTENT_URI_CLEAR_one_ICON = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_ICON");
    public static final Uri CONTENT_URI_CLEAR_one_FEATURE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE");
    public static final Uri CONTENT_URI_CLEAR_one_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_ASSOCIATION");
    public static final Uri CONTENT_URI_CLEAR_one_unique_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_unique_FEATURE_ASSOCIATION");
    public static final Uri CONTENT_URI_CLEAR_one_FEATURE_MAP = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_MAP");
    public static final Uri CONTENT_URI_CLEAR_one_feature_in_FEATURE_MAP = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_feature_in_FEATURE_MAP");
    public static final Uri CONTENT_URI_CLEAR_one_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_STATE");
    public static final Uri CONTENT_URI_CLEAR_one_place_type_in_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/CLEAR_one_place_type_in_FEATURE_ASSOCIATION");

    public static final Uri CONTENT_URI_UPDATE_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_FEATURE_STATE");
    public static final Uri CONTENT_URI_UPDATE_FEATURE_NAME = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_FEATURE_NAME");
    public static final Uri CONTENT_URI_UPDATE_FEATURE_POSITION_ID = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_FEATURE_POSITION_ID");
    public static final Uri CONTENT_URI_UPDATE_AREA_POSITION_ID = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_AREA_POSITION_ID");
    public static final Uri CONTENT_URI_UPDATE_ROOM_POSITION_ID = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_ROOM_POSITION_ID");
    public static final Uri CONTENT_URI_UPDATE_AREA_NAME = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_AREA_NAME");
    public static final Uri CONTENT_URI_UPDATE_ROOM_NAME = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_ROOM_NAME");
    public static final Uri CONTENT_URI_UPDATE_ICON_NAME = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPDATE_ICON_NAME");
    public static final Uri CONTENT_URI_UPGRADE_FEATURE_STATE = Uri.parse("content://" + AUTHORITY + "/" + DOMODROID_BASE_PATH + "/UPGRADE_FEATURE_STATE");


    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/domodroid";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/domodroid";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_AREA", REQUEST_AREA);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_ROOM", REQUEST_ROOM);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_ICON", REQUEST_ICON);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ALL", REQUEST_FEATURE_ALL);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_BY_ID", REQUEST_FEATURE_BY_ID);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP", REQUEST_FEATURE_MAP);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_MAP_SWITCHES", REQUEST_MAP_SWITCHES);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ID", REQUEST_FEATURE_ID);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_STATE", REQUEST_FEATURE_STATE);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ASSOCIATION", REQUEST_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ASSOCIATION_ALL", REQUEST_FEATURE_ASSOCIATION_ALL);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP_ALL", REQUEST_FEATURE_MAP_ALL);

        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_AREA", INSERT_AREA);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_ROOM", INSERT_ROOM);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_ICON", INSERT_ICON);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE", INSERT_FEATURE);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_ASSOCIATION", INSERT_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_MAP", INSERT_FEATURE_MAP);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_STATE", INSERT_FEATURE_STATE);

        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_AREA", CLEAR_AREA);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_ROOM", CLEAR_ROOM);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_FEATURE", CLEAR_FEATURE);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_ICON", CLEAR_ICON);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_FEATURE_ASSOCIATION", CLEAR_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_FEATURE_MAP", CLEAR_FEATURE_MAP);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_FEATURE_STATE", CLEAR_FEATURE_STATE);

        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_AREA", CLEAR_one_AREA);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_ROOM", CLEAR_one_ROOM);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_ICON", CLEAR_one_ICON);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE", CLEAR_one_FEATURE);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_ASSOCIATION", CLEAR_one_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_unique_FEATURE_ASSOCIATION", CLEAR_one_unique_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_place_type_in_FEATURE_ASSOCIATION", CLEAR_one_place_type_in_FEATURE_ASSOCIATION);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_MAP", CLEAR_one_FEATURE_MAP);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_feature_in_FEATURE_MAP", CLEAR_one_feature_in_FEATURE_MAP);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/CLEAR_one_FEATURE_STATE", CLEAR_one_FEATURE_STATE);

        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_FEATURE_STATE", UPDATE_FEATURE_STATE);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_FEATURE_NAME", UPDATE_FEATURE_NAME);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_FEATURE_POSITION_ID", UPDATE_FEATURE_POSITION_ID);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_AREA_POSITION_ID", UPDATE_AREA_POSITION_ID);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_ROOM_POSITION_ID", UPDATE_ROOM_POSITION_ID);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_AREA_NAME", UPDATE_AREA_NAME);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_ROOM_NAME", UPDATE_ROOM_NAME);
        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_ICON_NAME", UPDATE_ICON_NAME);

        sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPGRADE_FEATURE_STATE", UPGRADE_FEATURE_STATE);

    }

    @Override
    public boolean onCreate() {
        mDB = new DatabaseHelper(getContext());
        Context context = getContext();
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
        Tracer = tracerengine.getInstance(SP_params, context);
        return true;

    }

    public void close() {
        mDB.close();
        mDB = null;
        try {
            finalize();
        } catch (Throwable e) {
            Tracer.e(mytag, e.toString());
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // To erase all table contents
        int uriType = sURIMatcher.match(uri);
        if (uriType == UPGRADE_FEATURE_STATE) {
            bdd = mDB.getWritableDatabase();
            bdd.execSQL("delete from table_area where 1=1");
            bdd.execSQL("delete from table_room where 1=1");
            bdd.execSQL("delete from table_icon where 1=1");
            bdd.execSQL("delete from table_feature where 1=1");
            bdd.execSQL("delete from table_feature_association where 1=1");
            bdd.execSQL("delete from table_feature_state where 1=1");
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    public Uri clear(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        switch (uriType) {
            case CLEAR_AREA:
                bdd = mDB.getWritableDatabase();
                bdd.execSQL("delete from table_area where 1=1");
                break;
        }

        return Uri.parse(DOMODROID_BASE_PATH + "/" + id);
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        //long rowid = 0;
        switch (uriType) {
            case INSERT_AREA:
                mDB.getWritableDatabase().insert("table_area", null, values);
                break;
            case INSERT_ROOM:
                mDB.getWritableDatabase().insert("table_room", null, values);
                break;
            case INSERT_ICON:
                mDB.getWritableDatabase().insert("table_icon", null, values);
                break;
            case INSERT_FEATURE:
                mDB.getWritableDatabase().insert("table_feature", null, values);
                break;
            case INSERT_FEATURE_ASSOCIATION:
                mDB.getWritableDatabase().insert("table_feature_association", null, values);
                break;
            case INSERT_FEATURE_MAP:
                //case to add an element in table_feature_map table in DB.
                //Contains device_feature_id (rename as id), posx, posy and map_name
                mDB.getWritableDatabase().insert("table_feature_map", null, values);
                break;
            case INSERT_FEATURE_STATE:
                mDB.getWritableDatabase().insert("table_feature_state", null, values);
                break;

            case CLEAR_AREA:
                mDB.getWritableDatabase().execSQL("delete from table_area where 1=1");
                break;
            case CLEAR_ROOM:
                mDB.getWritableDatabase().execSQL("delete from table_room where 1=1");
                break;
            case CLEAR_ICON:
                Tracer.i(mytag, "Clear icons table");
                mDB.getWritableDatabase().execSQL("delete from table_icon where 1=1");
                break;
            case CLEAR_FEATURE:
                Tracer.i(mytag, "Clear feature table");
                mDB.getWritableDatabase().execSQL("delete from table_feature where 1=1");
                break;
            case CLEAR_FEATURE_ASSOCIATION:
                Tracer.i(mytag, "Clear feature_association table");
                mDB.getWritableDatabase().execSQL("delete from table_feature_association where 1=1");
                break;
            case CLEAR_FEATURE_MAP:
                //this case is call when you want to clear all widgets present on map.
                //it removes them from the table_feature_map table in DB.
                String[] map_name = new String[1];
                map_name[0] = values.getAsString("map");
                Tracer.i(mytag, "Clear widgets from map : " + values.getAsString("map"));
                mDB.getWritableDatabase().delete("table_feature_map", "map=?", map_name);
                break;
            case CLEAR_FEATURE_STATE:
                Tracer.i(mytag, "Clear feature_state table");
                mDB.getWritableDatabase().execSQL("delete from table_feature_state where 1=1");
                break;

            case CLEAR_one_AREA:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_area WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting area: " + e.toString());
                }
                break;
            case CLEAR_one_ROOM:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_room WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting room: " + e.toString());
                }
                break;
            case CLEAR_one_ICON:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_icon WHERE reference=" + values.getAsString("reference") + " AND name='" + values.getAsString("name") + "'");
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting icon: " + e.toString());
                }
                break;
            case CLEAR_one_FEATURE:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting feature: " + e.toString());
                }
                break;
            case CLEAR_one_FEATURE_ASSOCIATION:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature_association WHERE device_feature_id=" + values.getAsString("id"));
                    Tracer.v(mytag, "DELETE FROM table_feature_association WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting feature_association: " + e.toString());
                }
                break;
            case CLEAR_one_unique_FEATURE_ASSOCIATION:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature_association WHERE device_feature_id=" + values.getAsString("id") + " AND place_id=" + values.getAsString("place_id") + " AND place_type='" + values.getAsString("place_type") + "'");
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting one_unique_feature_association: " + e.toString());
                }
                break;
            case CLEAR_one_place_type_in_FEATURE_ASSOCIATION:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature_association WHERE place_id=" + values.getAsString("place_id") + " AND place_type='" + values.getAsString("place_type") + "'");
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting one_place_type_in_FEATURE_ASSOCIATION: " + e.toString());
                }
                break;
            //Add a new select case to remove only one widget on map
            //careful to avoid problem it must be call with id, posx, posy and map
            case CLEAR_one_FEATURE_MAP:
                //Tracer.e(mytag,"Remove one widgets from map : "+values.getAsString("map")+" posx:"+values.getAsString("posx")+" posy:"+values.getAsString("posy")+" id:"+values.getAsString("id")+" id_name:"+id_name[0]);
                try {
                    Resources r = getContext().getResources();
                    int dip20 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, r.getDisplayMetrics());
                    //Get a min and max value to be sure not needing a precise click.
                    int posxlow = values.getAsInteger("posx") - dip20;
                    int posxhigh = values.getAsInteger("posx") + dip20;
                    int posylow = values.getAsInteger("posy") - dip20;
                    int posyhigh = values.getAsInteger("posy") + dip20;
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature_map WHERE id=" + values.getAsString("id") + " AND map='" + values.getAsString("map") + "' AND posx BETWEEN " + posxlow + " AND " + posxhigh + " AND posy BETWEEN " + posylow + " AND " + posyhigh);
                    //Tracer.d(mytag, "Doing sql, DELETE FROM table_feature_map WHERE id="+values.getAsString("id") +" AND map='"+values.getAsString("map")+"' AND posx BETWEEN "+posxlow +" AND "+posxhigh+" AND posy BETWEEN "+posylow +" AND "+posyhigh);
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting widget: " + e.toString());
                }
                break;
            case CLEAR_one_feature_in_FEATURE_MAP:
                try {
                    mDB.getWritableDatabase().execSQL("DELETE FROM table_feature_map WHERE id=" + values.getAsString("id"));
                    Tracer.v(mytag, "DELETE FROM table_feature_map WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error deleting feature_map: " + e.toString());
                }
                break;
            case CLEAR_one_FEATURE_STATE:
                break;

            case UPDATE_FEATURE_NAME:
                //Rename the description of a device because it's what is first display if exist in a widget
                try {
                    mDB.getWritableDatabase().execSQL("UPDATE table_feature SET description='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                    Tracer.d(mytag, "UPDATE table_feature SET description='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error modifying the description of feature: " + e.toString());
                }
                break;
            case UPDATE_FEATURE_POSITION_ID:
                // Update the position id of a widget in current place
                try {
                    //select all feature in this place order by id like when drawing this place.
                    Cursor cursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature_association WHERE place_id=" + values.getAsString("place_id")
                            + " AND place_type='" + values.getAsString("place_type") + "' order by id", null);
                    int new_position_id = 0;
                    int old_position_id = 0;
                    //iterate the cursor to find the previous/current/next position of the selected feature.
                    if (cursor != null && cursor.moveToFirst()) {
                        int previous_position = cursor.getInt(cursor.getColumnIndex("id"));
                        boolean getnext = false;
                        boolean getprev = false;
                        cursor.moveToFirst();
                        //loop until end
                        while (!cursor.isAfterLast()) {
                            int current_device = cursor.getInt(cursor.getColumnIndex("device_feature_id"));
                            int current_position = cursor.getInt(cursor.getColumnIndex("id"));
                            if (current_device == values.getAsInteger("id")) {
                                if (values.getAsString("order").equals("up")) {
                                    //we need to get previous position
                                    getprev = true;
                                } else if (values.getAsString("order").equals("down")) {
                                    //we need to get next position
                                    getnext = true;
                                }
                                //store this position as old one as it's matching device feature id
                                old_position_id = current_position;
                                //store this position in case it is the last one
                                if (cursor.isLast()) {
                                    if (getnext) {
                                        new_position_id = current_position;
                                    } else {
                                        new_position_id = previous_position;
                                    }
                                }
                            } else if (getnext) {
                                //store this position as the next one from previous loop.
                                new_position_id = current_position;
                                getnext = false;
                            } else if (getprev) {
                                new_position_id = previous_position;
                                getprev = false;
                            } else {
                                //store position for next loop
                                previous_position = current_position;
                            }
                            cursor.moveToNext();
                        }
                        cursor.close();
                        Tracer.d(mytag, "Modifying the position of feature: from id " + old_position_id + " to: " + new_position_id);
                        int tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET id='" + tempid + "' WHERE id=" + old_position_id + " AND place_id=" + values.getAsString("place_id") + " AND place_type='" + values.getAsString("place_type") + "'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET id='" + old_position_id + "' WHERE id=" + new_position_id + " AND place_id=" + values.getAsString("place_id") + " AND place_type='" + values.getAsString("place_type") + "'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET id='" + new_position_id + "' WHERE id=" + tempid + " AND place_id=" + values.getAsString("place_id") + " AND place_type='" + values.getAsString("place_type") + "'");
                        // move icons too
                        tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + tempid + "' WHERE reference=" + old_position_id + " AND name='feature'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + old_position_id + "' WHERE reference=" + new_position_id + " AND name='feature'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + new_position_id + "' WHERE reference=" + tempid + " AND name='feature'");
                    }
                } catch (SQLException e) {
                    Tracer.e(mytag, "SQLException Error modifying the position of feature: " + e.toString());
                } catch (Exception e) {
                    Tracer.e(mytag, "GlobalException Error modifying the position of feature: " + e.toString());
                }
                break;
            case UPDATE_ROOM_POSITION_ID:
                // Update the position id of a room
                try {
                    //select all feature in this place order by id like when drawing this place.
                    Cursor cursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM table_room WHERE area_id=" + values.getAsString("place_id") + " order by id", null);
                    int new_position_id = 0;
                    int old_position_id = 0;
                    //iterate the cursor to find the previous/current/next position of the selected feature.
                    if (cursor != null && cursor.moveToFirst()) {
                        int previous_position = cursor.getInt(cursor.getColumnIndex("id"));
                        boolean getnext = false;
                        boolean getprev = false;
                        cursor.moveToFirst();
                        //loop until end
                        while (!cursor.isAfterLast()) {
                            int current_position = cursor.getInt(cursor.getColumnIndex("id"));
                            if (current_position == values.getAsInteger("id")) {
                                if (values.getAsString("order").equals("up")) {
                                    //we need to get previous position
                                    getprev = true;
                                } else if (values.getAsString("order").equals("down")) {
                                    //we need to get next position
                                    getnext = true;
                                }
                                //store this position as old one as it's matching device feature id
                                old_position_id = current_position;
                                //store this position in case it is the last one
                                if (cursor.isLast()) {
                                    if (getnext) {
                                        new_position_id = current_position;
                                    } else {
                                        new_position_id = previous_position;
                                    }
                                }
                            } else if (getnext) {
                                //store this position as the next one from previous loop.
                                new_position_id = current_position;
                                getnext = false;
                            } else if (getprev) {
                                new_position_id = previous_position;
                                getprev = false;
                            } else {
                                //store position for next loop
                                previous_position = current_position;
                            }
                            cursor.moveToNext();
                        }
                    }
                    cursor.close();
                    Tracer.d(mytag, "Modifying the position of room: from id " + old_position_id + " to: " + new_position_id);
                    int tempid = 0;
                    mDB.getWritableDatabase().execSQL("UPDATE table_room SET id='" + tempid + "' WHERE id=" + old_position_id);
                    mDB.getWritableDatabase().execSQL("UPDATE table_room SET id='" + old_position_id + "' WHERE id=" + new_position_id);
                    mDB.getWritableDatabase().execSQL("UPDATE table_room SET id='" + new_position_id + "' WHERE id=" + tempid);
                    // move icons too
                    tempid = 0;
                    mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + tempid + "' WHERE reference=" + old_position_id + " AND name='room'");
                    mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + old_position_id + "' WHERE reference=" + new_position_id + " AND name='room'");
                    mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + new_position_id + "' WHERE reference=" + tempid + " AND name='room'");
                    // move all feature too
                    tempid = 0;
                    mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + tempid + "' WHERE place_id=" + old_position_id + " AND place_type='room'");
                    mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + old_position_id + "' WHERE place_id=" + new_position_id + " AND place_type='room'");
                    mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + new_position_id + "' WHERE place_id=" + tempid + " AND place_type='room'");
                } catch (SQLException e) {
                    Tracer.e(mytag, "SQLException Error modifying the position of room: " + e.toString());
                } catch (Exception e) {
                    Tracer.e(mytag, "GlobalException Error modifying the position of room: " + e.toString());
                }
                break;
            case UPDATE_AREA_POSITION_ID:
                // Update the position id of an area
                try {
                    //select all area by id like when drawing this place.
                    Cursor cursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM table_area order by id", null);
                    int new_position_id = 0;
                    int old_position_id = 0;
                    //iterate the cursor to find the previous/current/next position of the selected feature.
                    if (cursor != null && cursor.moveToFirst()) {
                        int previous_position = cursor.getInt(cursor.getColumnIndex("id"));
                        boolean getnext = false;
                        boolean getprev = false;
                        cursor.moveToFirst();
                        //loop until end
                        while (!cursor.isAfterLast()) {
                            int current_position = cursor.getInt(cursor.getColumnIndex("id"));
                            if (current_position == values.getAsInteger("id")) {
                                if (values.getAsString("order").equals("up")) {
                                    //we need to get previous position
                                    getprev = true;
                                } else if (values.getAsString("order").equals("down")) {
                                    //we need to get next position
                                    getnext = true;
                                }
                                //store this position as old one as it's matching device area id
                                old_position_id = current_position;
                                //store this position in case it is the last one
                                if (cursor.isLast()) {
                                    if (getnext) {
                                        new_position_id = current_position;
                                    } else {
                                        new_position_id = previous_position;
                                    }
                                }
                            } else if (getnext) {
                                //store this position as the next one from previous loop.
                                new_position_id = current_position;
                                getnext = false;
                            } else if (getprev) {
                                new_position_id = previous_position;
                                getprev = false;
                            } else {
                                //store position for next loop
                                previous_position = current_position;
                            }
                            cursor.moveToNext();
                        }
                        cursor.close();
                        Tracer.d(mytag, "Modifying the position of area: from id " + old_position_id + " to: " + new_position_id);
                        int tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_area SET id='" + tempid + "' WHERE id=" + old_position_id);
                        mDB.getWritableDatabase().execSQL("UPDATE table_area SET id='" + old_position_id + "' WHERE id=" + new_position_id);
                        mDB.getWritableDatabase().execSQL("UPDATE table_area SET id='" + new_position_id + "' WHERE id=" + tempid);
                        // move icons too
                        tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + tempid + "' WHERE reference=" + old_position_id + " AND name='area'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + old_position_id + "' WHERE reference=" + new_position_id + " AND name='area'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET reference='" + new_position_id + "' WHERE reference=" + tempid + " AND name='area'");
                        // move all room too
                        tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_room SET area_id='" + tempid + "' WHERE area_id=" + old_position_id);
                        mDB.getWritableDatabase().execSQL("UPDATE table_room SET area_id='" + old_position_id + "' WHERE area_id=" + new_position_id);
                        mDB.getWritableDatabase().execSQL("UPDATE table_room SET area_id='" + new_position_id + "' WHERE area_id=" + tempid);
                        // move all feature too
                        tempid = 0;
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + tempid + "' WHERE place_id=" + old_position_id + " AND place_type='area'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + old_position_id + "' WHERE place_id=" + new_position_id + " AND place_type='area'");
                        mDB.getWritableDatabase().execSQL("UPDATE table_feature_association SET place_id='" + new_position_id + "' WHERE place_id=" + tempid + " AND place_type='area'");
                    }
                } catch (SQLException e) {
                    Tracer.e(mytag, "SQLException Error modifying the position of area: " + e.toString());
                } catch (Exception e) {
                    Tracer.e(mytag, "GlobalException Error modifying the position of area: " + e.toString());
                }
                break;
            case UPDATE_AREA_NAME:
                try {
                    mDB.getWritableDatabase().execSQL("UPDATE table_area SET name='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                    Tracer.d(mytag, "UPDATE table_area SET name='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error modifying the description of area: " + e.toString());
                }
                break;
            case UPDATE_ROOM_NAME:
                try {
                    mDB.getWritableDatabase().execSQL("UPDATE table_room SET name='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                    Tracer.d(mytag, "UPDATE table_room SET name='" + values.getAsString("newname") + "' WHERE id=" + values.getAsString("id"));
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error modifying the description of room: " + e.toString());
                }
                break;
            case UPDATE_ICON_NAME:
                Cursor cursor = null;
                try {
                    cursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM table_icon WHERE reference=" + values.getAsString("reference") + " AND name='" + values.getAsString("name") + "'", null);
                    if (cursor == null || !cursor.moveToFirst()) {
                        mDB.getWritableDatabase().insert("table_icon", null, values);
                    } else {
                        mDB.getWritableDatabase().execSQL("UPDATE table_icon SET value='" + values.getAsString("value") + "' WHERE reference=" + values.getAsString("reference") + " AND name='" + values.getAsString("name") + "'");
                        Tracer.d(mytag, "UPDATE table_icon SET value='" + values.getAsString("value") + "' WHERE reference=" + values.getAsString("reference") + " AND name='" + values.getAsString("name") + "'");
                    }
                } catch (SQLException e) {
                    Tracer.e(mytag, "Error modifying the description of icon: " + e.toString());
                }
                cursor.close();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI= " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(DOMODROID_BASE_PATH + "/" + id);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case REQUEST_AREA:
                //queryBuilder.setTables("table_area");
                //cursor = queryBuilder.query(mDB.getReadableDatabase(),projection, selection, selectionArgs, null, null, sortOrder);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_area order by id"
                        , null);
                //Tracer.d(mytag, "Query on table_area return " + cursor.getCount() + " rows");
                break;
            case REQUEST_ROOM:
                queryBuilder.setTables("table_room");
                cursor = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);

                //Tracer.d(mytag,"Query on table_room return "+cursor.getCount()+" rows for area_id :"+selectionArgs[0]);
                break;
            case REQUEST_ICON:
                queryBuilder.setTables("table_icon");
                cursor = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case REQUEST_FEATURE_ASSOCIATION:
                queryBuilder.setTables("table_feature_association");
                cursor = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, "id");
                break;
            case REQUEST_FEATURE_ALL:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id GROUP BY device_id,state_key",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature order by name COLLATE NOCASE"
                        , null);
                break;
            case REQUEST_FEATURE_BY_ID:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id GROUP BY device_id,state_key",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature WHERE table_feature.id = " + selectionArgs[0]
                        , null);
                break;
            case REQUEST_FEATURE_ASSOCIATION_ALL:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id GROUP BY device_id,state_key",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature_association order by id"
                        , null);
                break;
            case REQUEST_FEATURE_MAP_ALL:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id GROUP BY device_id,state_key",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature_map order by id"
                        , null);
                break;
            case REQUEST_FEATURE_MAP:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_map ON table_feature.id = table_feature_map.id",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature " +
                                "INNER JOIN table_feature_map ON table_feature.id = table_feature_map.id" +
                                " WHERE table_feature_map.map = " + selectionArgs[0]
                        , null);
                break;
            case REQUEST_MAP_SWITCHES:
                //cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_map ON table_feature.id = table_feature_map.id",null);
                cursor = mDB.getReadableDatabase().rawQuery(
                        "SELECT * FROM table_feature_map " +
                                " WHERE table_feature_map.map = " + selectionArgs[0] +
                                " AND table_feature_map.id > 99998"
                        , null);
                break;
            case REQUEST_FEATURE_ID:
                cursor = mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id WHERE table_feature_association.place_id = " + selectionArgs[0] + " AND table_feature_association.place_type=" + "\"" + selectionArgs[1] + "\"" + "order by table_feature_association.id ASC", null);
                break;
            case REQUEST_FEATURE_STATE:
                queryBuilder.setTables("table_feature_state");
                cursor = queryBuilder.query(mDB.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
                //cursor=mDB.getReadableDatabase().rawQuery(
                //		"SELECT value FROM table_feature_state " +
                //		" WHERE table_feature_state.device_id = '"+selectionArgs[0] + "' AND table_feature_state.key = '"+selectionArgs[1]+"' "
                //
                break;

            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int items;
        switch (uriType) {
            case UPDATE_FEATURE_STATE:
                //String id = selectionArgs[0];
                //String skey = selectionArgs[1];
                //Tracer.d("DMDContentProvider.update","try to updated feature_state with device_id = "+id+" skey = "+skey+" selection="+selection);
                items = mDB.getWritableDatabase().update("table_feature_state", values, selection, selectionArgs);
                //Tracer.d("DMDContentProvider.update","Updated rows : "+items);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return items;
    }

}
