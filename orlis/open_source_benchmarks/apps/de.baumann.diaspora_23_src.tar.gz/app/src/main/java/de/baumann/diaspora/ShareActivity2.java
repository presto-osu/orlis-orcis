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

package de.baumann.diaspora;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.baumann.diaspora.utils.Helpers;

public class ShareActivity2 extends MainActivity {

    private WebView webView;
    private static final String TAG = "Diaspora Share";
    private String podDomain;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private com.getbase.floatingactionbutton.FloatingActionsMenu fab;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeView;

    static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeView.setColorSchemeResources(R.color.colorPrimary,
                R.color.fab_big);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Helpers.isOnline(ShareActivity2.this)) {
                    Intent intent = new Intent(ShareActivity2.this, MainActivity.class);
                    startActivityForResult(intent, 100);
                    overridePendingTransition(0, 0);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE).show();
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

                new AlertDialog.Builder(ShareActivity2.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(description)
                        .setPositiveButton("CLOSE", null)
                        .show();
            }
        });

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Helpers.isOnline(ShareActivity2.this)) {
                    webView.reload();
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_INDEFINITE).show();
                    swipeView.setRefreshing(false);
                }
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

                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            new AlertDialog.Builder(ShareActivity2.this)
                                    .setMessage(R.string.permissions)
                                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (android.os.Build.VERSION.SDK_INT >= 23)
                                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                        REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.no), null)
                                    .show();
                            return (true);
                        }
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                        return (true);
                    }
                }

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
                        Snackbar.make(getWindow().findViewById(R.id.drawer_layout), "Unable to get image", Snackbar.LENGTH_SHORT).show();
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

                                Intent i = new Intent(ShareActivity2.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                overridePendingTransition(0, 0);

                                return false;
                            }
                        });

                        webView.loadUrl("javascript:(function() { " +
                                "document.getElementsByTagName('textarea')[0].style.height='110px'; " +
                                "document.getElementsByTagName('textarea')[0].innerHTML = '**[" + extraSubject + "]** " + extraText + " *[shared with #DiasporaWebApp]*'; " +
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
            if (Helpers.isOnline(ShareActivity2.this)) {
                webView.loadUrl("https://"+podDomain+"/status_messages/new");
            } else {
                Snackbar.make(getWindow().findViewById(R.id.drawer_layout), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if(resultCode == Activity.RESULT_OK) {
            if(data == null) {
                if(mCameraPhotoPath != null) {
                    results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
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

        if (id == R.id.action_reload) {
            if (Helpers.isOnline(ShareActivity2.this)) {
                webView.reload();
                return true;
            } else {
                Snackbar.make(getWindow().findViewById(R.id.drawer_layout), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fab.collapse();
        if (webView.canGoBack()) {
            webView.goBack();
            setTitle(R.string.app_name);
            Snackbar snackbar = Snackbar
                    .make(swipeView, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                    .setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            moveTaskToBack(true);
                        }
                    });
            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar
                    .make(swipeView, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                    .setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });
            snackbar.show();
        }
    }

}