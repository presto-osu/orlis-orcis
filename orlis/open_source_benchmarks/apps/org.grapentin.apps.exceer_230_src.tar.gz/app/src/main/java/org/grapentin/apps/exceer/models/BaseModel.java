/******************************************************************************
 *    This file is part of Exceer                                             *
 *                                                                            *
 *    Copyright (C) 2015  Andreas Grapentin                                   *
 *                                                                            *
 *    This program is free software: you can redistribute it and/or modify    *
 *    it under the terms of the GNU General Public License as published by    *
 *    the Free Software Foundation, either version 3 of the License, or       *
 *    (at your option) any later version.                                     *
 *                                                                            *
 *    This program is distributed in the hope that it will be useful,         *
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 *    GNU General Public License for more details.                            *
 *                                                                            *
 *    You should have received a copy of the GNU General Public License       *
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.   *
 ******************************************************************************/

package org.grapentin.apps.exceer.models;

import android.content.ContentValues;
import android.database.Cursor;

import org.grapentin.apps.exceer.helpers.Reflection;
import org.grapentin.apps.exceer.managers.DatabaseManager;

import java.util.ArrayList;

public abstract class BaseModel
{

  protected static final String TYPE_TEXT = "TEXT";
  protected static final String TYPE_INT = "INTEGER";
  protected static final String TYPE_LONG = "INTEGER";

  protected Column _ID = new Column("id", TYPE_INT, "PRIMARY KEY");

  public BaseModel ()
    {

    }

