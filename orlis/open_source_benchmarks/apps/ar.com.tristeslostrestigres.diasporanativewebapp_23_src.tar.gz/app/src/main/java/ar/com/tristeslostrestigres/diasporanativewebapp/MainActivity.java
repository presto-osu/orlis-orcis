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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.tristeslostrestigres.diasporanativewebapp.utils.Helpers;
import ar.com.tristeslostrestigres.diasporanativewebapp.utils.PrefManager;

public class MainActivity extends AppCompatActivity {

    private static final String URL_MESSAGE = "URL_MESSAGE";
    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    final Handler myHandler = new Handler();
    WebView webView;
    static final String TAG = "Diaspora Main";
    String podDomain;
    Menu menu;
    int notificationCount = 0;
    int conversationCount = 0;
    ValueCallback<Uri[]> mFilePathCallback;
    String mCameraPhotoPath;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    com.getbase.floatingactionbutton.FloatingActionsMenu fab;
    TextView txtTitle;
    ProgressBar progressBar;
    WebSettings wSettings;
    PrefManager pm;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        pm = new PrefManager(MainActivity.this);

        SharedPreferences config = getSharedPreferences("PodSettings", MODE_PRIVATE);
        podDomain = config.getString("podDomain", null);

        fab = (com.getbase.floatingactionbutton.FloatingActionsMenu) findViewById(R.id.multiple_actions);
        fab.setVisibility(View.GONE);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        txtTitle = (TextView) findViewById(R.id.toolbar_title);
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Helpers.isOnline(MainActivity.this)) {
                    txtTitle.setText(R.string.jb_stream);
                    webView.loadUrl("https://" + podDomain + "/stream");
                } else {  
                    Snackbar.make(v, R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                }
            }
        });


        webView = (WebView)findViewById(R.id.webView);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.addJavascriptInterface(new JavaScriptInterface(), "NotificationCounter");

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }

        wSettings = webView.getSettings();
        wSettings.setJavaScriptEnabled(true);
        wSettings.setUseWideViewPort(true);
        wSettings.setLoadWithOverviewMode(true);
        wSettings.setDomStorageEnabled(true);
        wSettings.setMinimumFontSize(pm.getMinimumFontSize());
        wSettings.setLoadsImagesAutomatically(pm.getLoadImages());

        if (android.os.Build.VERSION.SDK_INT >= 21)
            wSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        /*
         * WebViewClient
         */
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.contains(podDomain)) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                if (url.contains("/new") || url.contains("/sign_in")) {
                    fab.setVisibility(View.GONE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                new AlertDialog.Builder(MainActivity.this)
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
                    fab.setVisibility(View.VISIBLE);
                }

                if (progress == 100) {
                    fab.collapse();
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


        /*
         * NavigationView
         */
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()){
                    default:
                        Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                        return true;

                    case R.id.jb_stream:
                        if (Helpers.isOnline(MainActivity.this)) {
                            txtTitle.setText(R.string.jb_stream);
                            webView.loadUrl("https://" + podDomain + "/stream");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_public:
                        setTitle(R.string.jb_public);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/public");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_liked:
                        txtTitle.setText(R.string.jb_liked);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/liked");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_commented:
                        txtTitle.setText(R.string.jb_commented);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://"+podDomain+"/commented");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_contacts:
                        txtTitle.setText(R.string.jb_contacts);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/contacts");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_mentions:
                        txtTitle.setText(R.string.jb_mentions);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/mentions");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_activities:
                        txtTitle.setText(R.string.jb_activities);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://"+podDomain+"/activity");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_followed_tags:
                        txtTitle.setText(R.string.jb_followed_tags);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/followed_tags");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_manage_tags:

                        txtTitle.setText(R.string.jb_manage_tags);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/tag_followings/manage");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }


                    case R.id.jb_license:
                        txtTitle.setText(R.string.jb_license);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.license_title))
                                .setMessage(getString(R.string.license_text))
                                .setPositiveButton(getString(R.string.license_yes),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/martinchodev/Diaspora-Native-WebApp"));
                                                startActivity(i);
                                                dialog.cancel();
                                            }
                                        })
                                .setNegativeButton(getString(R.string.license_no), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                }).show();

                        return true;

                    case R.id.jb_aspects:
                        txtTitle.setText(R.string.jb_aspects);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/aspects");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }

                    case R.id.jb_settings:
                        txtTitle.setText(R.string.jb_settings);
                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl("https://" + podDomain + "/user/edit");
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();

                            return false;
                        }

                    case R.id.jb_pod:
                        txtTitle.setText(R.string.jb_pod);
                        if (Helpers.isOnline(MainActivity.this)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(getString(R.string.confirmation))
                                    .setMessage(getString(R.string.change_pod_warning))
                                    .setPositiveButton(getString(R.string.yes),
                                            new DialogInterface.OnClickListener() {
                                                @TargetApi(11)
                                                public void onClick(DialogInterface dialog, int id) {
                                                    webView.clearCache(true);
                                                    dialog.cancel();
                                                    Intent i = new Intent(MainActivity.this, PodsActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            })
                                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                        @TargetApi(11)
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).show();
                            return true;
                        } else {  
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                            return false;
                        }


                }
            }
        });


        /*
         * DrawerLayout
         */
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        if (savedInstanceState == null) {
            if (Helpers.isOnline(MainActivity.this)) {
                webView.loadData("", "text/html", null);
                webView.loadUrl("https://"+podDomain);
            } else {
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
            }
        }

    }




    /*
     * Fab button events
     */
    public void fab_top_click(View v){
        fab.collapse();
        webView.scrollTo(0, 70);
    }

    public void fab_compose_click(View v){
        fab.collapse();
        if (Helpers.isOnline(MainActivity.this)) {
            txtTitle.setText(R.string.fab3_title);
            webView.loadUrl("https://" + podDomain + "/status_messages/new");
        } else {  
            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void fab_exit_click(View v){
        fab.collapse();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.confirm_exit))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webView.clearCache(true);
                        finish();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }


    private File createImageFile() throws IOException {
        // Create an image file name
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(brLoadUrl, new IntentFilter(URL_MESSAGE));
    }

    @Override
    public void onBackPressed() {
        fab.collapse();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getString(R.string.confirm_exit))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            webView.clearCache(true);
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }


    private BroadcastReceiver brLoadUrl = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String url = intent.getStringExtra("url");
            txtTitle.setText(R.string.app_name);
            webView.loadUrl(url);

        }
    };

    @Override
    protected void onPause() {
        unregisterReceiver(brLoadUrl);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuItem itemNotification = menu.findItem(R.id.notifications);
        if (itemNotification != null) {
            if (notificationCount > 0) {
                itemNotification.setIcon(R.drawable.ic_bell_ring_white_24dp);
            } else {
                itemNotification.setIcon(R.drawable.ic_bell_outline_white_24dp);
            }

            MenuItem itemConversation = menu.findItem(R.id.conversations);
            if (conversationCount > 0) {
                itemConversation.setIcon(R.drawable.ic_message_text_white_24dp);
            } else {
                itemConversation.setIcon(R.drawable.ic_message_text_outline_white_24dp);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.notifications) {

            if (Helpers.isOnline(MainActivity.this)) {

//                webView.stopLoading();
//
//                WebView wvNotifications = new WebView(MainActivity.this);
//                wvNotifications.loadUrl("https://" + podDomain + "/notifications");
//
//                final AlertDialog d = new AlertDialog.Builder(MainActivity.this).setView(wvNotifications)
//                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
//                            @TargetApi(11)
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        }).show();
//
////                wvNotifications.setWebChromeClient(new WebChromeClient() {
////
////                   public void onProgressChanged(WebView view, int progress) {
////                       progressBar.setProgress(progress);
//
////                       if (progress > 0 && progress <= 60) {
////                           view.loadUrl("javascript: ( function() {" +
////                                   "    if (document.getElementById('notification')) {" +
////                                   "       var count = document.getElementById('notification').innerHTML;" +
////                                   "       NotificationCounter.setNotificationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
////                                   "    } else {" +
////                                   "       NotificationCounter.setNotificationCount('0');" +
////                                   "    }" +
////                                   "    if (document.getElementById('conversation')) {" +
////                                   "       var count = document.getElementById('conversation').innerHTML;" +
////                                   "       NotificationCounter.setConversationCount(count.replace(/(\\r\\n|\\n|\\r)/gm, \"\"));" +
////                                   "    } else {" +
////                                   "       NotificationCounter.setConversationCount('0');" +
////                                   "    }" +
////                                   "})();");
////                       }
//
////                       if (progress > 60) {
////                           view.loadUrl("javascript: ( function() {" +
////                                   "    if(document.getElementById('main_nav')) {" +
////                                   "        document.getElementById('main_nav').parentNode.removeChild(" +
////                                   "        document.getElementById('main_nav'));" +
////                                   "    } else if (document.getElementById('main-nav')) {" +
////                                   "        document.getElementById('main-nav').parentNode.removeChild(" +
////                                   "        document.getElementById('main-nav'));" +
////                                   "    }" +
////                                   "})();");
//////                           fab.setVisibility(View.VISIBLE);
////                       }
//
////                       if (progress == 100) {
////                           fab.collapse();
////                           progressBar.setVisibility(View.GONE);
////                       } else {
////                           progressBar.setVisibility(View.VISIBLE);
////                       }
////                   }
////               });
//
//                        wvNotifications.setWebViewClient(new WebViewClient() {
//                            @Override
//                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                                if (!url.equals("https://" + podDomain + "/notifications")) {
//                                    Intent urlIntent = new Intent(MainActivity.URL_MESSAGE);
//                                    urlIntent.putExtra("url", url);
//                                    sendBroadcast(urlIntent);
//                                }
//                                d.dismiss();
//                                return true;
//                            }
//                        });

                webView.loadUrl("https://" + podDomain + "/notifications");
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        if (id == R.id.conversations) {
            if (Helpers.isOnline(MainActivity.this)) {
                webView.loadUrl("https://" + podDomain + "/conversations");
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }


        if (id == R.id.search) {
            fab.collapse();
            if (Helpers.isOnline(MainActivity.this)) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setTitle(R.string.search_alert_title);
                alert.setPositiveButton(R.string.search_alert_people, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String inputTag = input.getText().toString().trim();
                        String cleanTag = inputTag.replaceAll("\\*", "");
                        // this validate the input data for tagfind
                        if (cleanTag == null || cleanTag.equals("")) {
                            dialog.cancel(); // if user don�t have added a tag
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.search_alert_bypeople_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                        } else { // User have added a search tag
                            txtTitle.setText(R.string.fab1_title_person);
                            webView.loadUrl("https://" + podDomain + "/people.mobile?q=" + cleanTag);
                        }
                    }
                }).setNegativeButton(R.string.search_alert_tag,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputTag = input.getText().toString().trim();
                                String cleanTag = inputTag.replaceAll("\\#", "");
                                // this validate the input data for tagfind
                                if (cleanTag == null || cleanTag.equals("")) {
                                    dialog.cancel(); // if user hasn't added a tag
                                    Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.search_alert_bytags_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                                } else { // User have added a search tag
                                    txtTitle.setText(R.string.fab1_title_tag);
                                    webView.loadUrl("https://" + podDomain + "/tags/" + cleanTag);
                                }
                            }
                        });
                alert.show();
            }
        }

        if (id == R.id.reload) {
            if (Helpers.isOnline(MainActivity.this)) {
                webView.reload();
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }


        if (id == R.id.mobile) {
            if (Helpers.isOnline(MainActivity.this)) {
                webView.loadUrl("https://" + podDomain + "/mobile/toggle");
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        if (id == R.id.loadImg) {
            if (Helpers.isOnline(MainActivity.this)) {
                wSettings.setLoadsImagesAutomatically(!pm.getLoadImages());
                pm.setLoadImages(!pm.getLoadImages());
                webView.loadUrl(webView.getUrl());
                return true;
            } else {  
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        if (id == R.id.fontSize) {
            if (Helpers.isOnline(MainActivity.this)) {
                alertFormElements();
                return true;
            } else {
                Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }



    public void alertFormElements() {

    /*
     * Inflate the XML view. activity_main is in
     * res/layout/form_elements.xml
     */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.font_size_chooser,
                null, false);

        final RadioGroup rgFontSize = (RadioGroup) formElementsView
                .findViewById(R.id.genderRadioGroup);

        // the alert dialog
        new AlertDialog.Builder(MainActivity.this).setView(formElementsView)
                .setTitle("Set Font Size")
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @TargetApi(11)
                    public void onClick(DialogInterface dialog, int id) {

                        int selectedId = rgFontSize
                                .getCheckedRadioButtonId();

                        // find the radiobutton by returned id
                        RadioButton selectedRadioButton = (RadioButton) formElementsView
                                .findViewById(selectedId);

                        if (selectedRadioButton.getId() == R.id.radNormal) {
                            pm.setMinimumFontSize(8);
                        } else if (selectedRadioButton.getId() == R.id.radLarge) {
                            pm.setMinimumFontSize(16);
                        } else if (selectedRadioButton.getId() == R.id.radLarger) {
                            pm.setMinimumFontSize(20);
                        }

                        wSettings.setMinimumFontSize(pm.getMinimumFontSize());

                        if (Helpers.isOnline(MainActivity.this)) {
                            webView.loadUrl(webView.getUrl());
                        } else {
                            Snackbar.make(getWindow().findViewById(R.id.drawer), R.string.no_internet, Snackbar.LENGTH_LONG).show();
                        }


                        dialog.cancel();
                    }
                }).show();
    }

    public class JavaScriptInterface {
        @JavascriptInterface
        public void setNotificationCount(final String webMessage){
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    notificationCount = Integer.valueOf(webMessage);

                    MenuItem item = menu.findItem(R.id.notifications);

                    if (item != null) {
                        if (notificationCount > 0) {
                            item.setIcon(R.drawable.ic_bell_ring_white_24dp);
                        } else {
                            item.setIcon(R.drawable.ic_bell_outline_white_24dp);
                        }
                    }


                }
            });
        }

        @JavascriptInterface
        public void setConversationCount(final String webMessage){
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    conversationCount = Integer.valueOf(webMessage);

                    MenuItem item = menu.findItem(R.id.conversations);

                    if (item != null) {
                        if (conversationCount > 0) {
                            item.setIcon(R.drawable.ic_message_text_white_24dp);
                        } else {
                            item.setIcon(R.drawable.ic_message_text_outline_white_24dp);
                        }
                    }

                }
            });
        }

    }

}

