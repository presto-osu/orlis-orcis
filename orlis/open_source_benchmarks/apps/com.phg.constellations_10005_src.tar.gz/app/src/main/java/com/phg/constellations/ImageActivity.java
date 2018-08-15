package com.phg.constellations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class ImageActivity extends Activity {
    private static String TAG = "Constellations-app-Image";
    private static String constellation_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            constellation_name = bundle.getString("Constellation", constellation_name);
        }
        Log.d(TAG, "got name: " + constellation_name);
        setTitle(constellation_name);

        StrictMode.allowThreadDiskReads();
        String name = TextUtils.join("_",constellation_name.toLowerCase().split(" "));
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/" + name + ".svg");
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setInitialScale(100);
        webView.getSettings().setUseWideViewPort(true);
        StrictMode.enableDefaults();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_details) {
            Intent detailIntent = new Intent(getApplicationContext(),DetailsActivity.class);
            detailIntent.putExtra("Constellation",constellation_name);
            startActivity(detailIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
