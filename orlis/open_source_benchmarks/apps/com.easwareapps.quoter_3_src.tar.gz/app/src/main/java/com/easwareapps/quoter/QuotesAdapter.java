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
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QuotesAdapter extends RecyclerView.Adapter<QuotesAdapter.ViewHolder>{

    Context context = null;
    Cursor cursor;
    boolean isDataValid;
    Bitmap nullImage;
    LruCache<String, Bitmap> cache;
    QuoterActivity activity = null;
    int lastSelected = -1;
    public QuotesAdapter(Context context, Cursor cursor, QuoterActivity activity) {
        this.context = context;
        this.cursor = cursor;
        this.activity = activity;
        cursor.registerDataSetObserver(new NotifyingDataSetObserver());

        nullImage = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory/8;
        cache = new LruCache<String, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(String key, Bitmap value) {
                // TODO Auto-generated method stub

                return value.getRowBytes() - value.getHeight();

            }

        };


    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView avatar = null;
        TextView quoute = null;
        TextView author = null;
        View mainView;
        public ViewHolder(View view){
            super(view);
            mainView = view;
            avatar = (ImageView)view.findViewById(R.id.avatar);
            quoute = (TextView)view.findViewById(R.id.quote);
            author = (TextView)view.findViewById(R.id.author);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quote, parent, false);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                shareImage(view);

                return false;
            }
        });
        return new ViewHolder(view);
    }






    public void shareImage(View view){
        Bitmap bm = getBitmap(view);
        Uri uri = saveBitmap(bm);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.setType("image/png");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(Intent.EXTRA_SUBJECT, "QUOTE");
        share.putExtra(Intent.EXTRA_TEXT, getQuoteFromView(view) + "\n\nDownload quoter from quoter.easwareapps.com");
        context.startActivity(share);
    }

    private String getQuoteFromView(View v){
        return  ((TextView)v.findViewById(R.id.quote)).getText().toString() +
                ((TextView)v.findViewById(R.id.author)).getText().toString();
    }




    private Uri saveBitmap(Bitmap bitmap){

        File cacheDir = new File(context.getExternalCacheDir(), "com.easwareapps.quoter");
        if(!cacheDir.exists()){
            if(!cacheDir.mkdirs()){
                Toast.makeText(context, "Sorry Can't make cache dir", Toast.LENGTH_LONG).show();
                return null;
            }
        }
        String name = "quoter" + System.currentTimeMillis();
        File filename = new File(cacheDir, name + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            filename.setReadable(true);
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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //avatar.setImageDrawable(Bitmap.createBitmap());
        cursor.moveToPosition(position);
        String authorName = cursor.getString(1);
        holder.quoute.setText(cursor.getString(0) + "\n  " );
        holder.author.setText(" -  " + authorName);
        int author = cursor.getInt(2);

        final String imageKey = String.valueOf(author);
        final Bitmap bitmap = cache.get(imageKey);
        authorName = authorName.replace(" ", "_");
        authorName = authorName.replace(".", "_");
        authorName = authorName.toLowerCase();
        if (bitmap != null) {
            holder.avatar.setImageBitmap(bitmap);
        } else {
            try {
                Resources res = context.getResources();
                EABitmapManager bm = new EABitmapManager(holder.avatar, res, cache);
                bm.setContext(context);
                final EABitmapManager.AsyncDrawable asyncDrawable =
                        new EABitmapManager.AsyncDrawable(res, nullImage, bm);
                int r = context.getResources().getIdentifier(authorName, "mipmap",
                        context.getPackageName());
                holder.avatar.setImageDrawable(asyncDrawable);
                bm.execute(r);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = inflater.inflate(R.layout.quote, parent);
        }
        ImageView avatar = (ImageView)convertView.findViewById(R.id.avatar);
        TextView quote = (TextView)convertView.findViewById(R.id.quote);

        //avatar.setImageDrawable(Bitmap.createBitmap());
        quote.setText("Test");
        return super.getView(position, convertView, parent);
    }




    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView avatar = (ImageView)view.findViewById(R.id.avatar);
        TextView quote = (TextView)view.findViewById(R.id.quote);
        //avatar.setImageDrawable(Bitmap.createBitmap());
        quote.setText("Test");
        quote.setTextColor(Color.RED);
    }
*/

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            isDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            isDataValid = false;
            notifyDataSetChanged();
            //There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }

    private Bitmap getBitmap(View view){
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

}
