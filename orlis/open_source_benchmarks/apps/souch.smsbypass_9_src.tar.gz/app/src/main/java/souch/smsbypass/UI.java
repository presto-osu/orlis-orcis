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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class UI extends ListActivity
{
    private static final String TAG = "UI";

    private int mSaveBlockedMessagesCheckableItemPosition = -1;
    private int mVibrateCheckableItemPosition = -2;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Settings settings = new Settings(this);

        if (android.os.Build.VERSION.SDK_INT > Integer.decode(getString(R.string.max_version))) // android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
            showAppMayNotWorkWarning();

        List<View> views = new ArrayList<View>();

        views.add(SimpleListItem2.build(
            this,
            R.id.filters,
            getString(R.string.messageFilters),
            getString(R.string.viewAndEditMessageFilters)));

        views.add(SimpleListItem2.build(
            this,
            R.id.messages,
            getString(R.string.messages),
            getString(R.string.viewBlockedMessages)));

        CheckableLinearLayout checkableLinearLayout =
                CheckableLinearLayout.build(
                        this,
                        R.id.saveBlockedMessages,
                        getString(R.string.saveMessages),
                        getString(R.string.saveMessagesAndShowNotifications));
        mSaveBlockedMessagesCheckableItemPosition = views.size();
        views.add(checkableLinearLayout);

        CheckableLinearLayout checkableVibrateLinearLayout =
                CheckableLinearLayout.build(
                        this,
                        R.id.vibrate,
                        getString(R.string.vibrate),
                        getString(R.string.vibrateSummary));
        mVibrateCheckableItemPosition = views.size();
        views.add(checkableVibrateLinearLayout);

        views.add(SimpleListItem2.build(
                this,
                R.id.version,
                getString(R.string.version),
                getString(R.string.app_version)));

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        ViewsAdapter adapter = new ViewsAdapter(views);
        setListAdapter(adapter);

        if (settings.saveMessages())
            listView.setItemChecked(mSaveBlockedMessagesCheckableItemPosition, true);
        if (settings.getVibrate())
            listView.setItemChecked(mVibrateCheckableItemPosition, true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // force exiting in order to not come back to this view if the app was only put in background
        finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        int itemID = v.getId();
        if (itemID == R.id.filters)
        {
            Intent intent = new Intent(this, FilterList.class);
            startActivity(intent);
        }
        else if (itemID == R.id.messages)
        {
            Intent intent = new Intent(this, MessageList.class);
            startActivity(intent);
        }
        else if (itemID == R.id.saveBlockedMessages)
        {
            SparseBooleanArray checkedItemPositions = l.getCheckedItemPositions();
            boolean isChecked = checkedItemPositions.get(mSaveBlockedMessagesCheckableItemPosition);
            new Settings(this).setSaveMessages(isChecked);
        }
        else if (itemID == R.id.vibrate)
        {
            SparseBooleanArray checkedItemPositions = l.getCheckedItemPositions();
            boolean isChecked = checkedItemPositions.get(mVibrateCheckableItemPosition);
            new Settings(this).setVibrate(isChecked);
        }
    }
    
    /* The add-on doesn't work properly on Android version 4.4 (and possibly
     * won't work on newer versions either).
     * Since Android 4.4, calling abortBroadcast() in our SMSReceiver.java no
     * longer has the desired effect (namely preventing the default SMS
     * application from receiving an SMS message that we wanted to block).
     * See the following links for more information:
     *     https://code.google.com/p/android/issues/detail?id=61684
     *     https://android-developers.blogspot.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
     */
    private void showAppMayNotWorkWarning()
    {
        SpannableString message = new SpannableString(getString(R.string.appMayNotWork));
        Linkify.addLinks(message, Linkify.WEB_URLS);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.sorry)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        if (messageView == null)
            Log.d(TAG, "Can't make link clickable. messageView == null");
        else
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
