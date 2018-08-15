/*
 * Oburo.O est un programme destinée à saisir son temps de travail sur un support Android.
 *
 *     This file is part of Oburo.O
 *     Oburo.O is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package fr.s3i.pointeuse.persistance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "POINTAGE";
    private static final int DATABASE_VERSION = 4;

    public static final String ID = "_ID";
    public static final String DATE_DEBUT = "DATE_DEBUT";
    public static final String DATE_FIN = "DATE_FIN";
    public static final String COMMENTAIRE = "COMMENTAIRE";

    public Context context;

    public DatabaseHelper(Context leContext) {
        super(leContext, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = leContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //android.util.Log.w("Constants", "onCreate");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);

        db.execSQL("CREATE TABLE " + DATABASE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DATE_DEBUT + " text," +
                DATE_FIN + " text," +
                COMMENTAIRE + " text)");

        db.execSQL("DROP INDEX IF EXISTS INDEX_" + DATABASE_NAME);
        String requete = "CREATE INDEX INDEX_" + DATABASE_NAME +
                " ON " + DATABASE_NAME + "(" + DATE_DEBUT + ")";

        db.execSQL(requete);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //android.util.Log.w("Constants", "Maj de la base");
        if (oldVersion == 3 && newVersion == 4) {
            db.execSQL("DROP TABLE TABLE_PAUSE");
        }
    }

    //---opens the database---
    public SQLiteDatabase open() throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase();
        purge_pointage_vide(db);
        return db;
    }

    //---closes the database---
    public void close(SQLiteDatabase db) {
        db.close();
    }

    /*****************************************/
    /*********** TABLE POINTAGE **************/
    /*****************************************/

    //---updates a title---
    public boolean updateEnregistrementPointage(SQLiteDatabase db, long rowId, String colonne, String valeur) {
        ContentValues args = new ContentValues();
        args.put(colonne, valeur);
        return db.update(DATABASE_NAME, args,
                ID + "=" + rowId, null) > 0;
    }

    //---deletes a particular title---
    public boolean purge_pointage_vide(SQLiteDatabase db) {
        db.delete(DATABASE_NAME, DATE_DEBUT + "=''", null);
        return db.delete(DATABASE_NAME, DATE_DEBUT + " IS NULL", null) > 0;
    }

    //---deletes a particular title---
    public boolean deleteEnregistrementPointage(SQLiteDatabase db, long rowId) {
        return db.delete(DATABASE_NAME, ID + "=" + rowId, null) > 0;
    }

    //---retrieves all the titles---
    public Cursor getAllPointage(SQLiteDatabase db) {
        return db.query(DATABASE_NAME, new String[]{
                        ID,
                        DATE_DEBUT,
                        DATE_FIN,
                        COMMENTAIRE},
                null,
                null,
                null,
                null,
                null);
    }

    //---retrieves all the titles---
    public Cursor getSomeDatePointage(SQLiteDatabase db, String conditions) {
        return db.query(true, DATABASE_NAME, new String[]{
                        ID,
                        DATE_DEBUT,
                        DATE_FIN,
                        COMMENTAIRE},
                conditions,
                null,
                null,
                null,
                " DATE_DEBUT ",
                null);
    }

    //---retrieves a particular title---
    public Cursor selectDatePointage(SQLiteDatabase db, long rowId) throws SQLException {
        Cursor mCursor =
                db.query(true, DATABASE_NAME, new String[]{
                                ID,
                                DATE_DEBUT,
                                DATE_FIN,
                                COMMENTAIRE},
                        ID + "=" + rowId,
                        null,
                        null,
                        null,
                        null,
                        null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getLastEnregistrementPointage(SQLiteDatabase db) throws SQLException {
        Cursor mCursor =
                db.query(true, DATABASE_NAME, new String[]{
                                ID,
                                DATE_DEBUT,
                                DATE_FIN,
                                COMMENTAIRE},
                        null,
                        null,
                        null,
                        null,
                        ID + " DESC",
                        "1");

        if (mCursor != null) {
            mCursor.moveToFirst();
            return mCursor;
        } else {
            return null;
        }

    }

    //---insert a title into the database---
    public long insereNouveauPointage(SQLiteDatabase db, String date_debut, String date_fin) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(DATE_DEBUT, date_debut);
        initialValues.put(DATE_FIN, date_fin);
        initialValues.put(COMMENTAIRE, "");
        return db.insert(DATABASE_NAME, null, initialValues);
    }

    public long insereNouveauPointage(SQLiteDatabase db, String date_debut, String date_fin, String commentaire) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(DATE_DEBUT, date_debut);
        initialValues.put(DATE_FIN, date_fin);
        initialValues.put(COMMENTAIRE, commentaire);
        return db.insert(DATABASE_NAME, null, initialValues);
    }

}
