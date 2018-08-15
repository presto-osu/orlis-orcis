/*
 * Copyright (C) 2015  Anthony Chomienne, anthony@mob-dev.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package fr.mobdev.goblim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fr.mobdev.goblim.Database;
import fr.mobdev.goblim.objects.Img;
import fr.mobdev.goblim.R;

/*
 * This Activity help user to find old shared pictures and re-share it if he wanted to
 */
public class HistoryActivity extends AppCompatActivity {

    private List<Img> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton new_image_button = (FloatingActionButton) findViewById(R.id.new_image_button);
        new_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newIntent = new Intent(HistoryActivity.this,UploadActivity.class);
                startActivity(newIntent);
            }
        });

        ListView historyList = (ListView) findViewById(R.id.history_list);

        updateHistory();

        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent linkIntent = new Intent(HistoryActivity.this,LinkActivity.class);
                if(position < images.size()) {
                    Img image = images.get(position);
                    linkIntent.putExtra("imageId", image.getId());
                    startActivity(linkIntent);
                }
            }

        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateHistory();
    }

    private void updateHistory()
    {
        ListView historyList = (ListView) findViewById(R.id.history_list);

        images = Database.getInstance(getApplicationContext()).getHistory();
        HistoryAdapter adapter = new HistoryAdapter(this,R.layout.history_item,R.id.url_history_item,images);
        historyList.setAdapter(adapter);
    }

    //Adapter to handle History Items
    private class HistoryAdapter extends ArrayAdapter<Img>
    {

        private List<Img> images;
        private LayoutInflater mInflater;
        int resource;

        public HistoryAdapter(Context context, int resource, int textViewResourceId, List<Img> objects) {
            super(context, resource, textViewResourceId, objects);
            images = new ArrayList<>(objects);
            this.resource = resource;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Img image = images.get(position);
            //reuse view or create a new one?
            if (convertView == null) {
                convertView = mInflater.inflate(resource, parent, false);
            }

            //display url
            TextView urlView = (TextView) convertView.findViewById(R.id.url_history_item);
            String url = image.getUrl();
            if(!url.endsWith("/"))
                url+="/";
            url+=image.getShortHash();
            urlView.setText(url);

            //display date
            TextView dateView = (TextView) convertView.findViewById(R.id.date);
            Calendar date = image.getDate();
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            df.setTimeZone(TimeZone.getDefault());
            String dateString = df.format(date.getTime());
            dateView.setText(dateString);

            //display durationLastDay
            TextView durationView = (TextView) convertView.findViewById(R.id.duration);
            int storageDuration = image.getStorageDuration();
            if(storageDuration == 0) {
                durationView.setText(getString(R.string.no_duration));
            }
            else {
                Calendar today = Calendar.getInstance();
                long millis = today.getTimeInMillis() - date.getTimeInMillis();
                long days = millis / (24*60*60*1000);
                //storage duration has ended or not?
                if(storageDuration - days < 0) {
                    durationView.setText(getString(R.string.duration_ended));
                }
                else {
                    if(storageDuration - days == 1)
                        durationView.setText(storageDuration - days +" "+ getString(R.string.day));
                    else
                        durationView.setText(storageDuration - days +" "+ getString(R.string.days));
                }
            }

            //Display miniatures if it exist
            ImageView thumb = (ImageView) convertView.findViewById(R.id.thumbnail);
            thumb.setImageBitmap(image.getThumb());

            return convertView;
        }
    }
}

