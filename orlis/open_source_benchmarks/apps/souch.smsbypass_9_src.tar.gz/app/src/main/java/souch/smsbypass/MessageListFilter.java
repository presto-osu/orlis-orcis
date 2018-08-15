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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageListFilter extends Activity
{
    public static final int SENT_COLOR = Color.argb(255, 186, 228, 244);
    public static final int RECEIVED_COLOR = Color.argb(255, 255, 239, 152);
    public static final int DRAFT_COLOR = Color.argb(255, 0xd9, 0xe9, 0xef);//0xff, 0xb5, 0xb5);d9e9ef

    public static final String FILTER_NAME_EXTRA = C.PACKAGE_NAME + ".filter_name";
    private String mFilterName = null;
    private String mAddress;
    private Settings mSettings;

    private EditText mNewMsgEditText;

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

            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.message_list_filter_item, null);
                holder = new ViewHolder();
                holder.messageTextView = (TextView) v.findViewById(R.id.message);
                holder.receivedAtTextView = (TextView) v.findViewById(R.id.receivedAt);
                holder.layout = (LinearLayout) v.findViewById(R.id.layout);
                holder.layoutInside = (LinearLayout) v.findViewById(R.id.layoutInside);
                v.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) v.getTag();
            }

            Message message = getItem(position);
            String msg = message.message;
            int backgroundColor;
            if (message.type == Message.MSG_TYPE_SENT) {
                holder.messageTextView.setGravity(Gravity.RIGHT);
                backgroundColor = SENT_COLOR;
                holder.layout.setGravity(Gravity.RIGHT);
                holder.layout.setPadding(8, 0, 0, 0);
            }
            else {
                holder.messageTextView.setGravity(Gravity.LEFT);
                backgroundColor = RECEIVED_COLOR;
                holder.layout.setGravity(Gravity.LEFT);
                holder.layout.setPadding(0, 0, 8, 0);
            }
            holder.layoutInside.setBackgroundColor(backgroundColor);
            holder.messageTextView.setText(msg);
            holder.receivedAtTextView.setText(
                TimeFormatter.f(MessageListFilter.this, message.receivedAt, TimeFormatter.FULL_FORMAT));

            return v;
        }

        private class ViewHolder
        {
            TextView messageTextView;
            TextView receivedAtTextView;
            LinearLayout layout;
            LinearLayout layoutInside;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new Settings(this);

        setContentView(R.layout.message_list_filter);

        mNewMsgEditText = (EditText) findViewById(R.id.messageEditText);

        Intent intent = getIntent();
        mFilterName = intent.getStringExtra(FILTER_NAME_EXTRA);
        if (mFilterName == null)
            throw new AssertionError();
        Filter filter = mSettings.findFilterByName(mFilterName);
        if (filter != null) {
            mAddress = filter.address;
            setTitle(mFilterName + " (" + mAddress + ")");

            Message draftMessage = mSettings.getDraftMessage(mFilterName);
            if (draftMessage != null)
                mNewMsgEditText.setText(draftMessage.message);
        }
        else {
            setTitle(mFilterName + " (" + getString(R.string.filterDeleted) + ")");

            Button newMsgButton = (Button) findViewById(R.id.sendButton);
            newMsgButton.setEnabled(false);
            mNewMsgEditText.setEnabled(false);
        }

        mListView = (ListView) findViewById(R.id.messageList);

        List<Message> messages = new ArrayList<Message>();
        mAdapter = new MessageListArrayAdapter(this, messages);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Message message = mAdapter.getItem(position);
                        startViewerActivity(message.id);
                    }
                });

        registerForContextMenu(mListView);
    }

    @Override
    protected void onStop() {
        super.onStop();

        String newMessageText = mNewMsgEditText.getText().toString();
        if (newMessageText.length() == 0)
            mSettings.deleteDraftMessage(mFilterName);
        else
            mSettings.saveDraftMessage(mFilterName, mAddress, newMessageText);

        setResult(FilterForm.RESULT_CODE_MUTATED);

        // force exiting in order to not come back to this view if the app was only put in background
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // force going to previous menu as the previous one has been destroyed
                Intent intent = new Intent(this, MessageList.class);
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

        // remove notification as soon as user seems to have seen it
        Notifier.cancel(getApplicationContext(), Notifier.NEW_MESSAGE);

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
        final long messageID = mAdapter.getItem(adapterPosition).id;
        int menuItemID = item.getItemId();
        if (menuItemID == R.id.view)
        {
            startViewerActivity(messageID);
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
                            mSettings.deleteMessage(messageID);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MessageViewer.REQUEST_CODE_MUTATED)
        {
            if (resultCode == MessageViewer.RESULT_CODE_MUTATED)
                refreshList();
        }
    }

    private void startViewerActivity(long messageID)
    {
        Intent intent = new Intent(this, MessageViewer.class);
        intent.putExtra(MessageViewer.MESSAGE_ID_EXTRA, messageID);
        startActivityForResult(intent, MessageViewer.REQUEST_CODE_MUTATED);
    }

    private void refreshList()
    {
        List<Message> messages = mSettings.getMessages(mFilterName, false);

        mAdapter.clear();
        for (Message message : messages)
            mAdapter.add(message);
        mAdapter.notifyDataSetChanged();

        mListView.setSelection(mListView.getCount() - 1); // smoothScrollToPosition
    }

    public void onSendMessage(View v)
    {
        SmsManager sms = SmsManager.getDefault();
        try {
            String msg = mNewMsgEditText.getText().toString();
            sms.sendTextMessage(mAddress, null, msg, null, null);
            mSettings.saveMessage(mFilterName, mAddress, System.currentTimeMillis(), Message.MSG_TYPE_SENT, msg);
            mNewMsgEditText.getText().clear();
            mSettings.deleteDraftMessage(mFilterName);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.sendSMSFailed,
                    Toast.LENGTH_LONG).show();
            //e.printStackTrace();
        }
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
        mSettings.deleteDraftMessage(mFilterName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.message_list_filter_menu, menu);
        return true;
    }

    private void exportMessages()
    {

        List<Message> messages = mSettings.getMessages(mFilterName, false);
        if (messages != null && !messages.isEmpty()) {
            String exportFilename = null;

            final int maxTestFile = 9999999;
            int i;
            //String musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

            File exportDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_dir));
            File exportFile = null;
            for (i = 0; i < maxTestFile; i++) {
                exportFile = new File(exportDir, getString(R.string.exportFileBasename) + i + ".txt");
                // file = getApplicationContext().getFileStreamPath(exportFile);
                if(!exportFile.exists()) {
                    exportFilename = exportFile.getPath();
                    break;
                }
            }
            if (exportFilename == null) {
                Toast.makeText(getApplicationContext(),
                        R.string.exportMessagesError,
                        Toast.LENGTH_LONG).show();
            }
            else {
                boolean exportOk = false;
                try {
                    exportDir.mkdirs();
                    FileOutputStream stream = new FileOutputStream(exportFile);
                    //outputStream = openFileOutput(exportFilename, Context.MODE_PRIVATE);
                    String address = mAddress;
                    if (address == null)
                        address = getString(R.string.noAddressSpecified);
                    stream.write((mFilterName + " (" + address + ")\n\n").getBytes());
                    for (Message message : messages) {
                        if (message.type == Message.MSG_TYPE_RECEIVED)
                            stream.write("<-".getBytes());
                        else
                            stream.write("->".getBytes());
                        stream.write((" " + TimeFormatter.f(MessageListFilter.this, message.receivedAt, TimeFormatter.FULL_FORMAT)).getBytes());
                        stream.write((" " + message.message).getBytes());
                        stream.write(("\n").getBytes());
                    }
                    stream.close();
                    exportOk = true;
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.exportMessagesError) + " (" + e.getMessage() + ")",
                            Toast.LENGTH_LONG).show();
                }

                if (exportOk) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.messagesExported) + " " + exportFilename,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_delete_all:
                confirmDeleteAll();
                return true;
            case R.id.action_export_messages:
                exportMessages();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
