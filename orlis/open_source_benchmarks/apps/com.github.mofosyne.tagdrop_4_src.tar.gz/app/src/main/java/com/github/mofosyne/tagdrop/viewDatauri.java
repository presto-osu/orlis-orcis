package com.github.mofosyne.tagdrop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class viewDatauri extends AppCompatActivity {
    String datauriString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdatauri);

        /*
        *   This provides instant display of html based data uri content.
        *   For now, this sends all data uris to the html display.
        * */
        Intent iObj = getIntent();
        if ( iObj.getAction().equals("android.intent.action.VIEW") ) {
            // ------- Get the "data:" uri string
            datauriString = iObj.getDataString();

            // --------- Debug
            String debugText =
                    "DEBUG VIEW\n"
                            + "Action: '" + iObj.getAction() + "'\n"
                            + ", getDataString:'" + iObj.getDataString() + "'\n"
                            + ", getData:'" + iObj.getData() + "'\n"
                            + ", getScheme:'" + iObj.getScheme() + "'\n"
                            + ", SCAN_RESULT:'" + iObj.getStringExtra("SCAN_RESULT") + "'\n"
                            + ", SCAN_RESULT_FORMAT:'" + iObj.getStringExtra("SCAN_RESULT_FORMAT") + "'\n"
                            + ", SCAN_RESULT:'" + iObj.getStringExtra("SCAN_RESULT") + "'\n"
                            + ", SCAN_RESULT:'" + iObj.getStringExtra("SCAN_RESULT") + "'\n"
                    ;
            Log.d("incoming intent", debugText);

            // ------ Detect and extract content???
            /*
            *   For now. Nope, just push the datauri to browser. And let them deal with it. Much easier for me.
            * */

            // ------ webview
            WebView myWebView = (WebView) findViewById(R.id.htmldisp);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            // zoom support
            webSettings.setBuiltInZoomControls(true);
            webSettings.setSupportZoom(true);
            myWebView.loadUrl(datauriString);
            //myWebView.loadData();
        } else {
            Log.d("incoming intent", "non detected");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_html_view, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
