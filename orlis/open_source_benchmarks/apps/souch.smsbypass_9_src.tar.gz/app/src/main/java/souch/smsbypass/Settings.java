/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

public class Settings extends SQLiteOpenHelper {
    private static final String TAG = "Settings";

    public static final String ACTION_NEW_MESSAGE = C.PACKAGE_NAME + ".new_message";

    public static final String ANY_ADDRESS = "#ANY#";

    private static final int DATABASE_VERSION = 1;

    private static final String SETTING_SAVE_MESSAGES = "save_messages";
    private static final String SETTING_VIBRATE = "vibrate";
    private static final String SETTING_FIRST_START = "first_start";

    private static final String DATABASE_NAME = C.PACKAGE_NAME + ".db";

    private static final String SETTINGS_TABLE = "settings";
    private static final String KEY_ID = "id";
    private static final String KEY_KEY = "key";
    private static final String KEY_VALUE = "value";

    private static final String FILTERS_TABLE = "filters";
    private static final String KEY_NAME = "name";
    private static final String KEY_ADDRESS = "address";

    private static final String CONTENT_STRINGS_TABLE = "content_strings";
    private static final String KEY_FILTER_ID = "filter_" + KEY_ID;

    private static final String MESSAGES_TABLE = "messages";
    private static final String KEY_FILTER_NAME = "name";
    private static final String KEY_RECEIVED_AT = "received_at";
    private static final String KEY_MSG_TYPE = "msg_type";

    private static final String FILTERS_ORDER_BY = KEY_NAME + " ASC";
    private static final String MESSAGES_ORDER_BY = KEY_RECEIVED_AT + " DESC";

    private Context mContext;

    public Settings(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE " + SETTINGS_TABLE + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ", " + KEY_KEY + " TEXT NOT NULL UNIQUE"
                        + ", " + KEY_VALUE + " TEXT)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
                "CREATE TABLE " + FILTERS_TABLE + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ", " + KEY_NAME + " TEXT NOT NULL UNIQUE"
                        + ", " + KEY_ADDRESS + " TEXT NOT NULL)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
                "CREATE TABLE " + CONTENT_STRINGS_TABLE + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ", " + KEY_FILTER_ID + " INTEGER REFERENCES " + FILTERS_TABLE + "(" + KEY_ID + ") ON DELETE CASCADE"
                        + ", " + KEY_VALUE + " TEXT NOT NULL)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

        sql =
                "CREATE TABLE " + MESSAGES_TABLE + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                        + ", " + KEY_FILTER_NAME + " TEXT NOT NULL"
                        + ", " + KEY_ADDRESS + " TEXT NOT NULL"
                        + ", " + KEY_RECEIVED_AT + " INTEGER NOT NULL"
                        + ", " + KEY_MSG_TYPE + " INTEGER NOT NULL"
                        + ", " + KEY_VALUE + " TEXT)";
        Log.d(TAG, "SQL:\n" + sql);
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        if (!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys = ON");
        return db;
    }

    public void tryUpgradeDB() {
        // open the DB in writeable mode will be enough to launch onUpgrade if necessary
        getWritableDatabase();
    }

    public String getSetting(String key, String defaultValue) {
        String value = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                SETTINGS_TABLE,
                new String[]{KEY_VALUE},
                KEY_KEY + "=?",
                new String[]{key},
                null, null, null);
        while (cursor.moveToNext()) {
            if (value != null) {
                // There must be only one setting with this key.
                throw new AssertionError();
            }
            value = cursor.getString(0);
        }
        cursor.close();
        db.close();
        if (value == null)
            value = defaultValue;
        return value;
    }

