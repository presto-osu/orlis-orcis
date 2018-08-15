/*
 * SMS-bypass - SMS bypass for Android
 * Copyright (C) 2015  Mathieu Souchaud
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
 *
 * Forked from smsfilter (author: Jelle Geerts).
 */

package souch.smsbypass;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MessageList extends Activity
{
    //private static final String TAG = "Messages";

    private Settings mSettings;

    private MessageListArrayAdapter mAdapter;

    private ListView mListView;

    private BroadcastReceiver mReceiver;

    private class MessageListArrayAdapter extends ArrayAdapter<Message>
    {
        public MessageListArrayAdapter(Context context, List<Message> messages)
        {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            View v = convertView;

            // todo: show only last msg for same filter

            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.message_list_item, null);
                holder = new ViewHolder();
                holder.addressTextView = (TextView) v.findViewById(R.id.address);
                holder.filterTextView = (TextView) v.findViewById(R.id.filter);
                holder.messageTextView = (TextView) v.findViewById(android.R.id.message);
                holder.receivedAtTextView = (TextView) v.findViewById(R.id.receivedAt);
                v.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }

            Message message = getItem(position);
            holder.filterTextView.setText(message.filter);

            holder.addressTextView.setText(message.address);
            String draft = "";
            if(message.type == Message.MSG_TYPE_DRAFT)
                draft = "(" + getString(R.string.draft) + ") ";
            holder.messageTextView.setText(draft + message.message);
            holder.receivedAtTextView.setText(
                    TimeFormatter.f(
                            MessageList.this, message.receivedAt, TimeFormatter.SHORT_FORMAT));

            if (message.type == Message.MSG_TYPE_RECEIVED)
                v.setBackgroundColor(MessageListFilter.RECEIVED_COLOR);
            else if (message.type == Message.MSG_TYPE_SENT)
                v.setBackgroundColor(MessageListFilter.SENT_COLOR);
            else if(message.type == Message.MSG_TYPE_DRAFT)
                v.setBackgroundColor(MessageListFilter.DRAFT_COLOR);

            return v;
        }

        private class ViewHolder
        {
            TextView addressTextView;
            TextView filterTextView;
            TextView messageTextView;
            TextView receivedAtTextView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSettings = new Settings(getApplicationContext());

        setTitle(getString(R.string.messages));
        setContentView(R.layout.message_list);

        mListView = (ListView) findViewById(R.id.messageList);

        List<Message> messages = new ArrayList<Message>();
        mAdapter = new MessageListArrayAdapter(this, messages);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Message message = mAdapter.getItem(position);
                        startFilterListActivity(message.filter);
                    }
                });

        registerForContextMenu(mListView);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // force exiting in order to not come back to this view if the app was only put in background
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // force going to previous menu as the previous one has been destroyed
                Intent intent = new Intent(this, UI.class);
                startActivity(intent);

                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Messages may have been removed via the notification, hence we simply
        // always refresh the message list.
        refreshList();

        mReceiver =
            new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    refreshList();
                }
            };
        registerReceiver(mReceiver, new IntentFilter(Settings.ACTION_NEW_MESSAGE));

        // remove notification as soon as user seems to have seen it
        Notifier.cancel(getApplicationContext(), Notifier.NEW_MESSAGE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (!(v instanceof ListView))
            throw new AssertionError();
        menu.setHeaderTitle(R.string.messageOptions);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.message_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int adapterPosition = info.position;
        int menuItemID = item.getItemId();
        if (menuItemID == R.id.view)
        {
            startFilterListActivity(mAdapter.getItem(adapterPosition).filter);
            return true;
        }
        else if (menuItemID == R.id.delete)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete)
                .setMessage(R.string.messageWillBeDeleted)
                .setPositiveButton(
                    R.string.delete,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            mSettings.deleteMessage(mAdapter.getItem(adapterPosition).id);
                            mAdapter.remove(mAdapter.getItem(adapterPosition));
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                .setNegativeButton(android.R.string.cancel, null);
            builder.show();
            return true;
        }
        return false;
    }

    private void startFilterListActivity(String filterName)
    {
        Intent intent = new Intent(this, MessageListFilter.class);
        intent.putExtra(MessageListFilter.FILTER_NAME_EXTRA, filterName);
        startActivityForResult(intent, FilterForm.REQUEST_CODE_MUTATED);
    }

    private void refreshList()
    {
        List<Message> messages = mSettings.getMessages();

        mAdapter.clear();
        for (Message message : messages)
            mAdapter.add(message);
        mAdapter.notifyDataSetChanged();
    }

    public void confirmDeleteAll()
    {
        if (mAdapter.getCount() == 0)
        {
            Toast.makeText(this, R.string.noMessagesToDelete, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.delete)
            .setMessage(R.string.allMessagesWillBeDeleted)
            .setPositiveButton(
                R.string.delete,
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteAll();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void deleteAll()
    {
        if (mAdapter.getCount() == 0)
        {
            // Condition should've been handled by confirmDeleteAll().
            throw new AssertionError();
        }

        for (int i = 0; i < mAdapter.getCount(); ++i)
        {
            Message message = mAdapter.getItem(i);
            mSettings.deleteMessage(message.id);
        }
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message_list_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete_all:
                confirmDeleteAll();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onNewMsg(View v)
    {
        Intent intent = new Intent(this, FilterListPicker.class);
        startActivityForResult(intent, FilterListPicker.REQUEST_PICK_FILTER);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (FilterForm.REQUEST_CODE_MUTATED) :
                if (resultCode == FilterForm.RESULT_CODE_MUTATED)
                    refreshList();
                break;
        }
    }


}
