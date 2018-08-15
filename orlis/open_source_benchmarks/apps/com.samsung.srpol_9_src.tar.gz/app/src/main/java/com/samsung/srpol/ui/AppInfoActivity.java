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
import com.samsung.srpol.data.Subcategory;
import com.samsung.srpol.loader.AppDetails;
import com.samsung.srpol.loader.AppListLoader;
import com.samsung.srpol.loader.AppListLoader.OnAppRemoveListener;
import com.samsung.srpol.utils.Utils;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppInfoActivity extends ActionBarActivity implements OnAppRemoveListener {
    public static final String APP_PACKAGE_NAME = "APP_PACKAGE_NAME";
    
    private AppDetails mAppDetails;
    private String mPackageName = null;
    private ThreatsArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        Intent intent = getIntent();
        if (intent != null) {
            mPackageName = intent
                    .getStringExtra(AppInfoActivity.APP_PACKAGE_NAME);
        }
        mAppDetails = AppListLoader.getAppDetails(mPackageName);
        if(mAppDetails == null){
            return;
        }
        ImageView imageView = (ImageView) findViewById(R.id.app_icon);
        imageView.setImageDrawable(mAppDetails.getAppIcon());
        TextView textView = (TextView) findViewById(R.id.app_name);
        textView.setText(mAppDetails.getAppName());

        grayoutText(textView);

        ListView listView = (ListView) findViewById(R.id.threats_list);
        mAdapter = new ThreatsArrayAdapter(getApplicationContext(),
                AppListLoader.getSubcategoriesOfMask(mAppDetails
                        .getSubcategoriesMask()));
        AnimationAdapter adapter = new AlphaInAnimationAdapter(mAdapter);
        adapter.setAbsListView(listView);
        listView.setAdapter(adapter);

        ImageButton uninstallButton = (ImageButton) findViewById(R.id.uninstall_button);
        ImageButton moreInfoButton = (ImageButton) findViewById(R.id.more_info_button);
        TextView systemAppTextView = (TextView) findViewById(R.id.system_app_text);
        View activityLayout = findViewById(R.id.container);
        if (mAppDetails.isSystemApp()) {
            moreInfoButton.setVisibility(View.VISIBLE);
            systemAppTextView.setVisibility(View.VISIBLE);
            uninstallButton
                    .setImageResource(R.drawable.app_info_settings_button);
            uninstallButton.setContentDescription(getResources().getText(R.string.app_detail_settings_button));
            activityLayout.setBackgroundResource(R.color.grayout_list_item_bg);
        } else {
            moreInfoButton.setVisibility(View.GONE);
            systemAppTextView.setVisibility(View.GONE);
            uninstallButton
                    .setImageResource(R.drawable.app_info_uninstall_button);
            uninstallButton.setContentDescription(getResources().getText(R.string.app_detail_uninstall_button));
            activityLayout.setBackgroundResource(R.color.app_detail_bg);
        }

        uninstallButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.showInstalledAppDetails(getApplicationContext(), mPackageName);
            }
        });
        moreInfoButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Utils.startBrowser(getApplicationContext(), getResources().getString(R.string.app_info_more_info));
            }
        });
        AppListLoader.setOnChangeListener(this);
    }

    @Override
    protected void onResume() {
        ImageView systemAppIcon = (ImageView) findViewById(R.id.list_system_app_icon);
        if (mAppDetails.isSystemApp()) {
            systemAppIcon.setVisibility(View.VISIBLE);
            if (mAppDetails.isEnabled()) {
                systemAppIcon.setImageDrawable(Utils.getmSystemIcon(this));
            } else {
                systemAppIcon.setImageDrawable(Utils
                        .getmSystemIconDisable(this));
            }
        } else {
            systemAppIcon.setVisibility(View.INVISIBLE);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        AppListLoader.setOnChangeListener(null);
        super.onDestroy();
    }

    class ThreatsArrayAdapter extends ArrayAdapter<Subcategory> {

        public ThreatsArrayAdapter(Context context, List<Subcategory> resources) {
            super(context, R.layout.threats_list_item, resources);
        }

        class ViewHolder {
            public TextView textView;
            public ImageView imageView;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.threats_list_item,
                        parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView
                        .findViewById(R.id.threat_icon);
                holder.textView = (TextView) convertView
                        .findViewById(R.id.threat_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Subcategory subgroupContainer = getItem(position);
            if (subgroupContainer != null) {
                holder.imageView
                        .setImageDrawable(subgroupContainer.getDarkIcon());
                holder.textView.setText(subgroupContainer.getDescription());
            }
            return convertView;
        }
    }

    private void grayoutText(TextView textView){
     // disabling and enabling apps in system application manager is
        // available since API 4.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (mAppDetails.isEnabled()) {
                textView.setTextColor(getResources().getColor(
                        android.R.color.black));
            } else {
                textView.setTextColor(getResources().getColor(
                        R.color.disabled_app_name));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onPackageRemoved(String packageName) {
        if (mPackageName.equals(packageName)) {
            finish();
        }
    }

}
