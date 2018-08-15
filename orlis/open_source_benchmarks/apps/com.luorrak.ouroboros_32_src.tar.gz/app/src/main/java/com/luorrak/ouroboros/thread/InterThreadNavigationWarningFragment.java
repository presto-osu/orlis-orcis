package com.luorrak.ouroboros.thread;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.luorrak.ouroboros.R;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class InterThreadNavigationWarningFragment extends DialogFragment {
    private final String LOG_TAG = InterThreadNavigationWarningFragment.class.getSimpleName();

    public static InterThreadNavigationWarningFragment newInstance(String url) {
        InterThreadNavigationWarningFragment frag = new InterThreadNavigationWarningFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String url = getArguments().getString("url");
        String[] url_split = url.split("/"); // Format /v/res/123456789.html#123456
        final String board = url_split[1];

        if (url.contains("index.html")){
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Proceed to board?")
                    .setMessage(">>>/" + board + "/")
                    .setPositiveButton(R.string.navigation_warning_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    ((ThreadActivity) getActivity()).doPositiveClickInternal("0", board);
                                }
                            }
                    )
                    .setNegativeButton(R.string.navigation_warning_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ThreadActivity) getActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        } else {
            final String no = url_split[3].split("\\.")[0];
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Proceed to thread?")
                    .setMessage(">>>/" + board + "/" + no)
                    .setPositiveButton(R.string.navigation_warning_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    ((ThreadActivity) getActivity()).doPositiveClickInternal(no, board);
                                }
                            }
                    )
                    .setNegativeButton(R.string.navigation_warning_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    ((ThreadActivity) getActivity()).doNegativeClick();
                                }
                            }
                    )
                    .create();
        }
    }

    public void show(){
        getActivity();
    }
}
