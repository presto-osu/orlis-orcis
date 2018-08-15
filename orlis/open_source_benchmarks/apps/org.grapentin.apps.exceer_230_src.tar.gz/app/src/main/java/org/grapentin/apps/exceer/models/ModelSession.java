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

import android.database.Cursor;

import org.grapentin.apps.exceer.managers.DatabaseManager;

public class ModelSession extends BaseModel
{

  @SuppressWarnings("unused") // accessed by reflection from BaseModel
  protected final static String TABLE_NAME = "sessions";

  // database layout
  public Column date = new Column("date", TYPE_LONG);
  public Column training_id = new Column("training_id", TYPE_LONG);

  public ModelSession (long training_id)
    {
      this.date.set(System.currentTimeMillis());
      this.training_id.set(training_id);
    }

  public ModelSession ()
    {

    }

  public static ModelSession get (long id)
    {
      return (ModelSession)BaseModel.get(ModelSession.class, id);
    }

  public static ModelSession getLast ()
    {
      ModelSession out = null;

      ModelSession tmp = new ModelSession();
      try
        {
          Cursor c = DatabaseManager.getSession().query(TABLE_NAME, new String[]{ tmp._ID.name }, null, null, null, null, tmp.date.name + " DESC", "1");
          if (c.getCount() == 1)
            {
              c.moveToFirst();
              out = get(c.getLong(c.getColumnIndex(tmp._ID.name)));
            }
          c.close();
        }
      catch (Exception e)
        {
          // nothing here
        }

      return out;
    }

}
