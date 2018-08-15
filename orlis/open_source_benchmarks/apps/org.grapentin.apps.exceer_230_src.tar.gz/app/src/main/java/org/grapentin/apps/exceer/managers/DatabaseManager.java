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

package org.grapentin.apps.exceer.managers;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.grapentin.apps.exceer.MainActivity;
import org.grapentin.apps.exceer.R;
import org.grapentin.apps.exceer.helpers.Reflection;
import org.grapentin.apps.exceer.helpers.XmlNode;
import org.grapentin.apps.exceer.models.BaseModel;
import org.grapentin.apps.exceer.models.ModelTraining;

import java.util.HashMap;

public class DatabaseManager extends SQLiteOpenHelper
{

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "TrainingStorage.db";
  private static final Revision revisions[] = new Revision[]{
      new Revision("", "")
  };
  private static HashMap<Class, HashMap<Long, BaseModel>> cache = new HashMap<>();
  private static DatabaseManager instance = null;

  private DatabaseManager ()
    {
      super(ContextManager.get(), DATABASE_NAME, null, DATABASE_VERSION);
    }

  private static DatabaseManager getInstance ()
    {
      if (instance == null)
        instance = new DatabaseManager();
      return instance;
    }

  public static void init ()
    {
      getInstance();

      // TODO: remove this, once database schema is stable
      //for (Class model : Reflection.getSubclassesOf(BaseModel.class))
      //  BaseModel.onDrop(model);
      //getInstance().onCreate(getSession());
    }

  public static SQLiteDatabase getSession ()
    {
      return getInstance().getWritableDatabase();
    }

  public static void add (BaseModel b)
    {
      b.onInsert();
    }

  public static void addToCache (BaseModel b)
    {
      if (!cache.containsKey(b.getClass()))
        cache.put(b.getClass(), new HashMap<Long, BaseModel>());
      cache.get(b.getClass()).put(b.getId(), b);
    }

  public static BaseModel getFromCache (Class c, long id)
    {
      if (!cache.containsKey(c))
        return null;
      if (!cache.get(c).containsKey(id))
        return null;
      return cache.get(c).get(id);
    }

  private static void importDefaults ()
    {
      XmlNode root;
      try
        {
          root = new XmlNode(ContextManager.get().getResources().getXml(R.xml.trainings_default));
        }
      catch (Exception e)
        {
          throw new Error(e);
        }

      for (XmlNode n : root.getChildren("training"))
        add(ModelTraining.fromXml(n));
    }

  public void onCreate (SQLiteDatabase db)
    {
      final ProgressDialog progress = new ProgressDialog(MainActivity.getInstance());
      progress.setTitle("Updating Database");
      progress.setMessage("Please wait while the database is updated...");
      progress.show();

      Runnable runnable = new Runnable()
      {
        @Override
        public void run ()
          {
            for (Class model : Reflection.getSubclassesOf(BaseModel.class))
              BaseModel.onCreate(model);

            importDefaults();

            progress.dismiss();
          }
      };
      new Thread(runnable).start();
    }

  public void onUpgrade (final SQLiteDatabase db, final int oldVersion, final int newVersion)
    {
      final ProgressDialog progress = new ProgressDialog(MainActivity.getInstance());
      progress.setTitle("Updating Database");
      progress.setMessage("Please wait while the database is updated...");
      progress.show();

      Runnable runnable = new Runnable()
      {
        @Override
        public void run ()
          {
            for (int i = oldVersion + 1; i <= newVersion; ++i)
              revisions[i - 2].runUpgrade(db);

            progress.dismiss();
          }
      };
      new Thread(runnable).start();
    }

  public void onDowngrade (final SQLiteDatabase db, final int oldVersion, final int newVersion)
    {
      final ProgressDialog progress = new ProgressDialog(MainActivity.getInstance());
      progress.setTitle("Updating Database");
      progress.setMessage("Please wait while the database is updated...");
      progress.show();

      Runnable runnable = new Runnable()
      {
        @Override
        public void run ()
          {
            for (int i = oldVersion; i > newVersion; --i)
              revisions[i - 2].runDowngrade(db);

            progress.dismiss();
          }
      };
      new Thread(runnable).start();
    }

  private static class Revision
  {
    private String upgradeSql;
    private String downgradeSql;

    public Revision (String upgradeSql, String downgradeSql)
      {
        this.upgradeSql = upgradeSql;
        this.downgradeSql = downgradeSql;
      }

    public void runUpgrade (SQLiteDatabase db)
      {
        db.execSQL(this.upgradeSql);
      }

    public void runDowngrade (SQLiteDatabase db)
      {
        db.execSQL(this.downgradeSql);
      }
  }

}
