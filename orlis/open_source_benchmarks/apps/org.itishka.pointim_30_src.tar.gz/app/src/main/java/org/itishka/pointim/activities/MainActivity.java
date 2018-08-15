package org.itishka.pointim.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.astuetz.PagerSlidingTabStrip;

import org.itishka.pointim.R;
import org.itishka.pointim.fragments.AllFragment;
import org.itishka.pointim.fragments.CommentedFragment;
import org.itishka.pointim.fragments.RecentFragment;
import org.itishka.pointim.fragments.SelfFragment;
import org.itishka.pointim.utils.Utils;

public class MainActivity extends ConnectedActivity {

    private static final int REQUEST_NEW_POST = 13;
    private FloatingActionButton mNewPost;
    private ViewPager mPager;
    public static final String EXTRA_TARGET = "target";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Utils.showPostSentSnack(this, mPager, data.getStringExtra(NewPostActivity.EXTRA_RESULT_POST));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        //TODO switch tabs
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        mNewPost = (FloatingActionButton) findViewById(R.id.new_post);
        mNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, NewPostActivity.class), REQUEST_NEW_POST);
            }
        });

        // Initialize the ViewPager and set an adapter
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(4);
        mPager.setAdapter(new ScreenSlidePagerAdapter(this, getSupportFragmentManager()));
        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_bookmarks) {
            startActivity(new Intent(this, BookmarksActivity.class));

        } else if (id == R.id.action_mailbox) {
            startActivity(new Intent(this, MailboxActivity.class));
        } else if (id == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final String[] titles;

        public ScreenSlidePagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            titles = new String[]{
                    context.getString(R.string.tab_recent),
                    context.getString(R.string.tab_commented),
                    context.getString(R.string.tab_blog),
                    context.getString(R.string.tab_all),
            };
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) return new RecentFragment();
            if (position == 1) return new CommentedFragment();
            if (position == 2) return new SelfFragment();
            if (position == 3) return new AllFragment();
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }
}