  public static String getTableName (Class model)
    {
      try
        {
          return (String)model.getDeclaredField("TABLE_NAME").get(null);
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public static void onCreate (Class model)
    {
      if (getTableName(model) == null)
        return;

      String columns = "";
      for (Object o : Reflection.getDeclaredFieldsOfType(model, Column.class))
        columns += (columns.equals("") ? "" : ", ") + ((Column)o).name + " " + ((Column)o).type + (((Column)o).params.equals("") ? "" : " " + ((Column)o).params);

      String query = "CREATE TABLE " + getTableName(model) + " (" + columns + ")";
      DatabaseManager.getSession().execSQL(query);

      for (Object o : Reflection.getDeclaredFieldsOfType(model, Relation.class))
        ((Relation)o).onCreate();
    }

  public static void onDrop (Class model)
    {
      if (getTableName(model) == null)
        return;

      String query = "DROP TABLE IF EXISTS " + getTableName(model);
      DatabaseManager.getSession().execSQL(query);

      for (Object o : Reflection.getDeclaredFieldsOfType(model, Relation.class))
        ((Relation)o).onDrop();
    }

  public static BaseModel get (Class model, long id)
    {
      BaseModel m = DatabaseManager.getFromCache(model, id);
      if (m != null)
        return m;

      try
        {
          m = (BaseModel)model.newInstance();

          Cursor c = DatabaseManager.getSession().query(getTableName(model), null, m._ID.name + "=" + id, null, null, null, null);
          if (c.getCount() == 1)
            {
              m._ID.value = Long.toString(id);
              c.moveToFirst();
              for (Object o : Reflection.getDeclaredFieldsOfType(model, Column.class, m))
                ((Column)o).value = c.getString(c.getColumnIndex(((Column)o).name));
            }
          c.close();

          DatabaseManager.addToCache(m);

          return m;
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public static ArrayList<Long> getAllIds (Class model)
    {
      ArrayList<Long> out = new ArrayList<>();

      try
        {
          BaseModel m = (BaseModel)model.newInstance();

          Cursor c = DatabaseManager.getSession().query(getTableName(model), new String[]{ m._ID.name }, null, null, null, null, null);
          c.moveToFirst();
          while (!c.isAfterLast())
            {
              out.add(c.getLong(c.getColumnIndex(m._ID.name)));
              c.moveToNext();
            }
          c.close();

          return out;
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public void onInsert ()
    {
      if (_ID.value != null)
        return;

      ContentValues values = new ContentValues();
      for (Object o : Reflection.getDeclaredFieldsOfType(getClass(), Column.class, this))
        if (o != _ID)
          values.put(((Column)o).name, ((Column)o).value);

      long id = DatabaseManager.getSession().insert(getTableName(this.getClass()), null, values);
      _ID.set(id);

      DatabaseManager.addToCache(this);

      for (Object o : Reflection.getDeclaredFieldsOfType(getClass(), Relation.class, this))
        ((Relation)o).onInsert();
    }

  public void commit ()
    {
      if (_ID.value == null)
        {
          onInsert();
          return;
        }

      ContentValues values = new ContentValues();
      for (Object o : Reflection.getDeclaredFieldsOfType(getClass(), Column.class, this))
        if (o != _ID)
          values.put(((Column)o).name, ((Column)o).value);

      DatabaseManager.getSession().update(getTableName(this.getClass()), values, _ID.name + "=" + _ID.get(), null);
    }

  public Relation makeRelation (String name, Class other)
    {
      return new Relation(this, name, other);
    }

  public Backref makeBackref (String name, Class other)
    {
      return new Backref(this, name, other);
    }

  public long getId ()
    {
      return _ID.getLong();
    }

  public static class Column
  {
    public String name;
    public String type;
    public String params;
    private String value = null;

    public Column (String name)
      {
        this(name, TYPE_TEXT);
      }

    public Column (String name, String type)
      {
        this(name, type, "");
      }

    public Column (String name, String type, String params)
      {
        this.name = name;
        this.type = type;
        this.params = params;
      }

    public String get ()
      {
        return this.value;
      }

    public long getLong ()
      {
        return Long.parseLong(this.value);
      }

    public int getInt ()
      {
        return Integer.parseInt(this.value);
      }

    public void set (String value)
      {
        this.value = value;
      }

    public void set (long value)
      {
        set(Long.toString(value));
      }

  }

  public static class Relation
  {
    public String name;
    public Class other;

    public BaseModel left = null;
    public ArrayList<BaseModel> right = null;

    public Relation (BaseModel left, String name, Class other)
      {
        this.left = left;
        this.name = name;
        this.other = other;
      }

    private String getRelationTableName ()
      {
        return "orm_" + getTableName(left.getClass()) + "_" + getTableName(other);
      }

    public void onCreate ()
      {
        String query = "CREATE TABLE " + getRelationTableName() + " (" + "left_id " + TYPE_INT + ", right_id " + TYPE_INT + ", PRIMARY KEY (left_id, right_id))";
        DatabaseManager.getSession().execSQL(query);
      }

    public void onDrop ()
      {
        String query = "DROP TABLE IF EXISTS " + getRelationTableName();
        DatabaseManager.getSession().execSQL(query);
      }

    public void onInsert ()
      {
        if (right == null)
          return;

        for (BaseModel m : right)
          {
            m.onInsert();
            ContentValues values = new ContentValues();
            values.put("left_id", left._ID.value);
            values.put("right_id", m._ID.value);
            DatabaseManager.getSession().insert(getRelationTableName(), null, values);
          }
      }

    public void add (BaseModel m)
      {
        if (left._ID.value != null)
          {
            getRight().add(m);

            m.onInsert();
            ContentValues values = new ContentValues();
            values.put("left_id", left._ID.value);
            values.put("right_id", m._ID.value);
            DatabaseManager.getSession().insert(getRelationTableName(), null, values);
          }
        else
          {
            if (right == null)
              right = new ArrayList<>();
            right.add(m);
          }
      }

    public ArrayList<BaseModel> getRight ()
      {
        if (right != null)
          return right;

        right = new ArrayList<>();

        Cursor c = DatabaseManager.getSession().query(getRelationTableName(), new String[]{
            "right_id"
        }, "left_id=" + left._ID.value, null, null, null, null);

        c.moveToFirst();
        while (!c.isAfterLast())
          {
            right.add(BaseModel.get(other, c.getLong(c.getColumnIndex("right_id"))));
            c.moveToNext();
          }

        c.close();
        return right;
      }

    public BaseModel at (int id)
      {
        return (id >= getRight().size() ? null : getRight().get(id));
      }

    public ArrayList<BaseModel> all ()
      {
        return getRight();
      }

    public boolean isEmpty ()
      {
        return getRight().isEmpty();
      }

    public int size ()
      {
        return getRight().size();
      }
  }

  public static class Backref
  {
    public String name;
    public Class other;

    public BaseModel left = null;
    public BaseModel right = null;

    public Backref (BaseModel right, String name, Class other)
      {
        this.right = right;
        this.name = name;
        this.other = other;
      }

    private String getRelationTableName ()
      {
        return "orm_" + getTableName(other) + "_" + getTableName(right.getClass());
      }

    public BaseModel get ()
      {
        if (left != null)
          return left;

        Cursor c = DatabaseManager.getSession().query(getRelationTableName(), new String[]{
            "left_id"
        }, "right_id=" + right._ID.value, null, null, null, null);

        c.moveToFirst();
        if (!c.isAfterLast())
          left = BaseModel.get(other, c.getLong(c.getColumnIndex("left_id")));

        c.close();
        return left;
      }

  }

}
