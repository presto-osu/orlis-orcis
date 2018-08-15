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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.TextView;

import org.grapentin.apps.exceer.R;
import org.grapentin.apps.exceer.TrainingActivity;
import org.grapentin.apps.exceer.managers.ContextManager;
import org.grapentin.apps.exceer.managers.SoundManager;
import org.grapentin.apps.exceer.managers.TaskManager;
import org.grapentin.apps.exceer.training.Properties;
import org.grapentin.apps.exceer.training.Reps;
import org.grapentin.apps.exceer.training.TrainingManager;

abstract public class BaseExercisable extends BaseModel
{

  @SuppressWarnings("unused") // accessed by reflection from BaseModel
  protected static final String TABLE_NAME = null;

  protected Properties props = new Properties();

  private TaskManager.TimerTask task = null;

  private boolean running = false;

  protected BaseExercisable ()
    {

    }

  abstract public BaseExercisable getLeafExercisable ();

  abstract public void levelUp ();

  abstract public String getCurrentProgress ();

  abstract public void setCurrentProgress (String progress);

  public boolean isRunning ()
    {
      return running;
    }

  public String getImage ()
    {
      return props.image;
    }

  public void prepare ()
    {
      if (props.reps_begin != null)
        {
          String progress = getCurrentProgress();
          Reps reps = ((progress == null) ? new Reps(props.reps_begin) : new Reps(progress));
          setCurrentProgress(reps.toString());

          task = new RepsTask(reps);
        }
      else if (props.duration_begin > 0)
        {
          String progress = getCurrentProgress();
          long duration = ((progress == null) ? props.duration_begin : Long.parseLong(progress));
          setCurrentProgress(Long.toString(duration));

          task = new DurationTask(duration);
        }
      else
        {
          task = new DurationTask(props.duration);
        }
    }

