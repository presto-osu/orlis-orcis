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

import android.os.Handler;

public class TaskManager
{

  private static TaskManager instance = null;

  private Handler handler;

  private TaskManager ()
    {
      handler = new Handler();
    }

  private static TaskManager getInstance ()
    {
      if (instance == null)
        instance = new TaskManager();
      return instance;
    }

  public static void init ()
    {
      getInstance();
    }

  abstract static public class TimerTask implements Runnable
  {
    public void run ()
      {
        long next = update();

        if (next > 0)
          {
            next -= System.currentTimeMillis();
            getInstance().handler.postDelayed(this, next);
          }
      }

    public void start ()
      {
        getInstance().handler.removeCallbacks(this);
        getInstance().handler.post(this);
      }

    public void stop ()
      {
        getInstance().handler.removeCallbacks(this);
      }

    public void pause ()
      {
        stop();
      }

    abstract public long update ();
  }

}
