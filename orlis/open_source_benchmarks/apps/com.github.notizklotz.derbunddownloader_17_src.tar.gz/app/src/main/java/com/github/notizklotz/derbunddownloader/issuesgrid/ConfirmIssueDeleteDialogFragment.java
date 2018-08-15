/*
 * Der Bund ePaper Downloader - App to download ePaper issues of the Der Bund newspaper
 * Copyright (C) 2013 Adrian Gygax
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see {http://www.gnu.org/licenses/}.
 */

package com.github.notizklotz.derbunddownloader.issuesgrid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.github.notizklotz.derbunddownloader.R;

public class ConfirmIssueDeleteDialogFragment extends DialogFragment {

    private static final String ARG_ISSUE_ID = "issueID";

    static ConfirmIssueDeleteDialogFragment createDialogFragment(long issueID) {
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_ISSUE_ID, issueID);
        ConfirmIssueDeleteDialogFragment confirmIssueDeleteDialogFragment = new ConfirmIssueDeleteDialogFragment();
        confirmIssueDeleteDialogFragment.setArguments(bundle);
        return confirmIssueDeleteDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long issueId = getArguments().getLong("issueID");

        return new AlertDialog.Builder(getActivity()).setMessage("Heruntergeladene Ausgabe entfernen?")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((DownloadedIssuesActivity) getActivity()).deleteIssue(issueId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }).create();
    }
}
