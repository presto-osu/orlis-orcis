package org.amoradi.syncopoli;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BackupSyncOpenHelper extends SQLiteOpenHelper {
    public BackupSyncOpenHelper(Context ctx) {
        super(ctx, BackupSyncSchema.DATABASE_NAME, null, BackupSyncSchema.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + BackupSyncSchema.TABLE_NAME + " (" +
                   BackupSyncSchema.COLUMN_TYPE         + " text, " +
                   BackupSyncSchema.COLUMN_NAME         + " text, " +
                   BackupSyncSchema.COLUMN_SOURCE       + " text, " +
                   BackupSyncSchema.COLUMN_DESTINATION  + " text, " +
                   BackupSyncSchema.COLUMN_LAST_UPDATE  + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table " + BackupSyncSchema.TABLE_NAME + ";");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
