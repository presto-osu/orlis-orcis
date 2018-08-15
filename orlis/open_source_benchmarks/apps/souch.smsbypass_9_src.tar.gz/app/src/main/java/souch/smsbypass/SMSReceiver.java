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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver
{
    private static final String TAG = "SMSReceiver";

    private boolean addressMatchContact(String contact, ArrayList<String> contacts)
    {
        for(String c : contacts) {
            if (contact.equalsIgnoreCase(c))
                return true;
        }
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        // NOTE:
        // One can receive multiple 'SmsMessage' objects, but they should have
        // the same originating address. In other words, it should be safe to
        // rely on the originating address of the first 'SmsMessage' to detect
        // whether this SMS should be filtered.
        Object[] pdus = (Object[]) extras.get("pdus");
        if (pdus.length == 0)
            return;

        SmsMessage first_message = SmsMessage.createFromPdu((byte[]) pdus[0]);
        String address = first_message.getDisplayOriginatingAddress();

        StringBuilder stringBuilder = new StringBuilder().append(first_message.getMessageBody());
        for (int i = 1; i < pdus.length; i++)
        {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) pdus[i]);
            stringBuilder.append(message.getMessageBody());
        }
        String fullMessageBody = stringBuilder.toString();

        String filterName = shouldBlockMessage(context, address, fullMessageBody);
        if (filterName != null)
        {
            Log.d(TAG, "Aborting SMS from [" + address + "].");

            abortBroadcast();

            Settings settings = new Settings(context);
            if (settings.saveMessages())
            {
                settings.saveMessage(filterName, address, first_message.getTimestampMillis(),
                        Message.MSG_TYPE_RECEIVED, fullMessageBody);
                Log.d(TAG, "Saved blocked SMS from [" + address + "].");

                if (settings.getVibrate())
                    vibrate(context);
            }
        }
    }

    public final long vibrateTime = 30;
    private void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(vibrateTime);
    }

    // if should block return filter name, otherwise returns null
    private String shouldBlockMessage(Context context,
                                       String incomingAddress,
                                       String fullMessageBody)
    {
        Settings settings = new Settings(context);
        List<Filter> filters = settings.getFilters();
        ArrayList<String> contacts = settings.getContacts(incomingAddress);
        for (Filter filter : filters)
        {
            if (PhoneNumberUtils.compare(filter.address, incomingAddress) ||
                    filter.address.equals(Settings.ANY_ADDRESS) ||
                    addressMatchContact(filter.address, contacts))
            {
                if (!contentFiltersMatch(filter.contentFilters, fullMessageBody))
                {
                    // There may be more filters for the same originating
                    // address. We have to check them all before knowing
                    // whether we should block the message.
                    continue;
                }
                return filter.name;
            }
        }
        return null;
    }

    private boolean contentFiltersMatch(List<String> contentFilters, String message)
    {
        for (String contentFilter : contentFilters)
        {
            if (!message.contains(contentFilter))
            {
                Log.d(TAG, "Content filter [" + contentFilter + "] not found, skipping it.");
                return false;
            }
        }
        return true;
    }
}
