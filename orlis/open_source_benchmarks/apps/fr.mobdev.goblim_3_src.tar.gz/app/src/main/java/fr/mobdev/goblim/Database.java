/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import fr.mobdev.goblim.objects.Img;
import fr.mobdev.goblim.objects.Server;

/*
 * Helper than manage all access to the database
 */
public class Database extends SQLiteOpenHelper {

	private static Database instance;

	public static Database getInstance(Context context)
	{
		if(instance == null)
			instance = new Database(context, "Lutim.db", null, 3);
		return instance;
	}

	private Database(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("Create table if not exists history (" +
				"id integer primary key autoincrement, url varchar(1024), short_hash varchar(1024), real_short_hash varchar(1024), date INTEGER, storage_duration INTEGER ,thumb TEXT, token varchar(1024));");
		db.execSQL("Create table if not exists servers (" +
				"id integer primary key autoincrement, url varchar(1024), isDefault INTEGER);");

		ContentValues values = new ContentValues();
		values.put("url","https://framapic.org");
		values.put("isDefault",true);
		db.insert("servers",null,values);
		values.clear();
		values.put("url","https://lut.im");
		values.put("isDefault",false);
		db.insert("servers", null, values);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion == 2)
        {
            db.execSQL("Alter table history add column token varchar(1024);");
        }
	}

	public List<Img> getHistory() {
		List<Img> history = new ArrayList<>();
        //ask for history order by date
		String orderBy = "date ASC";
		Cursor cursor = getReadableDatabase().query("history", null, null, null, null, null, orderBy);
		while(cursor.moveToNext())
		{
            //build Img history with data received from database
			int col = 0;
			long id = cursor.getLong(col++);
			String url = cursor.getString(col++);
			String shortHash = cursor.getString(col++);
			String realShortHash = cursor.getString(col++);
			long timestamp = cursor.getLong(col++);
			int storageDuration = cursor.getInt(col++);
			byte[] thumbData = cursor.getBlob(col++);
            String token = cursor.getString(col);

            //convert Long date to Calendar
			Calendar date = Calendar.getInstance();
			date.setTimeZone(TimeZone.getDefault());
			date.setTimeInMillis(timestamp);

			Img img = new Img(id, url, shortHash, realShortHash, date, storageDuration, thumbData, token);

			history.add(img);
		}
        cursor.close();
		return history;
	}

	public void deleteImg(List<Img> deletedList) {
		for(Img img : deletedList)
		{
			String whereClause = "id = ?";
			String[] whereArgs = new String[1];
			whereArgs[0] = String.valueOf(img.getId());
			getWritableDatabase().delete("history", whereClause, whereArgs);
		}
	}

	public Long addImage(Img img) {
		ContentValues values = new ContentValues();
		values.put("url", img.getUrl());
		values.put("short_hash", img.getShortHash());
		values.put("real_short_hash", img.getRealShortHash());
		values.put("date", img.getDate().getTimeInMillis());
		values.put("storage_duration", img.getStorageDuration());
		if(img.getThumbData() != null)
			values.put("thumb",img.getThumbData());
        values.put("token",img.getToken());
		return getWritableDatabase().insert("history", null, values);
	}

	public void addServer(String url)
	{
		ContentValues values = new ContentValues();
		values.put("url",url);
		values.put("isDefault",false);
		getWritableDatabase().insert("servers", null, values);
	}

	public List<Server> getServers(boolean defaultFirst)
	{
		List<Server> servers = new ArrayList<>();
		Cursor cursor = getReadableDatabase().query("servers", null, null, null, null, null, null);
		while(cursor.moveToNext())
		{
			int col = 0;
			long id = cursor.getLong(col++);
			String url = cursor.getString(col++);
			int defValue = cursor.getInt(col);
			boolean isDefault = false;
			if(defValue == 1)
				isDefault = true;
			Server server = new Server(id, url, isDefault);
			if(defaultFirst && isDefault)
			{
				servers.add(0,server);
			}
			else
			{
				servers.add(server);
			}

		}
        cursor.close();
		return servers;
	}

	public void deleteServer(long idServer)
	{
		String whereClause ="id = ?";
		String[] whereArgs = new String[1];
		whereArgs[0] = String.valueOf(idServer);
		getWritableDatabase().delete("servers", whereClause, whereArgs);
	}

	public void setDefaultServer(long newDefaultId, long oldDefaultId)
	{
		String whereClause = "id = ?";
		String[] whereargs = new String[1];
		whereargs[0] = String.valueOf(newDefaultId);
		ContentValues values = new ContentValues();
		values.put("isDefault",true);
		getWritableDatabase().update("servers", values, whereClause, whereargs);

		if(oldDefaultId != -1) {
			values.clear();
			whereargs[0] = String.valueOf(oldDefaultId);
			values.put("isDefault", false);
			getWritableDatabase().update("servers", values, whereClause, whereargs);
		}
	}

	public Img getImage(Long imageId) {

        Img image = null;
        String whereClause = "id = ?";
        String[] whereArgs = new String[1];
        whereArgs[0] = String.valueOf(imageId);
		Cursor cursor = getReadableDatabase().query("history", null, whereClause, whereArgs, null, null, null);
        if(cursor.moveToFirst()) {
            int col = 0;
            Long id = cursor.getLong(col++);
            String url = cursor.getString(col++);
            String short_hash = cursor.getString(col++);
            String real_short_hash = cursor.getString(col++);
            Long date = cursor.getLong(col++);
            int duration = cursor.getInt(col++);
            byte[] thumb = cursor.getBlob(col++);
            String token = cursor.getString(col);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(date);
            image = new Img(id,url,short_hash,real_short_hash,cal,duration,thumb,token);
        }
        cursor.close();
        return image;
	}
}
