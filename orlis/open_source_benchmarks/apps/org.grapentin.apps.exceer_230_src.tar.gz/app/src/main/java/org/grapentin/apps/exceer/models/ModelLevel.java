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
import org.grapentin.apps.exceer.managers.ContextManager;
import org.grapentin.apps.exceer.training.Properties;

public class ModelLevel extends BaseExercisable
{

  @SuppressWarnings("unused") // accessed by reflection from BaseModel
  protected final static String TABLE_NAME = "levels";

  // database layout
  public Column name = new Column("name");
  public Column progress = new Column("progress");
  public Relation properties = makeRelation("properties", ModelProperty.class);
  public Backref exercise = makeBackref("exercise", ModelExercise.class);

  public static ModelLevel fromXml (XmlNode root)
    {
      ModelLevel m = new ModelLevel();

      m.name.set(root.getAttribute("name"));

      for (XmlNode property : root.getChildren("property"))
        m.properties.add(ModelProperty.fromXml(property));

      return m;
    }

  public static ModelLevel get (long id)
    {
      return (ModelLevel)BaseModel.get(ModelLevel.class, id);
    }

  public BaseExercisable getLeafExercisable ()
    {
      return this;
    }

  public void prepare (Properties p)
    {
      props = new Properties(p, properties);

      super.prepare();
    }

  public void show ()
    {
      TextView currentExerciseLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLabel);
      TextView currentExerciseLevelLabel1 = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLevelLabel1);
      TextView currentExerciseLevelLabel2 = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityCurrentExerciseLevelLabel2);

      currentExerciseLabel.setText(((ModelExercise)exercise.get()).name.get());
      currentExerciseLevelLabel1.setText(ContextManager.get().getString(R.string.TrainingActivityCurrentExerciseLevelInt) + ((ModelExercise)exercise.get()).getCurrentLevelId());
      currentExerciseLevelLabel2.setText(name.get());

      super.show();
    }

  public void reset ()
    {

    }

  public void levelUp ()
    {
      ((ModelExercise)exercise.get()).levelUp();
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
      commit();
    }

}
