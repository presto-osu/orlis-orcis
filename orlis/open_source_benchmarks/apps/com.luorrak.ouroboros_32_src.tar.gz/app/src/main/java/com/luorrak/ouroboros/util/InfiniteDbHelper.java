package com.luorrak.ouroboros.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.luorrak.ouroboros.util.DbContract.BoardEntry;
import com.luorrak.ouroboros.util.DbContract.CatalogEntry;
import com.luorrak.ouroboros.util.DbContract.ReplyCheck;
import com.luorrak.ouroboros.util.DbContract.ThreadEntry;
import com.luorrak.ouroboros.util.DbContract.ThreadReplyCountTracker;
import com.luorrak.ouroboros.util.DbContract.UserPosts;
import com.luorrak.ouroboros.util.DbContract.WatchlistEntry;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
 */
public class InfiniteDbHelper extends SQLiteOpenHelper{

    private final String LOG_TAG = InfiniteDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "cache.db";
    private SQLiteDatabase db = getWritableDatabase();
    public static final int trueFlag = 1;
    public static final int falseFlag = 0;

    // Constructor /////////////////////////////////////////////////////////////////////////////////

    public InfiniteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Catalog Helper Functions ////////////////////////////////////////////////////////////////////

    public boolean insertCatalogEntry (String board, String no, String filename, String tim, String ext, String sub, String comment,
                                       Integer replies, Integer images, Integer sticky, Integer locked, String embed){
        ContentValues values = new ContentValues();
        values.put(CatalogEntry.COLUMN_BOARD_NAME, board); //LOOK UP KEY FOR THIS
        values.put(CatalogEntry.COLUMN_CATALOG_NO, no);
        values.put(CatalogEntry.COLUMN_CATALOG_FILENAME, filename);
        values.put(CatalogEntry.COLUMN_CATALOG_TIM, tim);
        values.put(CatalogEntry.COLUMN_CATALOG_EXT, ext);
        values.put(CatalogEntry.COLUMN_CATALOG_SUB, sub);
        values.put(CatalogEntry.COLUMN_CATALOG_COM, comment);
        values.put(CatalogEntry.COLUMN_CATALOG_REPLIES, replies);
        values.put(CatalogEntry.COLUMN_CATALOG_IMAGES, images);
        values.put(CatalogEntry.COLUMN_CATALOG_STICKY, sticky);
        values.put(CatalogEntry.COLUMN_CATALOG_LOCKED, locked);
        values.put(CatalogEntry.COLUMN_CATALOG_EMBED, embed);

        try {
            db.insertOrThrow(
                    CatalogEntry.TABLE_NAME,
                    null,
                    values
            );
            return true;
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + CatalogEntry.TABLE_NAME +
                    " NO: " + no);
            return false;
        }
    }

    public Cursor getCatalogCursor(String sortBy){
        Cursor cursor;
        if (sortBy.equals(SettingsHelper.BUMP_ORDER)){
            cursor = db.query(
                    CatalogEntry.TABLE_NAME, //table name
                    null, //columns to search
                    null, //where clause
                    null, //where arguements
                    null, //Group by
                    null, //having
                    null //orderby
            );
        } else if (sortBy.equals(SettingsHelper.CREATION_DATE)){
            cursor = db.query(
                    CatalogEntry.TABLE_NAME, //table name
                    null, //columns to search
                    null, //where clause
                    null, //where arguements
                    null, //Group by
                    null, //having
                    CatalogEntry.COLUMN_CATALOG_NO + " DESC" //orderby
            );
        } else {
            //SettingsHelper.REPLY_COUNT
            cursor = db.query(
                    CatalogEntry.TABLE_NAME, //table name
                    null, //columns to search
                    null, //where clause
                    null, //where arguements
                    null, //Group by
                    null, //having
                    CatalogEntry.COLUMN_CATALOG_REPLIES +" DESC" //orderby
            );
        }

        cursor.moveToFirst();
        return cursor;
    }

    public Cursor searchCatalogForThread(String searchString, String sortBy) {
        Cursor cursor;
        if (searchString == null || searchString.length() == 0){
            cursor = getCatalogCursor(sortBy);
        } else {
            cursor = db.query(
                    CatalogEntry.TABLE_NAME, //table name
                    null, //columns to search
                    CatalogEntry.COLUMN_CATALOG_COM + " LIKE ? OR " + CatalogEntry.COLUMN_CATALOG_SUB + " LIKE ?", //where clause
                    new String[] {"%" + searchString + "%", "%" + searchString + "%"}, //where arguements
                    null, //Group by
                    null, //having
                    null,
                    null//orderby
            );
        }
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void deleteCatalogCache(){
        //Delete all rows in table
        db.delete(CatalogEntry.TABLE_NAME, null, null);
    }

    // Thread Helper Functions /////////////////////////////////////////////////////////////////////

    public boolean insertThreadEntry(String board, String resto, String no, String sub, String com,
                                     String email, String name, String trip, String time, String last_modified,
                                     String id, String embed, byte[] mediaFiles, int position){
        ContentValues values = new ContentValues();
        values.put(ThreadEntry.COLUMN_BOARD_NAME, board);
        values.put(ThreadEntry.COLUMN_THREAD_RESTO, resto);
        values.put(ThreadEntry.COLUMN_THREAD_NO, no);
        values.put(ThreadEntry.COLUMN_THREAD_SUB, sub);
        values.put(ThreadEntry.COLUMN_THREAD_COM, com);
        values.put(ThreadEntry.COLUMN_THREAD_EMAIL, email);
        values.put(ThreadEntry.COLUMN_THREAD_NAME, name);
        values.put(ThreadEntry.COLUMN_THREAD_TRIP, trip);
        values.put(ThreadEntry.COLUMN_THREAD_TIME, time);
        values.put(ThreadEntry.COLUMN_THREAD_LAST_MODIFIED, last_modified);
        values.put(ThreadEntry.COLUMN_THREAD_ID, id);
        values.put(ThreadEntry.COLUMN_THREAD_EMBED, embed);
        values.put(ThreadEntry.COLUMN_THREAD_MEDIA_FILES, mediaFiles);
        values.put(ThreadEntry.COLUMN_POSITION, position);

        try {
            db.insertOrThrow(
                    ThreadEntry.TABLE_NAME,
                    null,
                    values
            );
            return true;
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + ThreadEntry.TABLE_NAME +
                    " NO: " + no);
            return false;
        }
    }

    public Cursor getThreadCursor(String resto){
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                ThreadEntry.COLUMN_THREAD_RESTO + "=?", //where clause
                new String[] {resto}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public void deleteThreadCache(){
        //Delete all rows in table
        db.delete(ThreadEntry.TABLE_NAME, null, null);
    }

    public Cursor getPost(String postNo){
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                "no=?", //where clause
                new String[] {postNo}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        return cursor;
    }

    public Cursor getReplies(String postNo) {
        Cursor cursor = db.query(
                ThreadEntry.TABLE_NAME, //table name
                null, //columns to search
                ThreadEntry.COLUMN_THREAD_COM + " LIKE ?", //where clause
                new String[] {"%onclick=\"highlightReply('" + postNo + "%"}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor searchThreadForString(String searchString, String resto) {
        Cursor cursor;
        if (searchString == null || searchString.length() == 0){
            cursor = getThreadCursor(resto);
        } else {
            cursor = db.query(
                    ThreadEntry.TABLE_NAME, //table name
                    null, //columns to search
                    ThreadEntry.COLUMN_THREAD_RESTO + "= ? AND (" +
                            ThreadEntry.COLUMN_THREAD_COM + " LIKE ? OR " +
                            ThreadEntry.COLUMN_THREAD_SUB + " LIKE ? OR "  +
                            ThreadEntry.COLUMN_THREAD_ID + " LIKE ? OR " +
                            ThreadEntry.COLUMN_THREAD_NAME + " LIKE ? OR " +
                            ThreadEntry.COLUMN_THREAD_TRIP + " LIKE ? OR " +
                            ThreadEntry.COLUMN_THREAD_NO + " LIKE ?)", //where clause
                    new String[] {resto,
                            "%" + searchString + "%",
                            "%" + searchString + "%",
                            "%" + searchString + "%",
                            "%" + searchString + "%",
                            "%" + searchString + "%",
                            "%" + searchString + "%"}, //where arguements
                    null, //Group by
                    null, //having
                    null,
                    null//orderby
            );
        }
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    // Reply Check /////////////////////////////////////////////////////////////////////////////////
    //Just a mirror of Thread to prevent deletion when browsing

    public boolean insertRCEntry(String board, String resto, String no, String sub, String com,
                                     String email, String name, String trip, String time, String last_modified,
                                     String id, String embed, byte[] mediaFiles, int position){
        ContentValues values = new ContentValues();
        values.put(ReplyCheck.COLUMN_BOARD_NAME, board);
        values.put(ReplyCheck.COLUMN_THREAD_RESTO, resto);
        values.put(ReplyCheck.COLUMN_THREAD_NO, no);
        values.put(ReplyCheck.COLUMN_THREAD_SUB, sub);
        values.put(ReplyCheck.COLUMN_THREAD_COM, com);
        values.put(ReplyCheck.COLUMN_THREAD_EMAIL, email);
        values.put(ReplyCheck.COLUMN_THREAD_NAME, name);
        values.put(ReplyCheck.COLUMN_THREAD_TRIP, trip);
        values.put(ReplyCheck.COLUMN_THREAD_TIME, time);
        values.put(ReplyCheck.COLUMN_THREAD_LAST_MODIFIED, last_modified);
        values.put(ReplyCheck.COLUMN_THREAD_ID, id);
        values.put(ReplyCheck.COLUMN_THREAD_EMBED, embed);
        values.put(ReplyCheck.COLUMN_THREAD_MEDIA_FILES, mediaFiles);
        values.put(ReplyCheck.COLUMN_REPLY_CHECK_POSITION, position);

        try {
            db.insertOrThrow(
                    ReplyCheck.TABLE_NAME,
                    null,
                    values
            );
            return true;
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + ReplyCheck.TABLE_NAME +
                    " NO: " + no);
            return false;
        }
    }

    public Cursor getRCCursor(String resto){
        Cursor cursor = db.query(
                ReplyCheck.TABLE_NAME, //table name
                null, //columns to search
                ReplyCheck.COLUMN_THREAD_RESTO + "=?", //where clause
                new String[] {resto}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public void deleteRCCache(){
        //Delete all rows in table
        db.delete(ReplyCheck.TABLE_NAME, null, null);
    }

    public Cursor getRCPost(String userPostBoardName, String userPostResto, String userPostNo){
        Cursor cursor = db.query(
                ReplyCheck.TABLE_NAME, //table name
                null, //columns to search
                ReplyCheck.COLUMN_BOARD_NAME + "=? AND " + ReplyCheck.COLUMN_THREAD_RESTO + "=? AND " + ReplyCheck.COLUMN_THREAD_NO + "=?", //where clause
                new String[] {userPostBoardName, userPostResto, userPostNo}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getRCReplies(String postNo) {
        Cursor cursor = db.query(
                ReplyCheck.TABLE_NAME, //table name
                null, //columns to search
                ReplyCheck.COLUMN_THREAD_COM + " LIKE ?", //where clause
                new String[] {"%onclick=\"highlightReply('" + postNo + "%"}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );
        cursor.moveToFirst();
        return cursor;
    }

    // Gallery Function ////////////////////////////////////////////////////////////////////////////

    public Cursor getGalleryCursor(String resto) {
        Cursor cursor = db.query(ThreadEntry.TABLE_NAME,
                null,
                ThreadEntry.COLUMN_THREAD_MEDIA_FILES + " IS NOT NULL AND " + ThreadEntry.COLUMN_THREAD_RESTO + "=?",
                new String[] {resto},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor;
    }

    // Board Helper Functions //////////////////////////////////////////////////////////////////////

    public void insertBoardEntry(String board, int orderId){
        long newRowId;

        ContentValues values = new ContentValues();
        values.put(BoardEntry.COLUMN_BOARDS, board);
        values.put(BoardEntry.BOARD_ORDER, orderId);

        try {
            db.insertOrThrow(
                    BoardEntry.TABLE_NAME,
                    null,
                    values
            );
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + BoardEntry.TABLE_NAME);
        }
    }

    @Deprecated
    public void deleteBoardEntry(String board){
        db.delete(BoardEntry.TABLE_NAME,
                BoardEntry.COLUMN_BOARDS + "=?",
                new String[]{board}
        );
    }

    public Cursor getBoardCursor(){

        Cursor cursor = db.query(
                BoardEntry.TABLE_NAME,
                null,
                null, //selection
                null, //selection args
                null, //group by
                null, //having
                BoardEntry.BOARD_ORDER + " ASC"  //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    private int findIdbyBoardOrder(int boardOrder){
        Cursor cursor = db.query(
                BoardEntry.TABLE_NAME,
                null,
                BoardEntry.BOARD_ORDER + " = ?",
                new String[] {String.valueOf(boardOrder)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        cursor.close();
        return id;
    }

    private void updateBoardOrder(int id, int newOrderValue){
        ContentValues values = new ContentValues();
        values.put(BoardEntry.BOARD_ORDER, newOrderValue);

        String selection = BoardEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(
                BoardEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public void swapBoardOrder(int fromPosition, int toPosition){
        ContentValues values = new ContentValues();
        //SELECT orderID FROM BoardEntry.TABLE_NAME WHERE orderId > toPosition
        int positionOne;
        int positionTwo;
        //Drag Down
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                //find first _id
                positionOne = findIdbyBoardOrder(i);
                positionTwo = findIdbyBoardOrder(i + 1);
                updateBoardOrder(positionOne, i + 1);
                updateBoardOrder(positionTwo, i);
            }
        //DragUp
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                positionOne = findIdbyBoardOrder(i);
                positionTwo = findIdbyBoardOrder(i - 1);
                updateBoardOrder(positionOne, i - 1);
                updateBoardOrder(positionTwo, i);
            }
        }
    }

    public void removeBoardEntry(int position){
        Cursor cursor = getBoardCursor();
        int boardListCount = cursor.getCount() - 1;
        swapBoardOrder(position, boardListCount);
        cursor.close();
        db.delete(
                BoardEntry.TABLE_NAME,
                BoardEntry.BOARD_ORDER + " =?",
                new String[]{String.valueOf(boardListCount)}
        );
    }
    // User Posts Functions ////////////////////////////////////////////////////////////////////////

    public void insertUserPostEntry(String board, String no, String resto, String subject, String comment){

        ContentValues values = new ContentValues();
        values.put(UserPosts.COLUMN_BOARDS, board);
        values.put(UserPosts.COLUMN_NO, no);
        values.put(UserPosts.COLUMN_RESTO, resto);
        values.put(UserPosts.COLUMN_SUBJECT, subject);
        values.put(UserPosts.COLUMN_COMMENT, comment);
        values.put(UserPosts.COLUMN_NUMBER_OF_REPLIES, 0);
        values.put(UserPosts.COLUMN_NEW_REPLY_FLAG, falseFlag);
        values.put(UserPosts.COLUMN_ERROR_COUNT, 0);
        values.put(UserPosts.COLUMN_POSITION, 0);

        try {
            db.insertOrThrow(
                    UserPosts.TABLE_NAME,
                    null,
                    values
            );
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + UserPosts.TABLE_NAME);
        }
    }

    public void deleteUserPostsEntry(String rowId){
        db.delete(UserPosts.TABLE_NAME,
                UserPosts._ID + "=?",
                new String[]{rowId});
    }

    public boolean isNoUserPost(String boardName, String no) {
       return DatabaseUtils.queryNumEntries(
                db, //Database
                UserPosts.TABLE_NAME, //Table name
                UserPosts.COLUMN_BOARDS + "=? AND " + UserPosts.COLUMN_NO + "=?", //where clause
                new String[] {boardName, no}) > 0; // selection
    }

    public void updateUserPostReplyCount(String rowID, int newReplyCount){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserPosts.COLUMN_NUMBER_OF_REPLIES, newReplyCount);
        db.update(UserPosts.TABLE_NAME,
                contentValues, //values
                UserPosts._ID + "= ?", //where
                new String[]{rowID} //where args
        );
    }

    public void updateUserPostPosition(String rowID, int position){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserPosts.COLUMN_POSITION, position);

        db.update(UserPosts.TABLE_NAME,
                contentValues, //values
                UserPosts._ID + "= ?", //where
                new String[]{rowID} //where args
        );
    }

    public void addUserPostFlag(String rowID){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserPosts.COLUMN_NEW_REPLY_FLAG, trueFlag);

        db.update(UserPosts.TABLE_NAME,
                contentValues, //values
                UserPosts._ID + "= ?", //where
                new String[]{rowID} //where args
        );
    }

    public void removeUserPostFlag(String rowID){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserPosts.COLUMN_NEW_REPLY_FLAG, falseFlag);

        db.update(UserPosts.TABLE_NAME,
                contentValues, //values
                UserPosts._ID + "= ?", //where
                new String[]{rowID} //where args
        );
    }

    public void updateUserPostErrorCount(String rowID, int errorCount){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserPosts.COLUMN_ERROR_COUNT, errorCount);

        db.update(UserPosts.TABLE_NAME,
                contentValues, //values
                UserPosts._ID + "= ?", //where
                new String[]{rowID} //where args
        );
    }

    public Cursor getUserPostsCursor(){

        Cursor cursor = db.query(
                UserPosts.TABLE_NAME,
                null,
                null, //selection
                null, //selection args
                null, //group by
                null, //having
                UserPosts.COLUMN_RESTO + " ASC" //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getFlaggedUserPostsCursor(){

        Cursor cursor = db.query(
                UserPosts.TABLE_NAME,
                null,
                UserPosts.COLUMN_NEW_REPLY_FLAG + " =?", //selection
                new String[] {String.valueOf(trueFlag)}, //selection args
                null, //group by
                null, //having
                UserPosts._ID + " DESC" //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    // Watchlist Functions /////////////////////////////////////////////////////////////////////////

    public void insertWatchlistEntry(String title, String board, String no, byte[] serializedMediaList, int orderId){
        ContentValues values = new ContentValues();
        values.put(WatchlistEntry.COLUMN_TITLE, title);
        values.put(WatchlistEntry.COLUMN_BOARD, board);
        values.put(WatchlistEntry.COLUMN_NO, no);
        values.put(WatchlistEntry.COLUMN_MEDIA_FILES, serializedMediaList);
        values.put(WatchlistEntry.WATCHLIST_ORDER, orderId);

        try {
            db.insertOrThrow(
                    WatchlistEntry.TABLE_NAME,
                    null,
                    values
            );
        } catch (SQLException e){
            Log.e(LOG_TAG, "Error Inserting row into " + WatchlistEntry.TABLE_NAME);
        }
    }

    public Cursor getWatchlistCursor(){

        Cursor cursor = db.query(
                WatchlistEntry.TABLE_NAME,
                null,
                null, //selection
                null, //selection args
                null, //group by
                null, //having
                WatchlistEntry.WATCHLIST_ORDER + " ASC"  //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    private int findIdbyWatchlistOrder(int watchlistOrder){
        Cursor cursor = db.query(
                WatchlistEntry.TABLE_NAME,
                null,
                WatchlistEntry.WATCHLIST_ORDER + " = ?",
                new String[] {String.valueOf(watchlistOrder)},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        cursor.close();
        return id;
    }


    private void updateWatchlistOrder(int id, int newOrderValue){
        ContentValues values = new ContentValues();
        values.put(WatchlistEntry.WATCHLIST_ORDER, newOrderValue);

        String selection = BoardEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        db.update(
                WatchlistEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }

    public void swapWatchlistOrder(int fromPosition, int toPosition){
        ContentValues values = new ContentValues();
        //SELECT orderID FROM BoardEntry.TABLE_NAME WHERE orderId > toPosition
        int positionOne;
        int positionTwo;
        //Drag Down
        if (fromPosition < toPosition){
            for (int i = fromPosition; i < toPosition; i++) {
                //find first _id
                positionOne = findIdbyWatchlistOrder(i);
                positionTwo = findIdbyWatchlistOrder(i + 1);
                updateWatchlistOrder(positionOne, i + 1);
                updateWatchlistOrder(positionTwo, i);
            }
            //DragUp
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                positionOne = findIdbyWatchlistOrder(i);
                positionTwo = findIdbyWatchlistOrder(i - 1);
                updateWatchlistOrder(positionOne, i - 1);
                updateWatchlistOrder(positionTwo, i);
            }
        }
    }

    public void removeWatchlistEntry(int position){
        Cursor cursor = getWatchlistCursor();
        int boardListCount = cursor.getCount() - 1;
        swapWatchlistOrder(position, boardListCount);
        cursor.close();
        db.delete(
                WatchlistEntry.TABLE_NAME,
                WatchlistEntry.WATCHLIST_ORDER + " =?",
                new String[] {String.valueOf(boardListCount)}
        );
    }

    // THREAD REPLY COUNT TABLE ////////////////////////////////////////////////////////////////////

    public Cursor getThreadReplyCountCursor(String resto) {
        Cursor cursor = db.query(
                ThreadReplyCountTracker.TABLE_NAME, //table name
                null, //columns to search
                ThreadReplyCountTracker.RESTO + "=?", //where clause
                new String[]{resto}, //where arguements
                null, //Group by
                null, //having
                null //orderby
        );

        cursor.moveToFirst();
        return cursor;
    }

    public void updateThreadReplyCount(String boardName, String resto, int newReplyCount){
        ContentValues values = new ContentValues();
        values.put(ThreadReplyCountTracker.BOARD_NAME, boardName);
        values.put(ThreadReplyCountTracker.RESTO, resto);
        values.put(ThreadReplyCountTracker.REPLY_COUNT, newReplyCount);

        int rowsUpdated = db.updateWithOnConflict(ThreadReplyCountTracker.TABLE_NAME,
                    values, //values
                    ThreadReplyCountTracker.BOARD_NAME + "= ? AND " + ThreadReplyCountTracker.RESTO + " = ?", //where
                    new String[]{boardName, resto},
                    SQLiteDatabase.CONFLICT_FAIL);//where args
        if (rowsUpdated == 0) {
            db.insert(ThreadReplyCountTracker.TABLE_NAME,
                    null,
                    values);
        }
    }

    // Database Lifecycle Functions ////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOARD_TABLE = "CREATE TABLE IF NOT EXISTS " + BoardEntry.TABLE_NAME + " (" +
                BoardEntry._ID + " INTEGER PRIMARY KEY, " +
                BoardEntry.COLUMN_BOARDS + " TEXT UNIQUE NOT NULL, " +
                BoardEntry.BOARD_ORDER +" INTEGER NOT NULL);";

        final String SQL_CREATE_CATALOG_TABLE = " CREATE TABLE IF NOT EXISTS " + CatalogEntry.TABLE_NAME + " (" +

                CatalogEntry._ID + " INTEGER PRIMARY KEY, " +

                //Name of board and foreign key
                CatalogEntry.COLUMN_BOARD_NAME + " TEXT NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_NO + " TEXT NOT NULL, " +

                //Clean up null data before it enters the database and enter a string to signify it
                CatalogEntry.COLUMN_CATALOG_FILENAME + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_TIM + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_EXT + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_SUB + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_COM + " TEXT, " +
                CatalogEntry.COLUMN_CATALOG_REPLIES + " INTEGER NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_IMAGES + " INTEGER NOT NULL, " +
                CatalogEntry.COLUMN_CATALOG_STICKY + " INTEGER, " +
                CatalogEntry.COLUMN_CATALOG_LOCKED + " INTEGER, " +
                CatalogEntry.COLUMN_CATALOG_EMBED + " TEXT, " +

                //One post per board. No board should have two post 1234567 for example
                " UNIQUE (" + CatalogEntry.COLUMN_CATALOG_NO + ", " + CatalogEntry.COLUMN_BOARD_NAME +
                ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_THREAD_TABLE = "CREATE TABLE IF NOT EXISTS " + ThreadEntry.TABLE_NAME + " (" +
                ThreadEntry._ID + " INTEGER PRIMARY KEY, " +

                //Foreign Key for board name
                ThreadEntry.COLUMN_BOARD_NAME + " INTEGER NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_RESTO + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_NO + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_SUB + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_COM + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_EMAIL + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_NAME + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_TRIP + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_TIME + " TEXT NOT NULL, " +
                ThreadEntry.COLUMN_THREAD_LAST_MODIFIED + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_ID + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_EMBED + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_IMAGE_HEIGHT + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_IMAGE_WIDTH + " TEXT, " +
                ThreadEntry.COLUMN_THREAD_MEDIA_FILES + " BLOB, " +
                ThreadEntry.COLUMN_POSITION + " INTEGER, " +

                //I think this should auto overwrite dup posts if they ever come up
                " UNIQUE (" + ThreadEntry.COLUMN_THREAD_NO + ", " + ThreadEntry.COLUMN_BOARD_NAME +
                ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_REPLY_CHECK_TABLE = "CREATE TABLE IF NOT EXISTS " + ReplyCheck.TABLE_NAME + " (" +
                ReplyCheck._ID + " INTEGER PRIMARY KEY, " +

                //Foreign Key for board name
                ReplyCheck.COLUMN_BOARD_NAME + " INTEGER NOT NULL, " +
                ReplyCheck.COLUMN_THREAD_RESTO + " TEXT NOT NULL, " +
                ReplyCheck.COLUMN_THREAD_NO + " TEXT NOT NULL, " +
                ReplyCheck.COLUMN_THREAD_SUB + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_COM + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_EMAIL + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_NAME + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_TRIP + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_TIME + " TEXT NOT NULL, " +
                ReplyCheck.COLUMN_THREAD_LAST_MODIFIED + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_ID + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_EMBED + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_IMAGE_HEIGHT + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_IMAGE_WIDTH + " TEXT, " +
                ReplyCheck.COLUMN_THREAD_MEDIA_FILES + " BLOB, " +
                ReplyCheck.COLUMN_REPLY_CHECK_POSITION + " INTEGER, " +

                //I think this should auto overwrite dup posts if they ever come up
                " UNIQUE (" + ReplyCheck.COLUMN_THREAD_NO + ", " + ReplyCheck.COLUMN_BOARD_NAME +
                ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_USER_POSTS_TABLE = "CREATE TABLE IF NOT EXISTS " + UserPosts.TABLE_NAME + " (" +
                UserPosts._ID + " INTEGER PRIMARY KEY, " +

                UserPosts.COLUMN_BOARDS + " TEXT NOT NULL, " +
                UserPosts.COLUMN_NO + " TEXT NOT NULL, " +
                UserPosts.COLUMN_RESTO + " TEXT NOT NULL, " +
                UserPosts.COLUMN_SUBJECT + " TEXT NOT NULL, " +
                UserPosts.COLUMN_COMMENT + " TEXT NOT NULL, " +
                UserPosts.COLUMN_NUMBER_OF_REPLIES + " INTEGER NOT NULL, " +
                UserPosts.COLUMN_NEW_REPLY_FLAG + " INTEGER NOT NULL, " +
                UserPosts.COLUMN_ERROR_COUNT + " INTEGER NOT NULL, "  +
                UserPosts.COLUMN_POSITION + " INTEGER NOT NULL);";

        final String SQL_CREATE_WATCHLIST_TABLE = "CREATE TABLE IF NOT EXISTS " + WatchlistEntry.TABLE_NAME + " (" +
                WatchlistEntry._ID + " INTEGER PRIMARY KEY, " +

                WatchlistEntry.COLUMN_TITLE + " TEXT, " +
                WatchlistEntry.COLUMN_BOARD + " TEXT NOT NULL, " +
                WatchlistEntry.COLUMN_NO + " TEXT NOT NULL, " +
                WatchlistEntry.COLUMN_MEDIA_FILES + " BLOB, " +
                WatchlistEntry.WATCHLIST_ORDER + " INTEGER NOT NULL);";

        final String SQL_CREATE_THREAD_REPLY_COUNT_TABLE = "CREATE TABLE IF NOT EXISTS " + ThreadReplyCountTracker.TABLE_NAME + " (" +
                ThreadReplyCountTracker._ID + " INTEGER PRIMARY KEY, " +
                ThreadReplyCountTracker.BOARD_NAME + " TEXT NOT NULL, " +
                ThreadReplyCountTracker.RESTO + " TEXT NOT NULL, " +
                ThreadReplyCountTracker.REPLY_COUNT + " INTEGER NOT NULL);";

        Log.d(LOG_TAG, "SQL STRINGS");
        Log.d(LOG_TAG, SQL_CREATE_BOARD_TABLE);
        Log.d(LOG_TAG, SQL_CREATE_CATALOG_TABLE);
        Log.d(LOG_TAG, SQL_CREATE_THREAD_TABLE);
        db.execSQL(SQL_CREATE_BOARD_TABLE);
        db.execSQL(SQL_CREATE_CATALOG_TABLE);
        db.execSQL(SQL_CREATE_THREAD_TABLE);
        db.execSQL(SQL_CREATE_REPLY_CHECK_TABLE);
        db.execSQL(SQL_CREATE_USER_POSTS_TABLE);
        db.execSQL(SQL_CREATE_WATCHLIST_TABLE);
        db.execSQL(SQL_CREATE_THREAD_REPLY_COUNT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CatalogEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ThreadEntry.TABLE_NAME);

        if (oldVersion < 4){
            db.execSQL("DROP TABLE IF EXISTS " + BoardEntry.TABLE_NAME);
        }
        if (oldVersion >= 4 && oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + CatalogEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ThreadEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + UserPosts.TABLE_NAME);
        }

        onCreate(db);
    }
}