    public void setSetting(String key, String value) {
        if (key.length() == 0)
            throw new AssertionError();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_KEY, key);
        values.put(KEY_VALUE, value);
        long rowID = db.replaceOrThrow(SETTINGS_TABLE, null, values);
        db.close();
        if (rowID < 0)
            throw new AssertionError();
    }

    public boolean saveMessages() {
        final boolean defaultValue = true;
        return Boolean.parseBoolean(getSetting(SETTING_SAVE_MESSAGES, String.valueOf(defaultValue)));
    }

    public void setSaveMessages(boolean value) {
        setSetting(SETTING_SAVE_MESSAGES, Boolean.toString(value));
    }

    public boolean getVibrate() {
        final boolean defaultValue = false;
        return Boolean.parseBoolean(getSetting(SETTING_VIBRATE, String.valueOf(defaultValue)));
    }

    public void setVibrate(boolean value) {
        setSetting(SETTING_VIBRATE, Boolean.toString(value));
    }

    public boolean getFirstStart() {
        final boolean defaultValue = true;
        return Boolean.parseBoolean(getSetting(SETTING_FIRST_START, String.valueOf(defaultValue)));
    }

    public void setFirstStart(boolean value) {
        setSetting(SETTING_FIRST_START, Boolean.toString(value));
    }

    public boolean isFilterNameUsed(String name) {
        return findFilterByName(name) != null;
    }

    public Filter findFilterByName(String name) {
        Filter filter = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                FILTERS_TABLE,
                new String[]{KEY_NAME, KEY_ADDRESS},
                KEY_NAME + "=?",
                new String[]{name},
                null, null, null);
        while (cursor.moveToNext()) {
            if (filter != null) {
                // There must be only one filter with this name.
                throw new AssertionError();
            }
            String address = cursor.getString(1);
            List<String> contentFilters = getContentFilters(name);
            filter = new Filter(name, address, contentFilters);
        }
        cursor.close();
        db.close();
        return filter;
    }


    public Filter getFilterByName(String name) {
        Filter filter = findFilterByName(name);
        if (filter == null)
            throw new AssertionError();
        return filter;
    }

    public List<Filter> getFilters() {
        List<Filter> filters = new ArrayList<Filter>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                FILTERS_TABLE,
                new String[]{KEY_NAME, KEY_ADDRESS},
                null, null, null, null,
                FILTERS_ORDER_BY);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String address = cursor.getString(1);
            List<String> contentFilters = getContentFilters(name);
            filters.add(new Filter(name, address, contentFilters));
        }
        cursor.close();
        db.close();
        return filters;
    }

    public List<String> getContentFilters(String filterName) {
        List<String> contentFilters = new ArrayList<String>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT s." + KEY_VALUE + " FROM " + FILTERS_TABLE + " f"
                        + " JOIN " + CONTENT_STRINGS_TABLE + " s"
                        + "  ON f." + KEY_ID + "=s." + KEY_FILTER_ID
                        + " WHERE f." + KEY_NAME + "=?";
        Cursor cursor = db.rawQuery(sql, new String[]{filterName});
        while (cursor.moveToNext()) {
            String value = cursor.getString(0);
            contentFilters.add(value);
        }
        cursor.close();
        db.close();
        return contentFilters;
    }

    public String getFilterAddress(String name) {
        Filter filter = getFilterByName(name);
        return filter.address;
    }

    public void saveFilter(Filter filter) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            if (filter.name.length() == 0)
                throw new AssertionError();
            if (filter.address.length() == 0)
                throw new AssertionError();

            db.beginTransaction();

            ContentValues values = new ContentValues();
            values.put(KEY_NAME, filter.name);
            values.put(KEY_ADDRESS, filter.address);
            long filter_id = db.replaceOrThrow(FILTERS_TABLE, null, values);
            if (filter_id < 0)
                throw new AssertionError();

            for (String s : filter.contentFilters) {
                if (s.length() == 0)
                    throw new AssertionError();
                values.clear();
                values.put(KEY_FILTER_ID, filter_id);
                values.put(KEY_VALUE, s);
                db.replaceOrThrow(CONTENT_STRINGS_TABLE, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void deleteFilter(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
                FILTERS_TABLE,
                KEY_NAME + "=?",
                new String[]{name});
        db.close();
    }

    public Message getMessage(long id) {
        Message message = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                MESSAGES_TABLE,
                new String[]{KEY_ID, KEY_FILTER_NAME, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_MSG_TYPE, KEY_VALUE},
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        while (cursor.moveToNext()) {
            if (message != null) {
                // There must be only one message with this identifier.
                throw new AssertionError();
            }
            message = getMessageFromCursor(cursor);
        }
        cursor.close();
        db.close();
        if (message == null)
            throw new AssertionError();
        return message;
    }

    // get every stored messages
    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                MESSAGES_TABLE,
                new String[]{KEY_ID, KEY_FILTER_NAME, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_MSG_TYPE, KEY_VALUE},
                null, null, null, null, MESSAGES_ORDER_BY);
        while (cursor.moveToNext())
            messages.add(getMessageFromCursor(cursor));
        cursor.close();
        db.close();
        return messages;
    }

    // get every stored messages of a given filter
    public List<Message> getMessages(String filterName, Boolean getDraft) {
        List<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = getReadableDatabase();
        String filterClause = KEY_FILTER_NAME + "=?";
        String[] selArgs;
        if (!getDraft) {
            filterClause += " AND " + KEY_MSG_TYPE + "!=?";
            selArgs = new String[]{filterName, String.valueOf(Message.MSG_TYPE_DRAFT)};
        }
        else {
            selArgs = new String[]{filterName};
        }
        Cursor cursor = db.query(
                MESSAGES_TABLE,
                new String[]{KEY_ID, KEY_FILTER_NAME, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_MSG_TYPE, KEY_VALUE},
                filterClause, selArgs, null, null,
                KEY_RECEIVED_AT + " ASC");
        while (cursor.moveToNext())
            messages.add(getMessageFromCursor(cursor));
        cursor.close();
        db.close();
        return messages;
    }

    public long saveMessage(String filter, String address, long receivedAt, long msgType, String message) {
        if (address.length() == 0)
            throw new AssertionError();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FILTER_NAME, filter);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_RECEIVED_AT, receivedAt);
        values.put(KEY_MSG_TYPE, msgType);
        values.put(KEY_VALUE, message);
        long messageID = db.replaceOrThrow(MESSAGES_TABLE, null, values);
        db.close();
        if (messageID < 0)
            throw new AssertionError();
        Intent intent = new Intent(ACTION_NEW_MESSAGE);
        mContext.sendBroadcast(intent);
        if (msgType == Message.MSG_TYPE_RECEIVED)
            showMessageNotification(messageID);
        return messageID;
    }

    public void saveDraftMessage(String filter, String address, String message) {
        if (filter.length() == 0)
            return;

        Message oldDraftMessage = getDraftMessage(filter);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (oldDraftMessage != null) {
            values.put(KEY_ID, oldDraftMessage.id);
        }
        values.put(KEY_FILTER_NAME, filter);
        values.put(KEY_ADDRESS, address);
        values.put(KEY_RECEIVED_AT, System.currentTimeMillis());
        values.put(KEY_MSG_TYPE, Message.MSG_TYPE_DRAFT);
        values.put(KEY_VALUE, message);
        long messageID = db.replaceOrThrow(MESSAGES_TABLE, null, values);
        db.close();
        if (messageID < 0)
            throw new AssertionError();
        Intent intent = new Intent(ACTION_NEW_MESSAGE);
        mContext.sendBroadcast(intent);
    }

    public Message getDraftMessage(String filter) {
        if (filter.length() == 0)
            return null;
        Message draftMessage = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                MESSAGES_TABLE,
                new String[]{KEY_ID, KEY_FILTER_NAME, KEY_ADDRESS, KEY_RECEIVED_AT, KEY_MSG_TYPE, KEY_VALUE},
                KEY_FILTER_NAME + "=? AND " + KEY_MSG_TYPE + "=?",
                new String[]{filter, String.valueOf(Message.MSG_TYPE_DRAFT)},
                null, null, null);
        while (cursor.moveToNext())
            draftMessage = getMessageFromCursor(cursor);
        cursor.close();
        db.close();
        return draftMessage;
    }

    public void deleteDraftMessage(String filter) {
        Message draftMessage = getDraftMessage(filter);
        if (draftMessage != null) {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(
                    MESSAGES_TABLE,
                    KEY_ID + "=?",
                    new String[]{String.valueOf(draftMessage.id)});
            db.close();
            Intent intent = new Intent(ACTION_NEW_MESSAGE);
            mContext.sendBroadcast(intent);
        }
    }

    public void deleteMessage(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
                MESSAGES_TABLE,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();

        // If there was any notification for this message, it is no longer
        // relevant. Hence, remove it.
        Notifier.cancel(mContext, Notifier.NEW_MESSAGE);
    }

    private Message getMessageFromCursor(Cursor cursor) {
        long id = cursor.getLong(0);
        String filter = cursor.getString(1);
        String address = cursor.getString(2);
        long receivedAt = cursor.getLong(3);
        long msgType = cursor.getLong(4);
        String value = cursor.getString(5);
        return new Message(id, filter, address, receivedAt, msgType, value);
    }

    private void showMessageNotification(long messageID) {
        // delete previous notification in order to show only one notification
        Notifier.cancel(mContext, Notifier.NEW_MESSAGE);

        float level = BatteryFacade.GetBatteryLevel(mContext.getApplicationContext());
        String strLevel = String.format(mContext.getString(R.string.batteryNotificationMessage), level);
        Notification notification = Notifier.build(R.drawable.ic_stat_alert, strLevel);

        // just do nothing
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(), 0);
        notification.setLatestEventInfo(mContext, strLevel, "", pendingIntent);
        Notifier.notify(mContext, Notifier.NEW_MESSAGE, notification);
    }


    public ArrayList<String> getContacts(String address) {
        ArrayList<String> contacts = new ArrayList<String>();

        // Resolving the contact name from the contacts.
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(address));
        Cursor cursor = mContext.getContentResolver().query(lookupUri,
                new String[]{ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        String displayName = cursor.getString(0);
                        //Toast.makeText(context, address + ": " + displayName, Toast.LENGTH_LONG).show();
                        contacts.add(displayName);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                cursor.close();
            }
        }

        return contacts;
    }

    public String getContactAddress(String contact) {
        String address = null;
        Cursor phones = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if (phones != null) {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                if (name.equalsIgnoreCase(contact)) {
                    // take first address
                    address = phoneNumber;
                    break;
                }
            }
            phones.close();
        }

        return address;
    }
}
