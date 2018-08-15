package ru.subprogram.paranoidsmsblocker.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.subprogram.paranoidsmsblocker.database.entities.CASetting;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import ru.subprogram.paranoidsmsblocker.database.entities.TASetting;
import ru.subprogram.paranoidsmsblocker.database.tables.CADbTableContacts;
import ru.subprogram.paranoidsmsblocker.database.tables.CADbTableSetting;
import ru.subprogram.paranoidsmsblocker.database.tables.CADbTableSms;
import ru.subprogram.paranoidsmsblocker.exceptions.CAError;
import ru.subprogram.paranoidsmsblocker.exceptions.CAException;

public class CADbEngine {

	private static final String TAG = "CADbEngine";

	private static final int CURR_VERSION = 2;

	private static final String DB_FILE_NAME = "test.db";

	private SQLiteDatabase mDatabase;

	private final Context mContext;

	private CADbTableSetting mTableSetting;

	private CADbTableContacts mTableContacts;

	private CADbTableSms mTableSms;
	
	public CADbEngine(Context context) {
		mContext = context;
	}
	
	public boolean isOpen() {
		return mDatabase != null && mDatabase.isOpen();
	}

	public void open() throws CAException {
		String path = mContext.getFilesDir().getPath();
		String fileName = DB_FILE_NAME;
		openOrCreate(path, fileName);

		mTableSetting = new CADbTableSetting(mDatabase);
		mTableContacts = new CADbTableContacts(mDatabase);
		mTableSms = new CADbTableSms(mDatabase);
		
		checkDbVersion();
	}

	public void clear() {
		close();
		String path = mContext.getFilesDir().getPath();
		File file = new File(path, DB_FILE_NAME);
		file.delete();
	}

	public CADbTableContacts getContactsTable() {
		return mTableContacts;
	}

	public CADbTableSms getSmsTable() {
		return mTableSms;
	}
	
	private void openOrCreate(String path, String fileName) throws CAException {
		
		if (isOpen())
			return;

		if (fileName == null) {
			throw new CAException(CAError.DB_ENGINE_OPENORCREATE_NO_FILENAME);
        }
		
		File file = new File(path);
        if (!file.exists() && !file.mkdirs()){
        	throw new CAException(CAError.DB_ENGINE_OPENORCREATE_FAILED_TO_CREATE_FOLDER);
        }		
		
        try {
			mDatabase = SQLiteDatabase.openDatabase(path + "/" + fileName, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        } catch(SQLiteException e){
    		throw new CAException(CAError.DB_ENGINE_OPENORCREATE_FAILED_TO_OPENORCREATE, e);
        }
	}

	public boolean backup(String path) {
		if(mDatabase.isOpen()) return false;
		String dbpath = mContext.getFilesDir().getPath()+"/"+DB_FILE_NAME;
		return copyFile(dbpath, path);
	}

	public boolean restore(String path) {
		if(mDatabase.isOpen()) return false;
		String dbpath = mContext.getFilesDir().getPath()+"/"+DB_FILE_NAME;
		return copyFile(path, dbpath);
	}
	
	private boolean copyFile(String srcPath, String dstPath) {
		try {
			InputStream is = new FileInputStream(srcPath);
			OutputStream os = new FileOutputStream(dstPath);
	 
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer))>0){
				os.write(buffer, 0, length);
			}
		 
			os.flush();
			os.close();
			is.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	 
	public void close() {
		mDatabase.close();
	}

	private void checkDbVersion() throws CAException {

		int version = 0;
		try {
			CASetting setting = mTableSetting.getById(TASetting.EDbVersion);
			version = setting.getValInt();
		}
		catch(Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
		if (version < 1) {
			CADbTableSetting.createTable(mDatabase);
			CADbTableContacts.createTable(mDatabase);
			CADbTableSms.createTable(mDatabase);
			
			ContentValues values = new ContentValues();
			
			version = CURR_VERSION;
			values.put(CADbTableSetting.FIELD_ID, TASetting.EDbVersion.getValue());
			values.put(CADbTableSetting.FIELD_VALUE, version);
			mDatabase.insert(CADbTableSetting.TABLE_NAME, null, values);
		}

		if(version < 2) {
			version = 2;
			try {
				CADbTableSms.updateTableToVersion(mDatabase, version);
			}
			catch (Exception e) {
				Log.e(TAG,e.getMessage());
			}

			updateVersion(version);
		}

		if(version != CURR_VERSION) {
			throw new CAException(CAError.DB_ENGINE_WRONG_DB_VERSION);
		}
	}

	private void updateVersion(int version) {
		ContentValues values = new ContentValues();
		values.put(CADbTableSetting.FIELD_ID, TASetting.EDbVersion.getValue());
		values.put(CADbTableSetting.FIELD_VALUE, version);
		mDatabase.update(CADbTableSetting.TABLE_NAME, values, CADbTableSetting.FIELD_ID+" = ?",
			new String[] {String.valueOf(TASetting.EDbVersion.getValue())});
	}

}
