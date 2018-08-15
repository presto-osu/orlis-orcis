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

package fr.mobdev.goblim.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.mobdev.goblim.Database;
import fr.mobdev.goblim.dialog.ServerDialog;
import fr.mobdev.goblim.listener.ServerListener;
import fr.mobdev.goblim.objects.Server;
import fr.mobdev.goblim.R;

/*
 * Activity that allow user to manage the server where he want to upload his images. Server must be lutim instance to work with the app
 */
public class ServersActivity extends AppCompatActivity {

    private List<Server> dbServers;
    private View.OnClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servers);

        Toolbar toolbar = (Toolbar) findViewById(R.id.servers_toolbar);
        setSupportActionBar(toolbar);

        ListView serverList = (ListView) findViewById(R.id.servers_list);
        serverList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //find oldDefault
                Server oldDefaultServer = null;
                for(Server server : dbServers)
                {
                    if(server.isDefaultServer())
                    {
                        oldDefaultServer = server;
                        break;
                    }
                }
                Server newDefaultServer = dbServers.get(position);
                //if old default server exist or not, make the selected one the new Default
                if(oldDefaultServer == null) {
                    Database.getInstance(getApplicationContext()).setDefaultServer(newDefaultServer.getId(),-1);
                }
                else {
                    Database.getInstance(getApplicationContext()).setDefaultServer(newDefaultServer.getId(), oldDefaultServer.getId());
                }
                updateServers();
                return true;
            }
        });

        //listener use to manage delete button on each view
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the position of the server in the list
                Integer pos = (Integer)v.getTag();
                if(pos == null)
                    return;
                //retrieve the server
                final Server server = dbServers.get(pos);
                //ask for delete to the user
                AlertDialog.Builder builder = new AlertDialog.Builder(ServersActivity.this);
                builder.setMessage(getString(R.string.delete_server_message)+" "+server.getUrl())
                 .setCancelable(false)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             //delete server from database and update the view
                             dbServers.remove(server);
                             Database.getInstance(getApplicationContext()).deleteServer(server.getId());
                             updateServers();
                         }
                 })
                 .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                                 dialog.cancel();
                         }
                 });
                AlertDialog alert = builder.create();
                alert.show();
            }
        };

        updateServers();
    }

    private void updateServers()
    {
        // build the view with server list in database
        dbServers = Database.getInstance(getApplicationContext()).getServers(false);

        ServerAdapter adapter = new ServerAdapter(this,R.layout.server_item,R.id.server_name,dbServers,listener);

        ListView serverList = (ListView) findViewById(R.id.servers_list);
        serverList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_servers, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_server) {

            ServerDialog serverDialog = new ServerDialog();
            ServerListener serverListener = new ServerListener() {
                @Override
                public void updateServerList() {
                    updateServers();
                }
            };
            serverDialog.setServerListener(serverListener);
            serverDialog.show(getSupportFragmentManager(),"Server Dialog");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //adapter for the server list
    private class ServerAdapter extends ArrayAdapter<Server>
    {

        private List<Server> servers;
        private LayoutInflater mInflater;
        private View.OnClickListener listener;
        int resource;

        public ServerAdapter(Context context, int resource, int textViewResourceId, List<Server> objects, View.OnClickListener listener) {
            super(context, resource, textViewResourceId, objects);
            servers = new ArrayList<>(objects);
            this.listener = listener;
            this.resource = resource;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //get the server
            Server server = servers.get(position);
            //create a new view or reuse a previous one
            if (convertView == null) {
                convertView = mInflater.inflate(resource, parent, false);
            }

            //setup the server url view
            TextView view = (TextView) convertView.findViewById(R.id.server_name);
            if(server.isDefaultServer()) {
                Typeface typeface = view.getTypeface();
                Typeface newTypeface = Typeface.create(typeface,Typeface.BOLD);
                view.setTypeface(newTypeface);
            }
            view.setText(server.getUrl());

            //setup the delete button view
            ImageView delete = (ImageView) convertView.findViewById(R.id.server_delete);
            delete.setOnClickListener(listener);
            delete.setTag(position);

            return convertView;
        }
    }
}
