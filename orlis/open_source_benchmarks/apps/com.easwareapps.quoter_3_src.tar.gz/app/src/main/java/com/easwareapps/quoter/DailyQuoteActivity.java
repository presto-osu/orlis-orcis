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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;




public class DailyQuoteActivity extends AppCompatActivity{

    ImageView avatar;
    TextView author;
    TextView quote;
    Bitmap nullImage;
    LruCache<String, Bitmap> cache;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.daily_quote);

        avatar = (ImageView)findViewById(R.id.avatar);
        quote = (TextView)findViewById(R.id.quote);
        author = (TextView)findViewById(R.id.author);

        nullImage = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);

        final int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory/8;
        cache = new LruCache<String, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(String key, Bitmap value) {
                // TODO Auto-generated method stub

                return value.getRowBytes() - value.getHeight();

            }

        };


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if(this.getIntent().getBooleanExtra("from_quote", false)) {
            toolbar.setTitle("Random Quote");

        }else{
            toolbar.setTitle("Daily Quote");
        }
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        int qid = -1;
		try{
			qid = Integer.parseInt(getIntent().getAction());
		}catch(Exception e){
			qid = -1;
		}
		if(qid!=-1){
			//qid = getIntent().getIntExtra("quote_id", -1);
            setQuote(qid);
        }else if(getIntent().getIntExtra("quote_id", -1) > 0){
            qid = getIntent().getIntExtra("quote_id", -1);
            if(getIntent().getBooleanExtra("dismiss_notification", false)){
				new EANotificationManager().clearNotification(getApplicationContext());
			}
            setQuote(qid);

        }else {
            setRandomQuote();
        }
        View v = (View)avatar.getParent();
        v.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new EAFunctions().shareImage(v, getApplicationContext());
                return false;
            }
        });



    }

    private void shareImage(String quote){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        paint.setTextSize(16);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.MONOSPACE);

        Bitmap mybitmap = Bitmap.createBitmap(100, 16, Bitmap.Config.ALPHA_8);
        Canvas c = new Canvas(mybitmap);
        c.drawText(quote , 0, 16, paint);
    }

    private void setRandomQuote(){

        DBHelper db = new DBHelper(getApplicationContext());
        String details[] = db.getRandomQuote();
        db.close();
        setContent(details);
    }

    private void setQuote(int id){
        DBHelper db = new DBHelper(getApplicationContext());
        String details[] = db.getQuote(id);
        db.close();
        setContent(details);
    }
    private void setContent(String[] details){
        String authorName = details[0];
        String quoteText = details[1];
        String avatarRes = authorName;

        avatarRes = avatarRes.replace(" ", "_");
        avatarRes = avatarRes.replace(".", "_");
        avatarRes = avatarRes.toLowerCase();

        quote.setText(quoteText + "\n  ");
        author.setText(" -  " + authorName);


        Context context = getApplicationContext();
        Resources res = context.getResources();
        EABitmapManager bm = new EABitmapManager(avatar, res, cache);
        bm.setContext(context);
        final EABitmapManager.AsyncDrawable asyncDrawable =
                new EABitmapManager.AsyncDrawable(res, nullImage, bm);
        int r = context.getResources().getIdentifier(avatarRes, "mipmap",
                context.getPackageName());
        avatar.setImageDrawable(asyncDrawable);
        bm.execute(r);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //(Toolbar)findViewById(R.id.toolbar).crea
        getMenuInflater().inflate(R.menu.random, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.random){
            setRandomQuote();
        }
        return super.onOptionsItemSelected(item);
    }


}
