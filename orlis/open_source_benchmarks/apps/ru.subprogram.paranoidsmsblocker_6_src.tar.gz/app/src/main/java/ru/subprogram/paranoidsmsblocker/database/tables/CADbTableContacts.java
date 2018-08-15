package ru.subprogram.paranoidsmsblocker.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.subprogram.paranoidsmsblocker.database.entities.CAContact;
import ru.subprogram.paranoidsmsblocker.database.entities.TAContactStatus;
import ru.subprogram.paranoidsmsblocker.exceptions.CAError;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

import java.util.ArrayList;

public class CADbTableContacts {

	private static final String TABLE_NAME = "PSB_PHONES";

	private static final String FIELD_ID = "_id";
	private static final String FIELD_PHONE = "phone";
	private static final String FIELD_STATUS = "status";

	private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
														+ FIELD_ID + " INTEGER PRIMARY KEY, " 
														+ FIELD_STATUS + " INTEGER, "
														+ FIELD_PHONE + " TEXT UNIQUE"
														+ ")";

	private static final String SQL_SELECT_FIELDS =
			FIELD_ID + ", " 
			+ FIELD_STATUS + ", " 
			+ FIELD_PHONE + "";

	private static final String SQL_SELECT_BY_ID =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME + " WHERE " + FIELD_ID + " = ? ";

	private static final String SQL_SELECT_BY_STATUS =
			"SELECT " 
			+ SQL_SELECT_FIELDS 
			+ " FROM " + TABLE_NAME 
			+ " WHERE " + FIELD_STATUS + " = ? ORDER BY " + FIELD_PHONE;	

	public static void createTable(SQLiteDatabase database) {
		database.execSQL(CADbTableContacts.SQL_DROP_TABLE);
		database.execSQL(CADbTableContacts.SQL_CREATE_TABLE);
	}

	private final SQLiteDatabase mDatabase;

	public CADbTableContacts(SQLiteDatabase database) {
		mDatabase = database;
	}

	public CAContact getById(int id) throws CAException {
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_BY_ID,
				new String[]{String.valueOf(id)});
			if (cursor.moveToNext()) {
				return fetchRow(cursor);
			}
			else
				return null;
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	public void getWhiteList(ArrayList<CAContact> dest) throws CAException {
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_BY_STATUS,
				new String[]{String.valueOf(TAContactStatus.EWhiteList.getValue())});
			while (cursor.moveToNext()) {
				dest.add(fetchRow(cursor));
			}
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	public void getBlackList(ArrayList<CAContact> dest) throws CAException {
		try {
			Cursor cursor = mDatabase.rawQuery(SQL_SELECT_BY_STATUS,
					new String[] {String.valueOf(TAContactStatus.EBlackList.getValue())});
			while(cursor.moveToNext()) {
				dest.add(fetchRow(cursor));
			}
		}
		catch (Exception e) {
			throw new CAException(CAError.DB_ENGINE_SQL_ERROR, e);
		}
	}

	private CAContact fetchRow(Cursor cursor) {
		String address = cursor.getString(cursor.getColumnIndex(FIELD_PHONE));
		TAContactStatus status = TAContactStatus.getEnum(cursor.getInt(cursor.getColumnIndex(FIELD_STATUS)));
		CAContact phone = new CAContact(status, address);
		phone.setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
		return phone;
	}	

	private ContentValues getContentValues(CAContact contact){
		ContentValues values = new ContentValues();
		
		values.put(FIELD_PHONE, contact.getAddress());
		values.put(FIELD_STATUS, contact.getStatus().getValue());
		
		return values;
	}

	public long insert(CAContact contact) {
		try {
			long id = mDatabase.insertOrThrow(TABLE_NAME, null,
						getContentValues(contact));
			contact.setId(id);
			return id;
		}
		catch (Exception e) {
			return -1;
		}
	}

	public void moveToWhiteList(CAContact contact) {
		contact.setStatus(TAContactStatus.EWhiteList);
		mDatabase.update(TABLE_NAME, getContentValues(contact), 
				FIELD_ID + "=?", new String[] {String.valueOf(contact.getId())});
	}

	public void moveToBlackList(CAContact contact) {
		contact.setStatus(TAContactStatus.EBlackList);
		mDatabase.update(TABLE_NAME, getContentValues(contact), 
				FIELD_ID + "=?", new String[] {String.valueOf(contact.getId())});
	}
}
