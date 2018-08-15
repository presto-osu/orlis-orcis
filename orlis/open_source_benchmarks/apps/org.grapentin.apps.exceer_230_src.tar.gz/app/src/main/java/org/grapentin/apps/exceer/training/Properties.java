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

package org.grapentin.apps.exceer.training;

import android.widget.Toast;

import org.grapentin.apps.exceer.helpers.DurationString;
import org.grapentin.apps.exceer.managers.ContextManager;
import org.grapentin.apps.exceer.models.BaseModel;
import org.grapentin.apps.exceer.models.ModelProperty;

import java.io.Serializable;
import java.lang.reflect.Field;

public class Properties implements Serializable
{

  public long pause_after_set = 90000;
  public long pause_after_exercise = 90000;

  public long reps_duration_concentric = 2000;
  public long reps_duration_eccentric = 3000;
  public long reps_pause_after_concentric = 1000;
  public long reps_pause_after_eccentric = 0;

  public long duration = 0;
  public long duration_begin = 0;
  public long duration_finish = 0;
  public long duration_increment = 5000;

  public PrimaryMotion primary_motion = PrimaryMotion.concentric;
  public boolean two_sided = false;

  public Reps reps_begin = null;
  public Reps reps_finish = null;
  public long reps_increment = 0;
  public RepsIncrementDirection reps_increment_direction = RepsIncrementDirection.front_to_back;
  public RepsIncrementStyle reps_increment_style = RepsIncrementStyle.balanced;

  public String image = null;

  public Properties ()
    {

    }

  public Properties (Properties properties)
    {
      try
        {
          for (Field f : this.getClass().getFields())
            {
              f.set(this, f.get(properties));
            }
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public Properties (BaseModel.Relation properties)
    {
      for (BaseModel p : properties.all())
        set(((ModelProperty)p).key.get(), ((ModelProperty)p).value.get());
    }

  public Properties (Properties other, BaseModel.Relation properties)
    {
      this(other);

      for (BaseModel p : properties.all())
        set(((ModelProperty)p).key.get(), ((ModelProperty)p).value.get());
    }

  public void set (String key, String value)
    {
      switch (key)
        {
        case "pause_after_rep":
        case "pause_after_set":
        case "pause_after_exercise":
        case "reps_duration_concentric":
        case "reps_duration_eccentric":
        case "reps_pause_after_concentric":
        case "reps_pause_after_eccentric":
        case "duration":
        case "duration_begin":
        case "duration_finish":
        case "duration_increment":
          setLong(key, DurationString.parseLong(value));
          break;
        case "reps_increment":
          setLong(key, Long.parseLong(value));
          break;
        case "reps_begin":
        case "reps_finish":
          setObject(key, new Reps(value));
          break;
        case "image":
          setObject(key, value);
          break;
        case "two_sided":
          setBoolean(key, Boolean.parseBoolean(value));
          break;
        case "reps_increment_direction":
          try
            {
              setObject(key, RepsIncrementDirection.valueOf(value));
            }
          catch (Exception e)
            {
              Toast.makeText(ContextManager.get(), "invalid value for reps_increment_direction: '" + value + "'", Toast.LENGTH_LONG).show();
            }
          break;
        case "reps_increment_style":
          try
            {
              setObject(key, RepsIncrementStyle.valueOf(value));
            }
          catch (Exception e)
            {
              Toast.makeText(ContextManager.get(), "invalid value for reps_increment_style: '" + value + "'", Toast.LENGTH_LONG).show();
            }
          break;
        case "primary_motion":
          try
            {
              setObject(key, PrimaryMotion.valueOf(value));
            }
          catch (Exception e)
            {
              Toast.makeText(ContextManager.get(), "invalid value for primary_motion: '" + value + "'", Toast.LENGTH_LONG).show();
            }
          break;
        default:
          Toast.makeText(ContextManager.get(), "invalid property: '" + key + "'", Toast.LENGTH_LONG).show();
          break;
        }
    }

  private void setLong (String key, long value)
    {
      try
        {
          this.getClass().getDeclaredField(key).setLong(this, value);
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  private void setObject (String key, Object value)
    {
      try
        {
          this.getClass().getDeclaredField(key).set(this, value);
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  private void setBoolean (String key, boolean value)
    {
      try
        {
          this.getClass().getDeclaredField(key).setBoolean(this, value);
        }
      catch (Exception e)
        {
          throw new Error(e);
        }
    }

  public enum RepsIncrementDirection
  {
    front_to_back,
    back_to_front
  }

  public enum RepsIncrementStyle
  {
    balanced,
    fill_sets
  }

  public enum PrimaryMotion
  {
    concentric,
    eccentric
  }
}
