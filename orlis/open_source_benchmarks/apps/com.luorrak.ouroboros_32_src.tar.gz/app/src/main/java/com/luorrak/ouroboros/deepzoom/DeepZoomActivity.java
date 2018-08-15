package com.luorrak.ouroboros.deepzoom;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
 */

public class DeepZoomActivity extends AppCompatActivity{
    private ArrayList<Media> mediaList;
    private String fileName;
    private String resto;
    private String boardName;
    private InfiniteDbHelper infiniteDbHelper;
    private DeepzoomViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, SettingsHelper.getTheme(this));
        // TODO: 2/12/16 figure out something better to do here besides making it black
        if (Build.VERSION.SDK_INT >= 21){
            getWindow().setStatusBarColor(getResources().getColor(R.color.md_black_1000));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deepzoom);
        Ion.getDefault(getApplicationContext()).getCache().setMaxSize(150 * 1024 * 1024);
        infiniteDbHelper = new InfiniteDbHelper(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fileName = getIntent().getStringExtra(Util.TIM);
        resto = getIntent().getStringExtra(Util.INTENT_THREAD_NO);
        boardName = getIntent().getStringExtra(Util.INTENT_BOARD_NAME);

        newMediaListInstance(infiniteDbHelper, resto);
        int selectedMediaItem = findMediaItemIndex(fileName);

        mPager = (DeepzoomViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(selectedMediaItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new DeepZoomFragment().newInstance(boardName, resto, position);
        }

        @Override
        public int getCount() {
            return mediaList.size();
        }
    }

    private int findMediaItemIndex(String fileName){
        for (int i = 0; i < mediaList.size(); i++){
            if (mediaList.get(i).fileName.equals(fileName)){
                return i;
            }
        }
        return 0;
    }

    public void newMediaListInstance(InfiniteDbHelper infiniteDbHelper, String resto){
        if (mediaList == null){
            mediaList = new ArrayList<Media>();
            Cursor cursor = infiniteDbHelper.getThreadCursor(resto);
            do {
                byte[] serializedPostMedia = cursor.getBlob(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_MEDIA_FILES));
                if(serializedPostMedia != null){
                    mediaList.addAll((Collection<? extends Media>) Util.deserializeObject(serializedPostMedia));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    public Media getMediaItem(int position){
        return mediaList.get(position);
    }
}
