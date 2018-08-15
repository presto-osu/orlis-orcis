/*
 * @author Gabriel Oexle
 * 2015.
 */
package peanutencryption.peanutencryption.SQL;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper implements Serializable {

    private static final int DATABASE_VERSION = 1;
    private String _databaseName;

    private String LOG_str = "peanutencryption";


    public SQLiteHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
        this._databaseName = databaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(this.createDataTable());

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }


    public long insertIntoDataTable(
            String CodeName,
            String Code) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DataTable.CreationDate, new java.util.Date().getTime());
        values.put(DataTable.CodeName, CodeName);
        values.put(DataTable.Code, Code);

        long id = db.insert(DataTable.DataTable, null, values);

        db.close();

        if (id != -1) {
            Log.d(LOG_str, "Data Successful added");
            return id;
        } else {
            Log.e(LOG_str, "Error. Failed to write to DataTable");
            return -1;
        }

    }

    public boolean deleteItemFromDatabase(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DataTable.DataTable, DataTable.DataIDIntern + "=" + id, null) > 0;
    }


    public ArrayList<CodeObject> getAllCodes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] attributes = new String[]{

                DataTable.DataIDIntern,
                DataTable.CreationDate,
                DataTable.CodeName,
                DataTable.Code
        };

        Cursor cursor = db.query(
                DataTable.DataTable,
                attributes,
                null, null, null, null, null, null);


        ArrayList<CodeObject> returnObject = new ArrayList<CodeObject>();

        if (cursor != null && cursor.moveToFirst()) {
        } else {
            Log.i(LOG_str, "No Data was found in the database");
            return returnObject;
        }

        do {
            long longDataID = cursor.getLong((cursor.getColumnIndex(DataTable.DataIDIntern)));
            long longCreationDate = cursor.getLong(cursor.getColumnIndex(DataTable.CreationDate));
            Timestamp sqlCreationTime = new Timestamp(longCreationDate);
            String sqlCodeName = cursor.getString(cursor.getColumnIndex(DataTable.CodeName));
            String sqlCode = cursor.getString(cursor.getColumnIndex(DataTable.Code));


            returnObject.add(new CodeObject(sqlCodeName, sqlCode, sqlCreationTime, longDataID));

        }
        while (cursor.moveToNext());
        cursor.close();
        return returnObject;
    }

    public SQLiteDatabase updateCodes(ArrayList<CodeObject> codeList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        for (CodeObject codeItem : codeList) {
            ContentValues values = new ContentValues();
            values.put(DataTable.Code, codeItem.getCode());
            int returnValue = db.update(DataTable.DataTable, values, DataTable.DataIDIntern + "=?", new String[]{Long.toString(codeItem.getDataID())});
            if (returnValue != 1) {
                db.endTransaction();
                return null;
            }
        }
        return db;

    }


    private String createDataTable() {
        return MessageFormat.format("CREATE TABLE IF NOT EXISTS {0} " +
                        "({1} INTEGER PRIMARY KEY  AUTOINCREMENT," +
                        " {2} INTEGER	NOT NULL," +
                        " {3} TEXT 	NOT NULL," +
                        " {4} TEXT	 	NOT NULL)",
                DataTable.DataTable,
                DataTable.DataIDIntern,
                DataTable.CreationDate,
                DataTable.CodeName,
                DataTable.Code);
    }


    final class DataTable {
        public static final String DataTable = "DataTable";
        public static final String DataIDIntern = "DataIDIntern";
        public static final String CreationDate = "CreationDate";

        public static final String CodeName = "CodeName";

        public static final String Code = "Code";


    }

}
