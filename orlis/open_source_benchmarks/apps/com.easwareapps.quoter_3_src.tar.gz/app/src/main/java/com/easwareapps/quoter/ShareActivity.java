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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;



public class ShareActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        int id = intent.getIntExtra("quote_id", -1);
        if(id!=-1) {
            String details[] = new DBHelper(getApplicationContext()).getQuote(id);
            String authorName = details[0];
            String quoteText = details[1];

            Uri uri = new EAFunctions().createAndSaveImageFromQuote(quoteText, authorName,
                    getApplicationContext());

            new EAFunctions().shareIt(uri, quoteText + "\n\n\t - " + authorName,
                    getApplicationContext());
        }else{
            String details[] = new DBHelper(getApplicationContext()).getRandomQuote();
            String authorName = details[0];
            String quoteText = details[1];

            Uri uri = new EAFunctions().createAndSaveImageFromQuote(quoteText, authorName,
                    getApplicationContext());

            new EAFunctions().shareIt(uri, quoteText + "\n\n\t - " + authorName,
                    getApplicationContext());
        }

        finish();
    }

    @Override
    protected void onResume() {
        finish();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
