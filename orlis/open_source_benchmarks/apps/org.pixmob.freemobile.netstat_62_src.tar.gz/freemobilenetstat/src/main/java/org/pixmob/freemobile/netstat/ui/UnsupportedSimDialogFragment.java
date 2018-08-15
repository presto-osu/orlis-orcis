/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.freemobile.netstat.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.pixmob.freemobile.netstat.MonitorService;
import org.pixmob.freemobile.netstat.R;

/**
 * Fragment shown when the SIM card is not compatible with the application.
 * @author Pixmob
 */
public class UnsupportedSimDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.unsupported_sim_error)
                .setMessage(R.string.unsupported_sim_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.quit_application,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    }).create();
    }

    @Override
    public void onStop() {
        getActivity().stopService(new Intent(getActivity().getApplicationContext(), MonitorService.class));
        System.exit(0);
    }
}
