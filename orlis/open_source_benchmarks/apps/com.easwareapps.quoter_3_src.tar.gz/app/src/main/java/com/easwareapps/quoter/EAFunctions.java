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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class EAFunctions {


    public Uri saveBitmap(Bitmap bitmap, Context context){

        File cacheDir = new File(context.getExternalCacheDir(), "com.easwareapps.quoter");
        if(!cacheDir.exists()){
            if(!cacheDir.mkdirs()){
                Toast.makeText(context, "Sorry Can't make cache dir", Toast.LENGTH_LONG).show();
                return null;
            }
        }
        File filename = new File(cacheDir, "quoter" + System.currentTimeMillis() + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            filename.setReadable(true);
            bitmap.recycle();
            return Uri.fromFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;


    }


    public void shareImage(View view, Context context){
        Bitmap bm = getBitmap(view);
        Uri uri = saveBitmap(bm, context);
        shareIt(uri, getQuoteFromView(view), context);
    }

    public void shareIt(Uri uri, String data, Context context){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        share.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        share.setType("image/png");
        if(uri != null)
            share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "QUOTE");
        share.putExtra(Intent.EXTRA_TEXT, data + "\n\nDownload quoter from quoter.easwareapps.com");
        context.startActivity(share);
    }

    public Bitmap getBitmap(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
//        final Paint paint = new Paint();
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.WHITE);
//        canvas.drawRect(0, 0, view.getWidth(), canvas.getHeight(), paint);
        view.draw(canvas);
        return returnedBitmap;
    }

    private String getQuoteFromView(View v){
        return  ((TextView)v.findViewById(R.id.quote)).getText().toString() +
                ((TextView)v.findViewById(R.id.author)).getText().toString();
    }

    public Bitmap getRoundImage(Bitmap bmp){


        if(bmp == null){
            return  null;
        }
        try {
            Bitmap output = Bitmap.createBitmap(bmp.getWidth(),
                    bmp.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.parseColor("#BAB399"));
            canvas.drawCircle(bmp.getWidth() / 2 + 0.7f, bmp.getHeight() / 2 + 0.7f,
                    bmp.getWidth() / 2 + 0.1f, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bmp, rect, rect, paint);

            return output;
        }catch (Exception e){
            e.printStackTrace();
        }

        return  null;

    }



    public Bitmap createBitmapFromText(String quote, String author, float textSize, int textColor) {

        String gText = splitText(quote) + "\n\n\t - " + author;
        int width = (int)(42/2 * textSize);
        int height = (int)((gText.split("\n").length+2 + 0.7f) * textSize );
        Bitmap newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas newCanvas = new Canvas(newBitmap);

        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.WHITE);
        newCanvas.drawRect(0, 0, newCanvas.getWidth(), newCanvas.getHeight(), paintText);
        paintText.setStyle(Paint.Style.FILL_AND_STROKE);
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(textSize);
        //paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);

        Rect rectText = new Rect();
        for(int i=0;i<gText.split("\n").length;i++) {
            newCanvas.drawText(gText.split("\n")[i], 10, (i+1 + 0.7f)*textSize + 10, paintText);
        }

        return  newBitmap;
        // prepare canvas

    }


    public Bitmap combineImages(Bitmap bmp1, Bitmap bmp2) {

        int height = bmp1.getHeight();
        if (height < bmp2.getHeight())
            height = bmp2.getHeight();

        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth() + bmp2.getWidth(), height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmOverlay);
        Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setColor(Color.WHITE);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paintText);
        canvas.drawBitmap(bmp1, 0, 0, null);
        canvas.drawBitmap(bmp2, bmp1.getWidth(), 0, null);
        return bmOverlay;


    }

    public String splitText(String text){
        if(text.length() > 40){
            String words[] = text.split(" ");
            String splitted = "";
            String remaining = "";
            boolean flag = false;
            for (String word: words){
                if(!flag && (splitted + word).length() <= 42){
                    splitted += word + " ";
                }else{
                    flag = true;
                    remaining += word + " ";
                }
            }

            if(flag){
                return  splitted + "\n" + splitText(remaining);
            }
        }
        return text;

    }
    
    public int dpToPx(int dp, Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
		return px;
	}

    public Uri createAndSaveImageFromQuote(String quote, String author, Context appContext){

        String iconPath = author.replace(" ", "_");
        iconPath = iconPath.replace(".", "_");
        iconPath = iconPath.toLowerCase();

        Resources res = appContext.getResources();

        int r = res.getIdentifier(iconPath, "mipmap",
                appContext.getPackageName());

        int textSize = dpToPx(16, appContext);
        Bitmap quoteImage = createBitmapFromText(
                quote, author, textSize, Color.BLACK);

        Bitmap avatar = getRoundBitmap(res, r);


        Uri uri = null;
        try {
            Bitmap all = combineImages(avatar, quoteImage);
            uri = saveBitmap(all, appContext);
        }catch (Exception e){
            e.printStackTrace();
        }

        return uri;
    }

    public Bitmap getRoundBitmap(Resources res, int r){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 4;
        options.inJustDecodeBounds = false;
        return getRoundImage(BitmapFactory.decodeResource(res, r, options));

    }
}
