/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.net;

import android.os.AsyncTask;

import java.util.ArrayList;

public class SickbeardTaskPool<T> implements AsyncTaskCompleteListener {
    private ArrayList<SickbeardAsyncTask> mTasks;
    private TaskPoolSubscriber mSubscriber;

    public SickbeardTaskPool() {
        mTasks = new ArrayList<SickbeardAsyncTask>();
    }

    public void addTask(SickbeardAsyncTask task) {
        task.addCompleteListener(this);
        mTasks.add(task);
    }

    public void setTaskPoolSubscriber(TaskPoolSubscriber subscriber) {
        mSubscriber = subscriber;
    }

    public void start(T... args) {
        for (int i = mTasks.size() - 1; i >= 0; i--) {
            mTasks.get(i).start(args);
        }
    }

    @Override
    public void onAsyncTaskComplete(AsyncTask task) {
        mTasks.remove(task);
        if (mTasks.size() == 0) {
            if (mSubscriber != null) {
                mSubscriber.executionFinished();
            }
        }
    }
}