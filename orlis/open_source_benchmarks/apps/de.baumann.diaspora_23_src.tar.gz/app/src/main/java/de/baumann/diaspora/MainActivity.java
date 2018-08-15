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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.baumann.diaspora.utils.Helpers;
import de.baumann.diaspora.utils.PrefManager;
import de.baumann.diaspora.utils.SoftKeyboardStateWatcher;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String URL_MESSAGE = "URL_MESSAGE";

    private AppSettings appSettings;
    private final Handler myHandler = new Handler();
    private WebView webView;
    private String podDomain;
    private Menu menu;
    private int notificationCount = 0;
    private int conversationCount = 0;
    private String profileId = "";
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private com.getbase.floatingactionbutton.FloatingActionsMenu fab;
    private TextView txtTitle;
    private ProgressBar progressBar;
    private WebSettings wSettings;
    private PrefManager pm;
    private SwipeRefreshLayout swipeView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkFirstRun();

        if (android.os.Build.VERSION.SDK_INT >= 21)
            WebView.enableSlowWholeDocumentDraw();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/stream");
                    setTitle(R.string.jb_stream);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Keyboard State Watcher
        final SoftKeyboardStateWatcher softKeyboardStateWatcher
                = new SoftKeyboardStateWatcher(findViewById(R.id.swipe));

        softKeyboardStateWatcher.addSoftKeyboardStateListener(new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                fab.setVisibility(View.GONE);
            }

            @Override
            public void onSoftKeyboardClosed() {
                fab.setVisibility(View.VISIBLE);
            }
        });


        // Load app settings
        appSettings = new AppSettings(getApplicationContext());
        profileId = appSettings.getProfileId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        pm = new PrefManager(MainActivity.this);

        SharedPreferences config = getSharedPreferences("PodSettings", MODE_PRIVATE);
        podDomain = config.getString("podDomain", null);

        fab = (com.getbase.floatingactionbutton.FloatingActionsMenu) findViewById(R.id.multiple_actions);
        fab.setVisibility(View.GONE);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeView.setColorSchemeResources(R.color.colorPrimary,
                R.color.fab_big);

        webView = (WebView) findViewById(R.id.webView);
        webView.addJavascriptInterface(new JavaScriptInterface(), "AndroidBridge");

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
                swipeView.setRefreshing(false);
            }
        });

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.reload();
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
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
                    Helpers.getProfileId(wv);
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
                        Snackbar.make(swipeView, R.string.image, Snackbar.LENGTH_LONG).show();
                        return false;
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
        });


        if (savedInstanceState == null) {
            if (Helpers.isOnline(MainActivity.this)) {
                webView.loadData("", "text/html", null);
                webView.loadUrl("https://" + podDomain);
            } else {
                Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
            }
        }

    }

    /*
     * Fab button events
     */

    public void fab1_click(View v) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(MainActivity.this)
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
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
        }
        fab.collapse();
        if (Helpers.isOnline(MainActivity.this)) {
            webView.loadUrl("https://" + podDomain + "/status_messages/new");
            setTitle(R.string.fab1_title);
        } else {
            Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    public void fab2_click(View v) {
        fab.collapse();
        if (Helpers.isOnline(MainActivity.this)) {
            final EditText input = new EditText(this);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setView(input)
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.search_alert_title)
                    .setPositiveButton(R.string.search_alert_people, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String inputTag = input.getText().toString().trim();
                            String cleanTag = inputTag.replaceAll("\\*", "");
                            // this validate the input data for tagfind
                            if (cleanTag == null || cleanTag.equals("")) {
                                dialog.cancel(); // if user don�t have added a tag
                                Snackbar.make(swipeView, R.string.search_alert_bypeople_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                            } else { // User have added a search tag
                                webView.loadUrl("https://" + podDomain + "/people.mobile?q=" + cleanTag);
                                setTitle(R.string.fab2_title_person);
                            }
                        }
                    })
                    .setNegativeButton(R.string.search_alert_tag,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String inputTag = input.getText().toString().trim();
                                    String cleanTag = inputTag.replaceAll("\\#", "");
                                    // this validate the input data for tagfind
                                    if (cleanTag == null || cleanTag.equals("")) {
                                        dialog.cancel(); // if user hasn't added a tag
                                        Snackbar.make(swipeView, R.string.search_alert_bytags_validate_needsomedata, Snackbar.LENGTH_LONG).show();
                                    } else { // User have added a search tag
                                        webView.loadUrl("https://" + podDomain + "/tags/" + cleanTag);
                                        setTitle(R.string.fab2_title_tag);
                                    }
                                }
                            });
            dialog.show();
        } else {
            Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
        }
    }

    public void fab3_click(View v) {
        fab.collapse();
        webView.scrollTo(0, 0);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                if (mCameraPhotoPath != null) {
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
            setTitle(R.string.app_name);
        } else {
            Snackbar snackbar = Snackbar
                    .make(swipeView, R.string.confirm_exit, Snackbar.LENGTH_LONG)
                    .setAction(R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            moveTaskToBack(true);
                        }
                    });
            snackbar.show();
        }
    }

    private final BroadcastReceiver brLoadUrl = new BroadcastReceiver() {

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
        MenuItem itemNotification = menu.findItem(R.id.action_notifications);
        if (itemNotification != null) {
            if (notificationCount > 0) {
                itemNotification.setIcon(R.drawable.ic_bell_ring_white_24dp);
            } else {
                itemNotification.setIcon(R.drawable.ic_bell_outline_white_24dp);
            }

            MenuItem itemConversation = menu.findItem(R.id.action_conversations);
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
        switch (item.getItemId()) {
            case R.id.action_notifications: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/notifications");
                    setTitle(R.string.jb_notifications);
                    return true;
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                    return false;
                }
            }

            case R.id.action_conversations: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/conversations");
                    setTitle(R.string.jb_conversations);
                    return true;
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                    return false;
                }
            }

            case R.id.action_exit: {
                moveTaskToBack(true);
            }
            break;

            case R.id.action_share: {
                final CharSequence[] options = {getString(R.string.share_link), getString(R.string.share_screenshot), getString(R.string.take_screenshot)};
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals(getString(R.string.share_link))) {
                                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                    sharingIntent.setType("image/png");
                                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
                                    sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                                }
                                if (options[item].equals(getString(R.string.share_screenshot))) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                                        int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                        if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                                            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                new AlertDialog.Builder(MainActivity.this)
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
                                                return;
                                            }
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                            return;
                                        }
                                    }
                                    Snackbar.make(swipeView, R.string.toast_screenshot, Snackbar.LENGTH_LONG).show();
                                    File directory = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/");
                                    if (!directory.exists()) {
                                        directory.mkdirs();
                                    }
                                    Date date = new Date();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
                                    Picture picture = webView.capturePicture();
                                    Bitmap b = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas c = new Canvas(b);
                                    File screen = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/"
                                            + dateFormat.format(date) + ".jpg");
                                    if (screen.exists())
                                        screen.delete();
                                    picture.draw(c);
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(screen);
                                        if (fos != null) {
                                            b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                            fos.close();
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
                                    }
                                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                    sharingIntent.setType("image/png");
                                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
                                    sharingIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                                    Uri bmpUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/"
                                            + dateFormat.format(date) + ".jpg"));
                                    sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                                    File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/"
                                            + dateFormat.format(date) + ".jpg");
                                    Uri uri = Uri.fromFile(file);
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                                    sendBroadcast(intent);
                                }
                                if (options[item].equals(getString(R.string.take_screenshot))) {
                                    if (android.os.Build.VERSION.SDK_INT >= 23) {
                                        int hasWRITE_EXTERNAL_STORAGE = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                        if (hasWRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                                            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                                new AlertDialog.Builder(MainActivity.this)
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
                                                return;
                                            }
                                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    REQUEST_CODE_ASK_PERMISSIONS);
                                            return;
                                        }
                                    }
                                    Snackbar.make(swipeView, R.string.toast_screenshot, Snackbar.LENGTH_LONG).show();
                                    File directory = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/");
                                    if (!directory.exists()) {
                                        directory.mkdirs();
                                    }
                                    Date date = new Date();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
                                    Picture picture = webView.capturePicture();
                                    Bitmap b = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas c = new Canvas(b);
                                    File screen = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/"
                                            + dateFormat.format(date) + ".jpg");
                                    if (screen.exists())
                                        screen.delete();
                                    picture.draw(c);
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(screen);
                                        if (fos != null) {
                                            b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                                            fos.close();
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
                                    }
                                    File file = new File(Environment.getExternalStorageDirectory() + "/Pictures/Diaspora/"
                                            + dateFormat.format(date) + ".jpg");
                                    Uri uri = Uri.fromFile(file);
                                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                                    sendBroadcast(intent);
                                }
                            }
                        }).show();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void alertFormElements() {

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
                            Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    }
                }).show();
    }

    public class JavaScriptInterface {
        @JavascriptInterface
        public void setNotificationCount(final String webMessage) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    notificationCount = Integer.valueOf(webMessage);

                    MenuItem item = menu.findItem(R.id.action_notifications);

                    if (item != null) {
                        if (notificationCount > 0) {
                            item.setIcon(R.drawable.ic_bell_ring_white_24dp);
                            Snackbar snackbar = Snackbar
                                    .make(swipeView, R.string.new_notifications, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.yes, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (Helpers.isOnline(MainActivity.this)) {
                                                webView.loadUrl("https://" + podDomain + "/notifications");
                                                setTitle(R.string.jb_notifications);
                                            } else {
                                                Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                            snackbar.show();
                        } else {
                            item.setIcon(R.drawable.ic_bell_outline_white_24dp);
                        }
                    }


                }
            });
        }

        @JavascriptInterface
        public void setProfileId(final String webMessage) {
            if(profileId.equals("") || !profileId.equals(webMessage)) {
                profileId = webMessage;
                appSettings.setProfileId(profileId);
            }
        }



            @JavascriptInterface
        public void setConversationCount(final String webMessage) {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    conversationCount = Integer.valueOf(webMessage);

                    MenuItem item = menu.findItem(R.id.action_conversations);

                    if (item != null) {
                        if (conversationCount > 0) {
                            item.setIcon(R.drawable.ic_message_text_white_24dp);
                            Snackbar snackbar = Snackbar
                                    .make(swipeView, R.string.new_conversations, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.yes, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (Helpers.isOnline(MainActivity.this)) {
                                                webView.loadUrl("https://" + podDomain + "/conversations");
                                                setTitle(R.string.jb_notifications);
                                            } else {
                                                Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                            snackbar.show();
                        } else {
                            item.setIcon(R.drawable.ic_message_text_outline_white_24dp);
                        }
                    }

                }
            });
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_stream: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/stream");
                    setTitle(R.string.jb_stream);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_profile: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/people/" + profileId);
                    setTitle(R.string.jb_profile);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            // TODO followed_tags currently not implemented as single viewable page (0.5.7.1-paf04894e, 2016 March 20)
            case R.id.nav_followed_tags: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/followed_tags");
                    setTitle(R.string.jb_followed_tags);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_aspects: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/aspects");
                    setTitle(R.string.jb_aspects);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_activities: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/activity");
                    setTitle(R.string.jb_activities);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_liked: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/liked");
                    setTitle(R.string.jb_liked);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_commented: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/commented");
                    setTitle(R.string.jb_commented);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_mentions: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/mentions");
                    setTitle(R.string.jb_mentions);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_public: {
                if (Helpers.isOnline(MainActivity.this)) {
                    webView.loadUrl("https://" + podDomain + "/public");
                    setTitle(R.string.jb_public);
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_settings_view: {
                final CharSequence[] options = {getString(R.string.settings_font), getString(R.string.settings_view), getString(R.string.settings_image)};
                if (Helpers.isOnline(MainActivity.this)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (options[item].equals(getString(R.string.settings_font)))
                                        alertFormElements();
                                    if (options[item].equals(getString(R.string.settings_view)))
                                        webView.loadUrl("https://" + podDomain + "/mobile/toggle");
                                    if (options[item].equals(getString(R.string.settings_image)))
                                        wSettings.setLoadsImagesAutomatically(!pm.getLoadImages());
                                    pm.setLoadImages(!pm.getLoadImages());
                                    webView.loadUrl(webView.getUrl());
                                }
                            }).show();
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_settings_diaspora: {
                final CharSequence[] options2 = {getString(R.string.jb_settings), getString(R.string.jb_manage_tags),
                        getString(R.string.jb_contacts), getString(R.string.jb_pod)};
                if (Helpers.isOnline(MainActivity.this)) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setItems(options2, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (options2[item].equals(getString(R.string.jb_settings)))
                                        webView.loadUrl("https://" + podDomain + "/user/edit");
                                    if (options2[item].equals(getString(R.string.jb_manage_tags)))
                                        webView.loadUrl("https://" + podDomain + "/tag_followings/manage");
                                    if (options2[item].equals(getString(R.string.jb_contacts)))
                                        webView.loadUrl("https://" + podDomain + "/contacts");
                                    if (options2[item].equals(getString(R.string.jb_pod)))
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
                                }
                            }).show();
                } else {
                    Snackbar.make(swipeView, R.string.no_internet, Snackbar.LENGTH_LONG).show();
                }
            }
            break;

            case R.id.nav_license_help: {
                final CharSequence[] options = {getString(R.string.help_license), getString(R.string.help_about), getString(R.string.help_help), getString(R.string.help_donate)};
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (options[item].equals(getString(R.string.help_license))) {
                                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.license_text)));
                                    Linkify.addLinks(s, Linkify.WEB_URLS);
                                    final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.license_title)
                                            .setMessage(s)
                                            .setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    }).show();
                                    d.show();
                                    ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                }
                                if (options[item].equals(getString(R.string.help_about))) {
                                    final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.about_text)));
                                    Linkify.addLinks(s, Linkify.WEB_URLS);
                                    final AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.help_about)
                                            .setMessage(s)
                                            .setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    }).show();
                                    d.show();
                                    ((TextView) d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                                }
                                if (options[item].equals(getString(R.string.help_help))) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.help_help)
                                            .setMessage(Html.fromHtml(getString(R.string.markdown_text)))
                                            .setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    }).show();
                                }
                                if (options[item].equals(getString(R.string.help_donate))) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage(getString(R.string.donate_text))
                                            .setPositiveButton(getString(R.string.yes),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    })
                                            .setNegativeButton(getString(R.string.donate_1),
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://martinv.tip.me/"));
                                                            startActivity(i);
                                                            dialog.cancel();
                                                        }
                                                    }).show();
                                }
                            }
                        }).show();
            }
            break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstBrowser", true);
        if (isFirstRun){
            // Place your dialog code here to display the dialog
            final SpannableString s = new SpannableString(Html.fromHtml(getString(R.string.about2_text)));
            Linkify.addLinks(s, Linkify.WEB_URLS);

            final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage(s)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(R.string.notagain, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("firstBrowser", false)
                                    .apply();
                        }
                    });
            dialog.show();
        }
    }

}