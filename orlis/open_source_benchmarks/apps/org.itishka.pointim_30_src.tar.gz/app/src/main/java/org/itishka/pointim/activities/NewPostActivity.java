package org.itishka.pointim.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import org.itishka.pointim.R;
import org.itishka.pointim.fragments.NewPostFragment;


public class NewPostActivity extends ConnectedActivity {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_TAGS = "tags";
    public static final String EXTRA_PRIVATE = "private";
    public static final String EXTRA_RESULT_POST = "post";
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private boolean mPermissionRequestOk = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i : grantResults) {
            if (i == PackageManager.PERMISSION_DENIED) {
                return;
            }
        }
        mPermissionRequestOk = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (savedInstanceState == null) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            }
            setupFragment();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPermissionRequestOk)
            setupFragment();
    }

    private void setupFragment() {
        Fragment fragment = null;
        mPermissionRequestOk = false;
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d("NPA", "type: " + type);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                fragment = NewPostFragment.newInstance(intent.getStringExtra(Intent.EXTRA_TEXT));
            } else if (type.startsWith("image/")) {
                fragment = NewPostFragment.newInstance((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM), type);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                fragment = NewPostFragment.newInstance(intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM), type);
            }
        } else {
            String id = getIntent().getStringExtra(EXTRA_ID);
            String text = getIntent().getStringExtra(EXTRA_TEXT);
            String[] tags = getIntent().getStringArrayExtra(EXTRA_TAGS);
            boolean isPrivate = getIntent().getBooleanExtra(EXTRA_PRIVATE, false);
            if (!TextUtils.isEmpty(id)) {
                fragment = NewPostFragment.newInstanceForEdit(id, text, tags, isPrivate);
            } else {
                fragment = NewPostFragment.newInstance(isPrivate);
            }
            if (!TextUtils.isEmpty(id)) {
                getSupportActionBar().setTitle("#" + id);
            }
        }
        if (fragment == null)
            fragment = NewPostFragment.newInstance(false);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
