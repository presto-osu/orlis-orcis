/*
    This file is part of the Diaspora Native WebApp.

    Diaspora Native WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Diaspora Native WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Diaspora Native WebApp.

    If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.tristeslostrestigres.diasporanativewebapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.tristeslostrestigres.diasporanativewebapp.utils.Helpers;

public class ShareActivity extends MainActivity {

    private WebView webView;
    private static final String TAG = "Diaspora Share";
    private String podDomain;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private com.getbase.floatingactionbutton.FloatingActionsMenu fab;
    private TextView txtTitle;
    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        txtTitle = (TextView) findViewById(R.id.toolbar_title);
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helpers.isOnline(ShareActivity.this)) {
                    txtTitle.setText(R.string.jb_stream);
                    Intent i = new Intent(ShareActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    Snackbar.make(v, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        SharedPreferences config = getSharedPreferences("PodSettings", MODE_PRIVATE);
        podDomain = config.getString("podDomain", null);

        fab = (com.getbase.floatingactionbutton.FloatingActionsMenu) findViewById(R.id.multiple_actions);
        fab.setVisibility(View.GONE);

        webView = (WebView)findViewById(R.id.webView);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        WebSettings wSettings = webView.getSettings();
        wSettings.setJavaScriptEnabled(true);
        wSettings.setBuiltInZoomControls(true);

        if (Build.VERSION.SDK_INT >= 21)
            wSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        /*
         * WebViewClient
         */
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, url);
                if (!url.contains(podDomain)) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                return false;

            }

            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Finished loading URL: " + url);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e(TAG, "Error: " + description);

                new AlertDialog.Builder(ShareActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(description)
                        .setPositiveButton("CLOSE", null)
                        .show();
            }
        });


        /*
         * WebChromeClient
         */
        webView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView wv, int progress) {
                progressBar.setProgress(progress);

                if (progress > 0 && progress <= 60) {
                    Helpers.getNotificationCount(wv);
                }

                if (progress > 60) {
                    Helpers.hideTopBar(wv);
                }

                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);

                mFilePathCallback = filePathCallback;

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Snackbar.make(getWindow().findViewById(R.id.drawer), "Unable to get image", Snackbar.LENGTH_SHORT).show();
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");

                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });




        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            webView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {

                    if (extras.containsKey(Intent.EXTRA_TEXT) && extras.containsKey(Intent.EXTRA_SUBJECT)) {
                        final String extraText = (String) extras.get(Intent.EXTRA_TEXT);
                        final String extraSubject = (String) extras.get(Intent.EXTRA_SUBJECT);

                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                                finish();

                                Toast.makeText(ShareActivity.this, R.string.please_reload, Toast.LENGTH_SHORT).show();

                                Intent i = new Intent(ShareActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);

                                return false;
                            }
                        });

                        webView.loadUrl("javascript:(function() { " +
                                "document.getElementsByTagName('textarea')[0].style.height='110px'; " +
                                "document.getElementsByTagName('textarea')[0].innerHTML = '[" + extraSubject + "](" + extraText + ") #ViaDiasporaNativeWebApp'; " +
                                "    if(document.getElementById(\"main_nav\")) {" +
                                "        document.getElementById(\"main_nav\").parentNode.removeChild(" +
                                "        document.getElementById(\"main_nav\"));" +
                                "    } else if (document.getElementById(\"main-nav\")) {" +
                                "        document.getElementById(\"main-nav\").parentNode.removeChild(" +
                                "        document.getElementById(\"main-nav\"));" +
                                "    }" +
                                "})();");

                    }
                }
            });
        }

        if (savedInstanceState == null) {
            if (Helpers.isOnline(ShareActivity.this)) {
                webView.loadUrl("https://"+podDomain+"/status_messages/new");
            } else {
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.reload) {
            if (Helpers.isOnline(ShareActivity.this)) {
                webView.reload();
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
