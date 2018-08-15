package com.luk.timetable2.listeners.MainActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.luk.timetable2.R;
import com.luk.timetable2.Utils;
import com.luk.timetable2.activities.MainActivity;
import com.luk.timetable2.tasks.DeleteLessonTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luk on 5/7/15.
 */
public class DeleteDialogListener implements View.OnLongClickListener {
    private final MainActivity mMainActivity;
    private int mDay;

    public DeleteDialogListener(MainActivity mainActivity, int day) {
        mMainActivity = mainActivity;
        mDay = day;
    }

    @Override
    public boolean onLongClick(View view) {
        TextView info = (TextView) view.findViewById(R.id.info);
        String hour = info.getText().toString().split("\n")[0];

        new Task(mMainActivity, mDay, hour).execute();
        return false;
    }
}

class Task extends AsyncTask<Integer, Integer, Integer> {
    private MainActivity mMainActivity;
    private int mDay;
    private String mHour;

    public Task(MainActivity mainActivity, int day, String hour) {
        mMainActivity = mainActivity;
        mDay = day;
        mHour = hour;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        final ArrayList<List<String>> lessons = Utils.getLessonsForHour(mMainActivity, mDay, mHour);

        if (lessons != null && lessons.size() > 1) {
            final CharSequence[] items = new String[lessons.size()];
            final int[] selected = {1};

            for (int i = 0; i < lessons.size(); i++) {
                items[i] = lessons.get(i).get(0);
            }

            mMainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getString(R.string.select_lesson))
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int number) {
                                            new DeleteLessonTask(
                                                    mMainActivity,
                                                    false,
                                                    mHour,
                                                    mDay,
                                                    lessons,
                                                    selected[0]
                                            ).execute();
                                        }
                                    })
                            .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int number) {
                                    selected[0] = number;
                                }
                            })
                            .show();
                }
            });
        } else {
            mMainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getString(R.string.hide_title))
                            .setMessage(mMainActivity.getString(R.string.hide_text))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    new DeleteLessonTask(
                                            mMainActivity,
                                            true,
                                            mHour,
                                            mDay,
                                            null,
                                            null
                                    ).execute();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            });
        }

        return null;
    }
}