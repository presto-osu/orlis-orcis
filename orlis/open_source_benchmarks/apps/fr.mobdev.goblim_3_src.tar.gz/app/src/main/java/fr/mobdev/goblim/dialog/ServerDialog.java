/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.support.annotation.NonNull;

import fr.mobdev.goblim.Database;
import fr.mobdev.goblim.listener.ServerListener;
import fr.mobdev.goblim.R;

/*
 * Dialog allow user to add a new Server where he can upload images
 */
public class ServerDialog extends DialogFragment {

    private static final int HTTPS_POSITION = 1;
    private ServerListener listener;

    public void setServerListener(ServerListener listener)
    {
        this.listener = listener;
    }

    @Override
    public @NonNull Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = View.inflate(getActivity(),R.layout.server_dialog, null);

        builder.setView(view)
                .setTitle(R.string.server_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //build the url
                        String url = "http";
                        //is it http or https?
                        Spinner httpSpinner = (Spinner) view.findViewById(R.id.http_spinner);
                        if(httpSpinner.getSelectedItemPosition() == HTTPS_POSITION)
                            url += "s";
                        url +="://";
                        //get the rest of the url
                        EditText urlText = (EditText) view.findViewById(R.id.url_text);
                        if(urlText.getText().length() > 0)
                            url += urlText.getText();
                        //add server to database
                        Database.getInstance(getActivity().getApplicationContext()).addServer(url);
                        listener.updateServerList();
                        dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
