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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;


public class QuoteProvider implements RemoteViewsService.RemoteViewsFactory {

    Context context =null;

    public QuoteProvider(Context context) {
        this.context = context;

    }


    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.widget);

        DBHelper db = new DBHelper(context);
        String details[] = db.getRandomQuote();
        String authorName = details[0];
        String quoteText = details[1];
        int qid = Integer.parseInt(details[3]);
        String avatarRes = authorName;

        avatarRes = avatarRes.replace(" ", "_");
        avatarRes = avatarRes.replace(".", "_");
        avatarRes = avatarRes.toLowerCase();

        remoteView.setTextViewText(R.id.quote, quoteText);
        remoteView.setTextViewText(R.id.author, " -  " + authorName);



        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 4;
        options.inJustDecodeBounds = false;

        Resources res = context.getResources();
        int id = res.getIdentifier(avatarRes, "mipmap",
                context.getPackageName());
        Bitmap avatar = BitmapFactory.decodeResource(res, id, options);
        avatar = new EAFunctions().getRoundImage(avatar);
        remoteView.setImageViewBitmap(R.id.avatar, avatar);
        //remoteView.setImageViewResource(R.id.avatar, id);


        db.close();


        Intent intent = new Intent(context, ShareService.class);
        intent.putExtra("quote_id", qid);
        intent.setAction("" + qid);

        Intent updateWidgetIntent = new Intent(context, QuoterWidget.class);
        updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        Intent viewIntent = new Intent(context, DailyQuoteActivity.class);
        viewIntent.putExtra("quote_id", qid);
        viewIntent.setAction("" + qid);


        int[] ids = {position};
        updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);


        PendingIntent piShare = PendingIntent.getBroadcast(context, 0,intent, 0);
        PendingIntent piView = PendingIntent.getActivity(context, 0,viewIntent, 0);
        PendingIntent piNextQuote = PendingIntent.getBroadcast(context, 0,updateWidgetIntent, 0);



        remoteView.setOnClickPendingIntent(R.id.widget, piView);
        remoteView.setOnClickPendingIntent(R.id.share, piShare);
        remoteView.setOnClickPendingIntent(R.id.next_quote, piNextQuote);



        return remoteView;
    }






    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDataSetChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }



}
