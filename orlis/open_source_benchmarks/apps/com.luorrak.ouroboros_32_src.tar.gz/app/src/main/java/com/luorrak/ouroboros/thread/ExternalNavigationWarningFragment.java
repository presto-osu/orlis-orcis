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
public class ExternalNavigationWarningFragment extends DialogFragment {

    public static ExternalNavigationWarningFragment newInstance(String url) {
        ExternalNavigationWarningFragment frag = new ExternalNavigationWarningFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final boolean external = getArguments().getBoolean("external");
        final String url = getArguments().getString("url");

        return new AlertDialog.Builder(getActivity())
                .setTitle("Proceed to URL?")
                .setMessage(url)
                .setPositiveButton(R.string.navigation_warning_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((ThreadActivity)getActivity()).doPositiveClickExternal(url);
                            }
                        }
                )
                .setNegativeButton(R.string.navigation_warning_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((ThreadActivity)getActivity()).doNegativeClick();
                            }
                        }
                )
                .create();
    }

    public void show(){
        getActivity();
    }
}
