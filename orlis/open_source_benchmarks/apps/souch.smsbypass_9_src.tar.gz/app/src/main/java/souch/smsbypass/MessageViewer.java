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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class MessageViewer extends Activity
{
    //private static final String TAG = "MessageViewer";

    public static final int REQUEST_CODE_MUTATED = 0;

    public static final int RESULT_CODE_NOT_MUTATED = 0;
    public static final int RESULT_CODE_MUTATED = 1;

    public static final String MESSAGE_ID_EXTRA = C.PACKAGE_NAME + ".message_id";

    private static final long MESSAGE_ID_INITIAL = -1;
    private long mMessageID = MESSAGE_ID_INITIAL;
    private Message mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.message_viewer);

        Intent intent = getIntent();
        mMessageID = intent.getLongExtra(MESSAGE_ID_EXTRA, MESSAGE_ID_INITIAL);
        if (mMessageID < 0)
            throw new AssertionError();

        // User is viewing this message. Hence, remove the notification.
        Notifier.cancel(this, Notifier.NEW_MESSAGE);

        mMessage = new Settings(this).getMessage(mMessageID);

        TextView addressTextView = (TextView) findViewById(R.id.address);
        TextView receivedAtTextView = (TextView) findViewById(R.id.receivedAt);
        TextView messageTextView = (TextView) findViewById(R.id.message);


        addressTextView.setText(
            getString(R.string.from)
            +  ": " + mMessage.filter + " (" + mMessage.address + ")");
        receivedAtTextView.setText(
            getString(R.string.received)
            + ": " + TimeFormatter.f(this, mMessage.receivedAt, TimeFormatter.FULL_FORMAT));
        messageTextView.setText(mMessage.message);

        setTitle(mMessage.address);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // force exiting in order to not come back to this view if the app was only put in background
        finish();
    }

    private void goBack() {
        Intent intent = new Intent(this, MessageListFilter.class);
        intent.putExtra(MessageListFilter.FILTER_NAME_EXTRA, mMessage.filter);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // force going to previous menu as the previous one has been destroyed
                goBack();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onConfirmDelete(View v)
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
                        delete();
                    }
                })
            .setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private void delete()
    {
        new Settings(this).deleteMessage(mMessageID);

        setResult(RESULT_CODE_MUTATED);
        goBack();
    }
}
