package io.github.phora.androptpb.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import io.github.phora.androptpb.R;

public class UploadOptionsActivity extends Activity {

    public final static String EXTRA_IS_PRIVATE = "EXTRA_IS_PRIVATE";
    public final static String EXTRA_VANITY = "EXTRA_VANITY";
    public final static String EXTRA_SUNSET = "EXTRA_SUNSET";
    public static final String EXTRA_RAW_TEXT = "EXTRA_RAW_TEXT";

    private Switch mIsPrivate;
    private Switch mHasVanity;
    private EditText mVanityUrl;
    private EditText mSunset;

    private ClipData clippy;
    private Uri data;
    private String raw_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIsDark()) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_options);

        mIsPrivate = (Switch)findViewById(R.id.Upload_IsPrivate);
        mHasVanity = (Switch)findViewById(R.id.Upload_HasVanity);
        mVanityUrl = (EditText)findViewById(R.id.Upload_VanityAlias);
        mSunset = (EditText)findViewById(R.id.Upload_Sunset);

        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getClipData() != null) {
                clippy = getIntent().getClipData();
                if (clippy.getItemCount() > 1) {
                    mHasVanity.setEnabled(false);
                }
            }
            else if (intent.getData() != null) {
                data = getIntent().getData();
            }
            else if (intent.hasExtra(EXTRA_RAW_TEXT)) {
                raw_text = getIntent().getStringExtra(EXTRA_RAW_TEXT);
            }
        }

        mHasVanity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVanityUrl.setEnabled(mHasVanity.isChecked());
                if(mHasVanity.isChecked()) {
                    mVanityUrl.setVisibility(View.VISIBLE);
                }
                else {
                    mVanityUrl.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private boolean getIsDark() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isdark = preferences.getBoolean("isDark", false);
        return isdark;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    //http://stackoverflow.com/questions/920306/sending-data-back-to-the-main-activity-in-android
    public void finishSubmission(View view) {
        Intent output = new Intent();
        String sunset = mSunset.getText().toString();
        if (mHasVanity.isChecked()) {
            output.putExtra(EXTRA_VANITY, mVanityUrl.getText().toString());
        }
        if (!TextUtils.isEmpty(sunset)) {
            long ttl = 0;
            String[] items = sunset.split(":");
            switch (items.length) {
                case 1:
                    ttl = Long.valueOf(items[0])*60;
                    break;
                case 2:
                    ttl = Long.valueOf(items[0])*3600+Long.valueOf(items[1])*60;
                    break;
                case 3:
                    ttl = Long.valueOf(items[0])*86400+Long.valueOf(items[1])*3600+Long.valueOf(items[2])*60;
                    break;
            }
            output.putExtra(EXTRA_SUNSET, ttl);
        }
        if (clippy != null) {
            output.setClipData(clippy);
        }
        else if (data != null) {
            output.setData(data);
        }
        else if (raw_text != null) {
            output.putExtra(EXTRA_RAW_TEXT, raw_text);
        }
        output.putExtra(EXTRA_IS_PRIVATE, mIsPrivate.isChecked());
        setResult(RESULT_OK, output);
        finish();
    }

    public void cancelSubmission(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
