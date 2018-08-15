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

import org.grapentin.apps.exceer.managers.DatabaseManager;
import org.grapentin.apps.exceer.models.BaseExercisable;
import org.grapentin.apps.exceer.models.ModelSession;
import org.grapentin.apps.exceer.models.ModelTraining;

import java.io.Serializable;

public class TrainingManager implements Serializable
{

  private static TrainingManager instance = null;

  private ModelTraining currentTraining = null;
  private int currentTrainingId;

  private TrainingManager ()
    {

    }

  public static TrainingManager getInstance ()
    {
      if (instance == null)
        instance = new TrainingManager();
      return instance;
    }

  public static void init ()
    {
      getInstance();
    }

  public static BaseExercisable getLeafExercisable ()
    {
      return getInstance().currentTraining.getLeafExercisable();
    }

  public static boolean isRunning ()
    {
      return getInstance().currentTraining.isRunning();
    }

  public static boolean isFinished ()
    {
      return getInstance().currentTraining.isFinished();
    }

  public static void prepare ()
    {
      if (getInstance().currentTraining != null)
        return;

      // TODO: get currentTrainingId from settings
      getInstance().currentTrainingId = 1;
      getInstance().currentTraining = ModelTraining.get(getInstance().currentTrainingId);
      getInstance().currentTraining.prepare();
    }

  public static void next ()
    {
      getInstance().currentTraining.next();
    }

  public static void reset ()
    {
      getInstance().currentTraining.reset();
      getInstance().currentTraining = null;
    }

  public static void start ()
    {
      getInstance().currentTraining.start();
    }

  public static void pause ()
    {
      getInstance().currentTraining.pause();
    }

  public static void wrapUp ()
    {
      getInstance().currentTraining.wrapUp();
      DatabaseManager.add(new ModelSession(getInstance().currentTraining.getId()));
      getInstance().currentTraining = null;
    }

}
