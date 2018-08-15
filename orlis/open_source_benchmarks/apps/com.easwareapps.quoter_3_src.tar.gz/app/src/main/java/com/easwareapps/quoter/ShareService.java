/**
 ************************************** ॐ ***********************************
 ***************************** लोकाः समस्ताः सुखिनो भवन्तु॥**************************
 * <p/>
 * Quoter is a Quotes collection with daily notification and widget
 * Copyright (C) 2016  vishnu
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.easwareapps.quoter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ShareService extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        int id = intent.getIntExtra("quote_id", -1);
        if(id!=-1) {
            String details[] = new DBHelper(context).getQuote(id);
            String authorName = details[0];
            String quoteText = details[1];

            Uri uri = new EAFunctions().createAndSaveImageFromQuote(quoteText, authorName,
                    context);

            new EAFunctions().shareIt(uri, quoteText + "\n\n\t - " + authorName,
                    context);
        }

        if(intent.getBooleanExtra("from_notification", false)){
            Intent closeDrawer = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeDrawer);
        }
    }

}
