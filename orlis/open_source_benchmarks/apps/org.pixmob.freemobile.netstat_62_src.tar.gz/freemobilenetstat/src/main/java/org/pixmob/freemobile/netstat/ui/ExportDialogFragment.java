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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.pixmob.freemobile.netstat.R;

/**
 * Fragment for exporting the application database, showing a progress dialog.
 * @author Pixmob
 * @see ExportTask
 */
public class ExportDialogFragment extends DialogFragment {
    public void update(int current, int total) {
        final ProgressDialog dialog = (ProgressDialog) getDialog();
        if (dialog != null) {
            dialog.setIndeterminate(false);
            dialog.setMax(total);
            dialog.setProgress(current);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.exporting_data));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }
}
