package com.luk.timetable2.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.luk.timetable2.parser.Parser;
import com.luk.timetable2.R;
import com.luk.timetable2.Utils;
import com.luk.timetable2.activities.MainActivity;

import java.util.HashMap;

/**
 * Created by LuK on 2015-05-01.
 */
public class ClassesTask extends AsyncTask<Integer, Integer, Integer> {
    private static String TAG = "ClassesTask";
    private MainActivity mMainActivity;
    private ProgressDialog mDialog;
    private String mUrl;
    private HashMap<Integer, String> mData;

    public ClassesTask(MainActivity mainActivity) {
        mMainActivity = mainActivity;

        // load prefs
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mMainActivity);

        this.mUrl = sharedPref.getString("school", "");
    }

    @Override
    protected Integer doInBackground(Integer... strings) {
        mMainActivity.runOnUiThread(new Runnable() {
            public void run() {
                mDialog = ProgressDialog.show(
                        mMainActivity,
                        null,
                        mMainActivity.getString(R.string.sync_in_progress),
                        true
                );
            }
        });

        try {
            mData = new Parser(String.format("%s/lista.html", mUrl)).parseClasses();
        } catch (Exception e) {
            Log.e(TAG, "", e);
            return -1;
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (result == -1) {
            final String msg = mMainActivity.getString(Utils.isOnline(mMainActivity) ?
                    R.string.error_offline : R.string.error_no_network);

            mMainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getString(R.string.error_title))
                            .setMessage(msg)
                            .setPositiveButton(android.R.string.yes, null)
                            .show();
                }
            });
        } else {
            final CharSequence[] items = new String[mData.size()];
            final int[] selected = {1};

            for (int i = 1; i <= mData.size(); i++) {
                items[i - 1] = mData.get(i);
            }

            mMainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    new AlertDialog.Builder(mMainActivity)
                            .setTitle(mMainActivity.getString(R.string.select_class))
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface mDialog, int number) {
                                            new SyncTask(mMainActivity, selected[0]).execute();
                                        }
                                    })
                            .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface mDialog, int number) {
                                    selected[0] = number + 1;
                                }
                            })
                            .show();
                }
            });
        }

        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}