package ru.subprogram.paranoidsmsblocker.database.tables;

import java.util.ArrayList;

import ru.subprogram.paranoidsmsblocker.database.entities.CASms;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.subprogram.paranoidsmsblocker.exceptions.CAError;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

public class CADbTableSms {

	private static final String TABLE_NAME = "PSB_SMS";

	private static final String FIELD_ID = "_id";
	private static final String FIELD_PHONE = "sms_phone";
	private static final String FIELD_TEXT = "sms_body";
	private static final String FIELD_DATE = "sms_date";

	private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
														+ FIELD_ID + " INTEGER PRIMARY KEY, " 
														+ FIELD_DATE + " INTEGER NOT NULL DEFAULT(0), "
														+ FIELD_PHONE + " TEXT, "
														+ FIELD_TEXT + " TEXT"
														+ ")";

	private static final String SQL_UPDATE_DB_VERSION_TO_2 = "ALTER TABLE " + TABLE_NAME +" ADD COLUMN "
			+ FIELD_DATE + " INTEGER NOT NULL DEFAULT(0)";

	private static final String SQL_SELECT_FIELDS =
			FIELD_ID + ", " 
			+ FIELD_DATE + ", " 
			+ FIELD_PHONE + ", " 
			+ FIELD_TEXT + "";

	private static final String SQL_SELECT_BY_ID =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME + " WHERE " + FIELD_ID + " = ? ";

	private static final String SQL_SELECT_ALL =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME
			+ " ORDER BY " + FIELD_ID + " DESC"
			+ " LIMIT ? OFFSET ?";

	private static final String SQL_SELECT_BY_IDS =
			"SELECT "
					+ SQL_SELECT_FIELDS
					+ " FROM " + TABLE_NAME
					+ " WHERE " + FIELD_ID + " IN(?)"
					+ " ORDER BY " + FIELD_ID + " DESC";

	private static final String SQL_SELECT_LAST_SMS_FROM_ADDRESS =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME 
			+ " WHERE " + FIELD_PHONE + "=?"
			+ " ORDER BY " + FIELD_ID + " DESC";	

	public static void createTable(SQLiteDatabase database) {
		database.execSQL(CADbTableSms.SQL_DROP_TABLE);
		database.execSQL(CADbTableSms.SQL_CREATE_TABLE);
	}

	public static void updateTableToVersion(SQLiteDatabase database, int version) {
		switch (version) {
		case 2:
			database.execSQL(SQL_UPDATE_DB_VERSION_TO_2);
			break;

		default:
			break;
		}
	}

	private final SQLiteDatabase mDatabase;

	public CADbTableSms(SQLiteDatabase database) {
		mDatabase = database;
	}

	public void getAll(ArrayList<CASms> dest) throws CAException {
		getAll(dest, 0, Integer.MAX_VALUE);
	}

	public void getAll(ArrayList<CASms> dest, int offset, int count) throws CAException {
		try {
			String[] args = new String[]{String.valueOf(count), String.valueOf(offset)};
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_ALL, args);
			while (cursor.moveToNext()) {
				dest.add(fetchRow(cursor));
			}
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	public void selectByIds(ArrayList<CASms> dest, ArrayList<Integer> selectedIds) throws CAException {
		StringBuilder ids = new StringBuilder();
		boolean isFirst = true;
		for(Integer id: selectedIds) {
			if(isFirst)
				isFirst = false;
			else
				ids.append(",");
			ids.append(id);
		}

		String[] whereArgs = new String[] { ids.toString() };
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_BY_IDS, whereArgs);
			while (cursor.moveToNext()) {
				dest.add(fetchRow(cursor));
			}
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	public CASms getLastSms(String address) throws CAException {
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_LAST_SMS_FROM_ADDRESS, new String[] {address});
			CASms res = null;
			if(cursor.moveToNext()) {
				res = fetchRow(cursor);
			}
			return res;
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	public CASms getById(int id) throws CAException {
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_BY_ID, new String[] {String.valueOf(id)});
			CASms res = null;
			if(cursor.moveToNext()) {
				res = fetchRow(cursor);
			}
			return res;
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	private CASms fetchRow(Cursor cursor) {
		String address = cursor.getString(cursor.getColumnIndex(FIELD_PHONE));
		String text = cursor.getString(cursor.getColumnIndex(FIELD_TEXT));
		long date = cursor.getLong(cursor.getColumnIndex(FIELD_DATE));
		CASms sms = new CASms(address, text, date);
		sms.setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
		return sms;
	}	

	private ContentValues getContentValues(CASms sms){
		ContentValues values = new ContentValues();
		
		values.put(FIELD_PHONE, sms.getAddress());
		values.put(FIELD_TEXT, sms.getText());
		values.put(FIELD_DATE, sms.getDate());
		
		return values;
	}

	public long insert(CASms sms) {
		try {
			long id = mDatabase.insertOrThrow(TABLE_NAME, null,
						getContentValues(sms));
			sms.setId(id);
			return id;
		}
		catch (Exception e) {
			return -1;
		}
	}

	public void deleteAll() {
		createTable(mDatabase);
	}

	public int deleteByIds(ArrayList<Integer> selectedIds) {
		StringBuilder ids = new StringBuilder();
		boolean isFirst = true;
		for(Integer id: selectedIds) {
			if(isFirst)
				isFirst = false;
			else
				ids.append(",");
			ids.append(id);
		}

		String whereCause = FIELD_ID + " IN ("+ids.toString()+")";
		String[] whereArgs = null;
		int count = mDatabase.delete(TABLE_NAME, whereCause, whereArgs);
		return count;
	}
}
