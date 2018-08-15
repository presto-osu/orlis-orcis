package com.github.mofosyne.tagdrop;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class main extends AppCompatActivity {
    //

    //
    TextView debugDisp;

    // Related to structured append of seqences of QR codes
    boolean structuredAppendDetected;
    int currentSeqNum;
    int totalInSeqNum;
    String hashtype;
    String hashcheck;
    // Related to string based structured appends
    String datauriString; // Stores the full data uri string to parse
    // todo: Related to binary based string append.

    String[] contentArray; //  Stores sequences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On load, clear everything
        clear();

        /*
        Intent iObj = getIntent();
        if ( iObj.getAction().equals("android.intent.action.VIEW") ) {
            datauriString = iObj.getDataString();
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
            debugDisp = (TextView) findViewById(R.id.debugView);
            debugDisp.setText( debugText );
            Log.d("incoming intent", debugText);

            //------ webview
            WebView myWebView = (WebView) findViewById(R.id.webdisp);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            //myWebView.loadData();
            myWebView.loadUrl(datauriString);
        } else {
            Log.d("incoming intent", "non detected");
        }
        */

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
        if (id == R.id.action_readme) {
            startActivity( new Intent(this, ReadMe.class ));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------

    public void launchContent(View v) { // You must have "View v" or app will crash
        openWebPage(datauriString);
    }

    // Does this work to parse dataUri??? I wonder... ANS: NOOOPE... this just replaces "data:" with "http:"
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    //--------------------------------------------------------------------------
    /*
    *   For zxing loading
    * */

    static int QR_READER_ACTIVITY_REQUEST_CODE=0;

    public void scanButton(View v) { // You must have "View v" or app will crash
        launchBarcodeReader("Scan first code. Press back/return to escape. ");
    }

    public void launchBarcodeReader( String promptMessage ) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        intent.putExtra("SCAN_FORMATS", "DATA_MATRIX,QR_CODE,MAXICODE,PDF_417,AZTEC");
        intent.putExtra("PROMPT_MESSAGE", promptMessage );
        startActivityForResult(intent, QR_READER_ACTIVITY_REQUEST_CODE);
    }

    public void clearButton(View v) { // You must have "View v" or app will crash
        clear();
    }



    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == QR_READER_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                String bytecount = intent.getStringExtra("SCAN_RESULT_BYTES");
                String orientation = intent.getStringExtra("SCAN_RESULT_ORIENTATION");
                String eccLvl = intent.getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");
                String strExtList = intent.getExtras().toString();
                // Handle successful scan
                String debugText =
                        "DEBUG VIEW\n"
                                + "Content: '" + datauriString + "'\n\n"
                                + ", format:'" + format + "'\n\n"
                                + ", bytecount:'" + bytecount + "'\n\n"
                                + ", orientation:'" + orientation + "'\n\n"
                                + ", eccLvl:'" + eccLvl + "'\n\n"
                                + ", Other Strings Extras:'" + strExtList + "'\n\n"
                        ; // Can we get structured append metadata here?
                debugDisp = (TextView) findViewById(R.id.debugView);
                debugDisp.setText( debugText );
                Log.d("incoming intent", debugText);

                // Save and append if required
                if(structuredAppendDetected) {
                    //todo: logic on smart addition of qr content (Then we could auto launch when all is received.
                    contentArray[currentSeqNum] = contents;
                } else {
                    if (false) {
                        //todo: would be good to autodetect datauris and respond as if it was a new sequence
                    } else {
                        // Dumb append mode
                        contentArray[currentSeqNum] = contents;
                        currentSeqNum++;
                        totalInSeqNum++;

                        // Ask for next sequence
                        launchBarcodeReader("Scanning Next Sequence. Press back/return to escape. ");
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
            }
        }

        // Update Preview Screen
        datauriString = TextUtils.join( "", contentArray ); // todo: need to concat all the arrays together
        debugDisp = (TextView) findViewById(R.id.debugView);
        debugDisp.setText( datauriString );
    }

    //-------------------------------------------------------
    public void clear(){
        structuredAppendDetected = false;
        currentSeqNum = 0;
        totalInSeqNum = 1;
        hashtype = "none";
        hashcheck = "";
        datauriString = "";
        contentArray = new String[50]; // For now, just hardcode the amount to 256, which is more than enough for anyone
        for(int i = 0; i < contentArray.length; i++){
            contentArray[i]="";
        }
        // Don't forget to reset the display
        debugDisp = (TextView) findViewById(R.id.debugView);
        debugDisp.setText( "" );
    }
}
