package org.itishka.pointim.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import org.itishka.pointim.R;
import org.itishka.pointim.fragments.SinglePostFragment;


public class SinglePostActivity extends ConnectedActivity {

    public static final String EXTRA_POST = "post";
    public static final String EXTRA_COMMENT = "comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        if (savedInstanceState == null) {
            String post = getIntent().getStringExtra(EXTRA_POST);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, SinglePostFragment.newInstance(post))
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("#" + getIntent().getStringExtra(EXTRA_POST));
    }


}
