package org.itishka.pointim.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import org.itishka.pointim.R;
import org.itishka.pointim.fragments.TagViewFragment;


public class TagViewActivity extends ConnectedActivity {

    public static final String EXTRA_USER = "user";
    public static final String EXTRA_TAG = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        String user = getIntent().getStringExtra(EXTRA_USER);
        String tag = getIntent().getStringExtra(EXTRA_TAG);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, TagViewFragment.newInstance(user, tag))
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (TextUtils.isEmpty(user))
            getSupportActionBar().setTitle("*" + tag);
        else
            getSupportActionBar().setTitle("@" + user + ": *" + tag);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tag_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

}