  public void show ()
    {
      if (props.reps_begin != null)
        {
          Reps reps = new Reps(getCurrentProgress());

          TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
          progressLabel.setText("1:0/" + reps.sets.get(0));
        }
      else if (props.duration_begin > 0)
        {
          long duration = Long.parseLong(getCurrentProgress());

          long min = duration / 60000;
          long sec = (duration % 60000) / 1000;

          TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
          progressLabel.setText((min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
        }
      else
        {
          long min = props.duration / 60000;
          long sec = (props.duration % 60000) / 1000;

          TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
          progressLabel.setText((min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);
        }
    }

  public void start ()
    {
      task.start();
      Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
      contextButton.setText(ContextManager.get().getString(R.string.TrainingActivityContextButtonTextPause));
      running = true;
    }

  public void pause ()
    {
      if (task != null)
        task.pause();
      Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
      contextButton.setText(ContextManager.get().getString(R.string.TrainingActivityContextButtonTextStart));
      running = false;
    }

  public void finishExercise ()
    {
      running = false;

      if (props.reps_begin != null)
        {
          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick (DialogInterface dialog, int which)
              {
                switch (which)
                  {
                  case DialogInterface.BUTTON_POSITIVE:
                    increment();
                    break;
                  case DialogInterface.BUTTON_NEGATIVE:
                    break;
                  }
              }
          };

          AlertDialog.Builder builder = new AlertDialog.Builder(TrainingActivity.getInstance());
          builder.setMessage("Did you make it?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
      else if (props.duration_begin > 0)
        {
          TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
          progressLabel.setText("00:00");

          DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick (DialogInterface dialog, int which)
              {
                switch (which)
                  {
                  case DialogInterface.BUTTON_POSITIVE:
                    increment();
                    break;
                  case DialogInterface.BUTTON_NEGATIVE:
                    break;
                  }
              }
          };

          AlertDialog.Builder builder = new AlertDialog.Builder(TrainingActivity.getInstance());
          builder.setMessage("Did you make it?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
        }
      else
        {
          TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
          progressLabel.setText("00:00");
        }

      if (props.pause_after_exercise > 0)
        {
          task = new PauseTask(props.pause_after_exercise);
          task.start();
        }
      else
        afterPause();
    }

  private void afterPause ()
    {
      Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
      contextButton.setEnabled(true);
      contextButton.setText(ContextManager.get().getString(R.string.TrainingActivityContextButtonTextStart));

      TrainingManager.next();
    }

  private void increment ()
    {
      if (props.reps_begin != null)
        {
          Reps reps = new Reps(getCurrentProgress());

          if (reps.greaterOrEqual(props.reps_finish))
            {
              levelUp();
              return;
            }

          for (int i = 0; i < props.reps_increment; ++i)
            reps.increment(props);

          setCurrentProgress(reps.toString());
        }
      else if (props.duration_begin > 0)
        {
          long duration = Long.parseLong(getCurrentProgress());

          if (duration >= props.duration_finish)
            {
              levelUp();
              return;
            }

          duration += props.duration_increment;
          setCurrentProgress(Long.toString(duration));
        }
    }

  private class DurationTask extends TaskManager.TimerTask
  {
    private long duration;

    private long start = 0;
    private long paused = 0;
    private long countdown = 3;
    private boolean halftime = false;

    public DurationTask (long duration)
      {
        this.duration = duration;
      }

    public void pause ()
      {
        if (start > 0)
          paused = System.currentTimeMillis();
        else
          countdown = 3;

        super.stop();
      }

    public long update ()
      {
        if (start == 0 && countdown > 0)
          {
            SoundManager.play(R.raw.beep_low);
            countdown--;
            return System.currentTimeMillis() + 1000;
          }

        if (start == 0)
          {
            SoundManager.play(R.raw.beep_four);
            start = System.currentTimeMillis();
            paused = 0;
            countdown = 3;
          }

        if (paused > 0)
          {
            start += System.currentTimeMillis() - paused;
            paused = 0;
          }

        long elapsed = System.currentTimeMillis() - start;
        long remaining = Math.round((duration - elapsed) / 1000.0);

        if (props.two_sided && !halftime && elapsed >= 0.5 * duration)
          {
            SoundManager.play(R.raw.beep_two);
            halftime = true;
          }

        if (countdown > 0 && countdown >= remaining)
          {
            SoundManager.play(R.raw.beep_low);
            countdown--;
          }

        if (remaining <= 0)
          {
            SoundManager.play(R.raw.beep_four);
            finishExercise();
            return 0;
          }

        long min = remaining / 60;
        long sec = remaining % 60;

        TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
        progressLabel.setText((min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);

        return start + (Math.round(elapsed / 1000.0) + 1) * 1000;
      }
  }

  private class RepsTask extends TaskManager.TimerTask
  {
    private Reps reps;
    private Reps done;

    private int currentSet = 0;
    private int phase = 0;

    private long pause_start = 0;
    private long pause_duration = 0;
    private long pause_countdown = 0;

    private long countdown = 3;
    private int next_sound = 0;

    public RepsTask (Reps reps)
      {
        this.reps = reps;
        this.done = reps.empty();
      }

    public void pause ()
      {
        countdown = 3;
        phase = 0;

        super.pause();
      }

    public long update ()
      {
        if (countdown > 0)
          {
            SoundManager.play(R.raw.beep_low);
            countdown--;
            next_sound = R.raw.beep_four;
            return System.currentTimeMillis() + 1000;
          }

        if (pause_duration > 0)
          return updateSetPause();

        phase = (phase + 1) % 5;

        // phase 0: pause
        if (phase == 0)
          {
            done.sets.set(currentSet, done.sets.get(currentSet) + 1);

            TextView progressLabel = (TextView)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityProgressLabel);
            progressLabel.setText("" + (currentSet + 1) + ":" + done.sets.get(currentSet) + "/" + reps.sets.get(currentSet));

            if (done.sets.get(currentSet) >= reps.sets.get(currentSet)) // a set finished
              {
                SoundManager.play(R.raw.beep_four);
                next_sound = 0;
                currentSet++;

                if (currentSet >= reps.sets.size()) // exercise finished
                  {
                    finishExercise();
                    return 0;
                  }

                progressLabel.setText("" + (currentSet + 1) + ":" + done.sets.get(currentSet) + "/" + reps.sets.get(currentSet));

                if (props.pause_after_set > 0)
                  {
                    pause_duration = props.pause_after_set;
                    return System.currentTimeMillis();
                  }
              }
            phase++;
          }

        if (next_sound != 0)
          {
            SoundManager.play(next_sound);
            next_sound = 0;
          }

        // phase 1: primary motion
        if (phase == 1)
          {
            next_sound = (props.primary_motion == Properties.PrimaryMotion.concentric ? R.raw.beep_high : R.raw.beep_low);
            if (props.primary_motion == Properties.PrimaryMotion.concentric && props.reps_duration_concentric > 0)
              return System.currentTimeMillis() + props.reps_duration_concentric;
            if (props.primary_motion == Properties.PrimaryMotion.eccentric && props.reps_duration_eccentric > 0)
              return System.currentTimeMillis() + props.reps_duration_eccentric;
            phase++; // skip phase if no time set
          }

        // phase 2: pause after primary motion
        if (phase == 2)
          {
            next_sound = (props.primary_motion == Properties.PrimaryMotion.concentric ? R.raw.beep_high : R.raw.beep_low);
            if (props.primary_motion == Properties.PrimaryMotion.concentric && props.reps_pause_after_concentric > 0)
              return System.currentTimeMillis() + props.reps_pause_after_concentric;
            if (props.primary_motion == Properties.PrimaryMotion.eccentric && props.reps_pause_after_eccentric > 0)
              return System.currentTimeMillis() + props.reps_pause_after_eccentric;
            phase++; // skip phase if no time set
          }

        // phase 3: secondary motion
        if (phase == 3)
          {
            next_sound = (props.primary_motion == Properties.PrimaryMotion.concentric ? R.raw.beep_low : R.raw.beep_high);
            if (props.primary_motion == Properties.PrimaryMotion.concentric && props.reps_duration_eccentric > 0)
              return System.currentTimeMillis() + props.reps_duration_eccentric;
            if (props.primary_motion == Properties.PrimaryMotion.eccentric && props.reps_duration_concentric > 0)
              return System.currentTimeMillis() + props.reps_duration_concentric;
            phase++; // skip phase if no time set
          }

        // phase 3: pause after secondary motion
        if (phase == 4)
          {
            next_sound = (props.primary_motion == Properties.PrimaryMotion.concentric ? R.raw.beep_low : R.raw.beep_high);
            if (props.primary_motion == Properties.PrimaryMotion.concentric && props.reps_pause_after_eccentric > 0)
              return System.currentTimeMillis() + props.reps_pause_after_eccentric;
            if (props.primary_motion == Properties.PrimaryMotion.eccentric && props.reps_pause_after_concentric > 0)
              return System.currentTimeMillis() + props.reps_pause_after_concentric;
          }

        return System.currentTimeMillis();
      }

    private long updateSetPause ()
      {
        if (pause_start == 0)
          {
            pause_start = System.currentTimeMillis();
            pause_countdown = 3;
          }

        long elapsed = System.currentTimeMillis() - pause_start;
        long remaining = Math.round((pause_duration - elapsed) / 1000.0);

        if (pause_countdown > 0 && pause_countdown >= remaining)
          {
            SoundManager.play(R.raw.beep_low);
            pause_countdown--;
          }

        if (remaining <= 0)
          {
            SoundManager.play(R.raw.beep_four);
            pause_duration = 0;
            pause_start = 0;
            countdown = 3;
            running = false;
            Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
            contextButton.setEnabled(true);
            contextButton.setText(ContextManager.get().getString(R.string.TrainingActivityContextButtonTextStart));
            return 0;
          }

        long min = remaining / 60;
        long sec = remaining % 60;

        Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
        contextButton.setEnabled(false);
        contextButton.setText((min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);

        return pause_start + (Math.round(elapsed / 1000.0) + 1) * 1000;
      }

  }

  private class PauseTask extends TaskManager.TimerTask
  {
    private long duration;

    private long start = 0;
    private long countdown = 3;

    public PauseTask (long duration)
      {
        this.duration = duration;
      }

    public long update ()
      {
        if (start == 0)
          start = System.currentTimeMillis();

        long elapsed = System.currentTimeMillis() - start;
        long remaining = Math.round((duration - elapsed) / 1000.0);

        if (countdown > 0 && countdown >= remaining)
          {
            SoundManager.play(R.raw.beep_low);
            countdown--;
          }

        if (remaining <= 0)
          {
            SoundManager.play(R.raw.beep_four);
            afterPause();
            return 0;
          }

        long min = remaining / 60;
        long sec = remaining % 60;

        Button contextButton = (Button)TrainingActivity.getInstance().findViewById(R.id.TrainingActivityContextButton);
        contextButton.setEnabled(false);
        contextButton.setText((min < 10 ? "0" : "") + min + ":" + (sec < 10 ? "0" : "") + sec);

        return start + (Math.round(elapsed / 1000.0) + 1) * 1000;
      }
  }

}
