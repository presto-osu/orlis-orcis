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


import android.widget.TextView;

import org.grapentin.apps.exceer.R;
import org.grapentin.apps.exceer.TrainingActivity;
import org.grapentin.apps.exceer.helpers.XmlNode;
import org.grapentin.apps.exceer.training.Properties;

public class ModelExercise extends BaseExercisable
{

  @SuppressWarnings("unused") // accessed by reflection from BaseModel
  protected final static String TABLE_NAME = "exercises";

  // database layout
  protected Column name = new Column("name");
  protected Column currentExerciseId = new Column("currentExerciseId", TYPE_INT);
  protected Column currentLevelId = new Column("currentLevelId", TYPE_INT);
  protected Column progress = new Column("progress");
  protected Relation levels = makeRelation("levels", ModelLevel.class);
  protected Relation exercises = makeRelation("exercises", ModelExercise.class);
  protected Relation properties = makeRelation("properties", ModelProperty.class);

  public static ModelExercise fromXml (XmlNode root)
    {
      ModelExercise m = new ModelExercise();

      m.name.set(root.getAttribute("name"));
      m.currentExerciseId.set(0);
      m.currentLevelId.set(0);

      for (XmlNode property : root.getChildren("property"))
        m.properties.add(ModelProperty.fromXml(property));
      for (XmlNode exercise : root.getChildren("exercise"))
        m.exercises.add(ModelExercise.fromXml(exercise));
      for (XmlNode level : root.getChildren("level"))
        m.levels.add(ModelLevel.fromXml(level));

      return m;
    }

  public static ModelExercise get (long id)
    {
      return (ModelExercise)BaseModel.get(ModelExercise.class, id);
    }

  @Override
  public BaseExercisable getLeafExercisable ()
    {
      if (getCurrentExercise() != null)
        return getCurrentExercise().getLeafExercisable();
      if (getCurrentLevel() != null)
        return getCurrentLevel().getLeafExercisable();
      return this;
    }

  public ModelExercise getCurrentExercise ()
    {
      return (ModelExercise)exercises.at(currentExerciseId.getInt());
    }

  public ModelLevel getCurrentLevel ()
    {
      return (ModelLevel)levels.at(currentLevelId.getInt());
    }

  public int getCurrentLevelId ()
    {
      return currentLevelId.getInt() + 1;
    }

  public void prepare (Properties p)
    {
      props = new Properties(p, properties);

      if (getCurrentExercise() != null)
        getCurrentExercise().prepare(props);
      else if (getCurrentLevel() != null)
        getCurrentLevel().prepare(props);
      else
        super.prepare();
    }

  public void show ()
    {
      TextView currentExerciseLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLabel);
      TextView currentExerciseLevelLabel1 = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLevelLabel1);
      TextView currentExerciseLevelLabel2 = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLevelLabel2);

      currentExerciseLabel.setText(name.get());
      currentExerciseLevelLabel1.setText("");
      currentExerciseLevelLabel2.setText("");

      super.show();
    }

  public void reset ()
    {
      if (getCurrentExercise() != null)
        getCurrentExercise().reset();
      else if (getCurrentLevel() != null)
        getCurrentLevel().reset();
    }

  public void levelUp ()
    {
      if (levels.at(currentLevelId.getInt() + 1) != null)
        {
          currentLevelId.set(currentLevelId.getInt() + 1);
          progress.set(null);
        }
    }

  public String getCurrentProgress ()
    {
      return progress.get();
    }

  public void setCurrentProgress (String s)
    {
      progress.set(s);
    }

  public void wrapUp ()
    {
      if (!exercises.isEmpty())
        currentExerciseId.set((currentExerciseId.getInt() + 1) % exercises.size());

      for (BaseModel e : exercises.all())
        ((ModelExercise)e).wrapUp();
      for (BaseModel l : levels.all())
        ((ModelLevel)l).wrapUp();

      commit();
    }

}

