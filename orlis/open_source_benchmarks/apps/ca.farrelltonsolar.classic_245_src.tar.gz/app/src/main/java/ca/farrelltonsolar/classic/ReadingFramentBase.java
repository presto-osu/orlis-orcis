/*
 * Copyright (c) 2014. FarrelltonSolar
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ca.farrelltonsolar.classic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Graham on 14/12/2014.
 */
public abstract class ReadingFramentBase extends Fragment implements ReadingFragmentInterface {

    int layoutId;
    private boolean isReceiverRegistered = false;
    protected ViewGroup container;

    protected ReadingFramentBase(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        View theView = inflater.inflate(layoutId, container, false);
        return theView;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(ReadingFramentBase.this.getActivity()).unregisterReceiver(mReadingsReceiver);
            } catch (IllegalArgumentException e) {
                // Do nothing
            }
            isReceiverRegistered = false;
        }
        Log.d(getClass().getName(), "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(ReadingFramentBase.this.getActivity()).registerReceiver(mReadingsReceiver, new IntentFilter(Constants.CA_FARRELLTONSOLAR_CLASSIC_READINGS));
            isReceiverRegistered = true;
        }
        Log.d(getClass().getName(), "onStart");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeReadings(view, savedInstanceState);
    }

    // Our handler for received Intents.
    protected BroadcastReceiver mReadingsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            doSetReadings(new Readings(intent.getBundleExtra("readings")));
        }
    };

    protected void doSetReadings(Readings reading) {
        if (this.isVisible()) {
            setReadings(reading);
        }
    }


}
