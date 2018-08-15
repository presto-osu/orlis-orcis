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

package org.grapentin.apps.exceer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.grapentin.apps.exceer.training.TrainingManager;

import java.io.InputStream;

public class TrainingActivity extends Activity
{

  private static TrainingActivity instance;

  public static TrainingActivity getInstance ()
    {
      return instance;
    }

  @Override
  protected void onCreate (Bundle savedInstanceState)
    {
      instance = this;

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_training);

      TrainingManager.prepare();
    }

  @Override
  public void onBackPressed ()
    {
      DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick (DialogInterface dialog, int which)
          {
            switch (which)
              {
              case DialogInterface.BUTTON_POSITIVE:
                TrainingManager.reset();
                TrainingActivity.super.onBackPressed();
                break;
              case DialogInterface.BUTTON_NEGATIVE:
                break;
              }
          }
      };

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Are you sure you want to abort this session?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }

  public void onContextButtonClicked (View view)
    {
      if (TrainingManager.isRunning())
        TrainingManager.pause();
      else if (TrainingManager.isFinished())
        {
          TrainingManager.wrapUp();
          super.onBackPressed();
        }
      else
        TrainingManager.start();
    }

  public void onCurrentExerciseLevelLabelClicked (View view)
    {
      String url = (TrainingManager.getLeafExercisable() == null) ? null : TrainingManager.getLeafExercisable().getImage();
      if (url == null)
        return;

      Bitmap bitmap;
      try
        {
          InputStream in = new java.net.URL(url).openStream();
          bitmap = BitmapFactory.decodeStream(in);
        }
      catch (Exception e)
        {
          return;
        }

      ImageView imageView = new ImageView(this);
      imageView.setImageBitmap(bitmap);

      Toast toast = new Toast(getApplicationContext());
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.setView(imageView);
      toast.show();
    }

}
