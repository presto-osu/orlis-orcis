/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.ui;

import java.util.List;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.samsung.srpol.R;
import com.samsung.srpol.data.Category;
import com.samsung.srpol.data.Subcategory;
import com.samsung.srpol.loader.AppListLoader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PopupActivity extends ActionBarActivity {

    public static final String POPUP_CATEGORY = "POPUP_CATEGORY";

    private Category mCategory;
    private ThreatsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        int categotyId = 0;
        Intent intent = getIntent();
        if (intent != null) {
            categotyId = intent.getIntExtra(POPUP_CATEGORY, -1);
        }

        TextView header = (TextView) findViewById(R.id.header_text);
        List<Category> containerList = AppListLoader.getCategories();
        if (containerList != null && categotyId >= 0) {
            mCategory = containerList.get(categotyId);
            header.setText(getString(R.string.apps_that)
                    + mCategory.getHeader());
        }

        ListView listview = (ListView) findViewById(R.id.threats_list);
        mAdapter = new ThreatsAdapter(this);
        for (Subcategory subCat : mCategory.getSubCategories())
            mAdapter.add(subCat);
        AnimationAdapter adapter = new AlphaInAnimationAdapter(mAdapter);
        adapter.setAbsListView(listview);
        listview.setAdapter(adapter);

        ImageButton back = (ImageButton) findViewById(R.id.back_button);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        ImageButton more = (ImageButton) findViewById(R.id.moreinfo_button);
        more.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse(mCategory.getLink()));
                startActivity(intent);
            }
        });
    }

    private class ThreatsAdapter extends ArrayAdapter<Subcategory> {

        private class ViewHolder {
            ImageView icon;
            TextView description;
        }

        public ThreatsAdapter(Context context) {
            super(context, R.layout.threats_list_item);
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.threats_list_item,
                        parent, false);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView
                        .findViewById(R.id.threat_icon);
                holder.description = (TextView) convertView
                        .findViewById(R.id.threat_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Subcategory item = getItem(position);

            holder.icon.setImageDrawable(item.getIconDrawable());
            holder.description.setText(item.getDescription());
            holder.description.setTextColor(getResources().getColor(
                    android.R.color.white));

            return convertView;
        }
    }

}
