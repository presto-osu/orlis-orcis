package ru.subprogram.paranoidsmsblocker.database.tables;

import ru.subprogram.paranoidsmsblocker.database.entities.TASetting;
import ru.subprogram.paranoidsmsblocker.database.entities.CASetting;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CADbTableSetting {

	public static final String TABLE_NAME = "GS_SETTING";
	
	public static final String FIELD_ID = "_id";
	public static final String FIELD_VALUE = "val";
	
	public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
														+ FIELD_ID + " INTEGER PRIMARY KEY, " 
														+ FIELD_VALUE + " TEXT"
														+ ")";

	private static final String SQL_SELECT_FIELDS =
			FIELD_ID + ", " 
			+ FIELD_VALUE + "";

	private static final String SQL_SELECT_BY_ID =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME + " WHERE " + FIELD_ID + " = ? ";

	private final SQLiteDatabase mDatabase;

	public CADbTableSetting(SQLiteDatabase database) {
		mDatabase = database;
	}

	public static void createTable(SQLiteDatabase database) {
		database.execSQL(CADbTableSetting.SQL_DROP_TABLE);
		database.execSQL(CADbTableSetting.SQL_CREATE_TABLE);
	}
	
	private CASetting fetchRow(Cursor cursor) {
		CASetting setting = new CASetting();
		setting.setId(TASetting.getEnum(cursor.getInt(cursor.getColumnIndex(FIELD_ID))));
		setting.setValStr(cursor.getString(cursor.getColumnIndex(FIELD_VALUE)));
		return setting;
	}	
	
	public CASetting getById(TASetting id) {
		Cursor cursor = mDatabase.rawQuery(
				SQL_SELECT_BY_ID,
				new String[] {Integer.toString(id.getValue())});
		
		try {
			if (cursor.moveToNext())
				return fetchRow(cursor);
			else
				return null;
		}
		finally {
			cursor.close();
		}
	
	}
}
