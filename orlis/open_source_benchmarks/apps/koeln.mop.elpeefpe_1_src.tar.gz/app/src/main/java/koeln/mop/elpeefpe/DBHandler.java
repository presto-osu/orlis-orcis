package koeln.mop.elpeefpe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andreas Streichardt on 22.06.2016.
 */
public class DBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "elpeefpe";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "CREATE TABLE characters (id INTEGER PRIMARY KEY, name TEXT NOT NULL";

        String[] categories = {"elpe", "efpe"};
        String[] types = {"kanalisiert", "erschoepft", "verzehrt"};

        for (String category: categories) {
            createSql += ", " + category + " INTEGER NOT NULL DEFAULT 0";
            for (String type: types) {
                createSql += ", " + category + "_" + type + " INTEGER NOT NULL DEFAULT 0";
            }
        }
        createSql += ")";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
    }

    private Character hydrate(Cursor cursor) {
        Character c = new Character();
        c.id = cursor.getInt(cursor.getColumnIndex("id"));
        c.name = cursor.getString(cursor.getColumnIndex("name"));

        c.setValues(cursor.getInt(cursor.getColumnIndex("elpe")), cursor.getInt(cursor.getColumnIndex("efpe")));

        c.elpe.damage = new HashMap<>();
        c.elpe.damage.put(DamageType.KANALISIERT, cursor.getInt(cursor.getColumnIndex("elpe_kanalisiert")));
        c.elpe.damage.put(DamageType.ERSCHOEPFT, cursor.getInt(cursor.getColumnIndex("elpe_erschoepft")));
        c.elpe.damage.put(DamageType.VERZEHRT, cursor.getInt(cursor.getColumnIndex("elpe_verzehrt")));

        c.efpe.damage = new HashMap<>();
        c.efpe.damage.put(DamageType.KANALISIERT, cursor.getInt(cursor.getColumnIndex("efpe_kanalisiert")));
        c.efpe.damage.put(DamageType.ERSCHOEPFT, cursor.getInt(cursor.getColumnIndex("efpe_erschoepft")));
        c.efpe.damage.put(DamageType.VERZEHRT, cursor.getInt(cursor.getColumnIndex("efpe_verzehrt")));

        return c;
    }

    public Character save(Character c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", c.name);
        values.put("elpe", c.elpe.value);
        values.put("elpe_kanalisiert", c.elpe.damage.get(DamageType.KANALISIERT));
        values.put("elpe_erschoepft", c.elpe.damage.get(DamageType.ERSCHOEPFT));
        values.put("elpe_verzehrt", c.elpe.damage.get(DamageType.VERZEHRT));
        values.put("efpe", c.efpe.value);
        values.put("efpe_kanalisiert", c.efpe.damage.get(DamageType.KANALISIERT));
        values.put("efpe_erschoepft", c.efpe.damage.get(DamageType.ERSCHOEPFT));
        values.put("efpe_verzehrt", c.efpe.damage.get(DamageType.VERZEHRT));

        if (c.id != 0) {
            String[] args = new String[]{Integer.toString(c.id)};
            db.update("characters", values, "id=?", args);
        } else {
            c.id = (int)db.insert("characters", null, values);
        }
        db.close();
        return c;
    }

    public List<Character> getAll() {
        ArrayList<Character> list = new ArrayList<Character>();

        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM characters", null)) {
            while (cursor.moveToNext()) {
                list.add(hydrate(cursor));
            }
        }
        return list;
    }

    public Character find(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = new String[]{Integer.toString(id)};
        Cursor cursor = db.query("characters", null, "id=?", args, null, null, null, null);
        cursor.moveToNext();
        return hydrate(cursor);
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = new String[]{Integer.toString(id)};

        db.delete("characters", "id=?", args);
    }
}
