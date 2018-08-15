package com.alexcruz.papuhwalls;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alexcruz.papuhwalls.Live.GridAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.mrengineer13.snackbar.SnackBar;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.lang.ref.WeakReference;

import okio.BufferedSink;
import okio.Okio;


public class WallsFragment extends ActionBarActivity {

    private Toolbar toolbar;
    public String wall;
    private String saveWallLocation, picName, livePicName, dialogContent;
    private File destWallFile;
    private View fabBg;
    private Activity context;
    Preferences Preferences;

    private FloatingActionsMenu fab;

    private static final int ACTIVITY_SHARE = 13452;
    private boolean isAddedToLWList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.Preferences = new Preferences(getApplicationContext());
        setContentView(R.layout.wallpaper_activity);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_ab_detailed_wallpaper);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fab = (FloatingActionsMenu) findViewById(R.id.wall_actions);

        fabBg = findViewById(R.id.fabBg);
        fabBg.setVisibility(View.GONE);

        saveWallLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + context.getResources().getString(R.string.walls_save_location);
        picName = context.getResources().getString(R.string.walls_prefix_name);
        livePicName = context.getResources().getString(R.string.live_walls_prefix_name);

        dialogContent = getResources().getString(R.string.download_done) + " " + saveWallLocation;

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isfirstrun", true);

        if (isFirstRun) {

            File folder = new File(saveWallLocation);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                    .putBoolean("isfirstrun", false).commit();

        }

        final WallpaperManager wm = WallpaperManager.getInstance(context);

        setFullScreen();

        scan(this, "external");

        final ImageView image = (ImageView) findViewById(R.id.bigwall);
        wall = getIntent().getStringExtra("wall");
        destWallFile = new File(saveWallLocation + "/" + picName + convertWallName(wall) + ".png");

        new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
            @Override
            public void callback(Uri uri) {

                if (uri == null) {
                    Log.e("Bitmap", "is null");
                    return;
                }

                BitmapFactory.Options options = new BitmapFactory.Options();

                int wallpaperManagerHeight = wm.getDesiredMinimumHeight();
                int wallpaperManagerWidth = wm.getDesiredMinimumWidth();

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(uri.getPath(), options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = GridAdapter.calculateInSampleSize(options, wallpaperManagerWidth, wallpaperManagerHeight);

                Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath(), options);

                //If bitmap height is bigger that device's - resize it
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                //Get imageView (device in this case) height
                int imageViewHeight = image.getHeight();

                if (height > imageViewHeight) {

                    float resizeRatio = ((float) imageViewHeight) / height;

                    Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, (int) (width * resizeRatio), imageViewHeight, false);

                    if (bitmap!= newBitmap){
                        bitmap.recycle();
                    }

                    image.setImageBitmap(newBitmap);
                } else {
                    image.setImageURI(uri);
                }


                final HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scroll);

                horizontalScrollView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int scrollAmount = (image.getMeasuredWidth() - horizontalScrollView.getMeasuredWidth()) / 2;
                        horizontalScrollView.smoothScrollBy(scrollAmount, 0);
                    }
                }, 500);

            }
        }, false).execute();


        final FloatingActionsMenu wallsFab = (FloatingActionsMenu) findViewById(R.id.wall_actions);
        wallsFab.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                fabBg.setVisibility(fabBg.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                fabBg.setBackgroundColor(Preferences.FABbackground());
            }

            @Override
            public void onMenuCollapsed() {
                fabBg.setVisibility(fabBg.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        final FloatingActionButton setWall = (FloatingActionButton) findViewById(R.id.setwall);
        setWall.setColorNormal(Preferences.FABapply());
        setWall.setColorPressed(Preferences.FABpressed());
        setWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri object) {
                        try {
                            wm.setStream(context.getContentResolver().openInputStream(object));
                            fab.collapse();

                            new SnackBar.Builder(context)
                                    .withMessageId(R.string.set_as_wall_done)
                                    .withActionMessageId(R.string.ok)
                                    .withStyle(SnackBar.Style.ALERT)
                                    .withDuration(SnackBar.SHORT_SNACK)
                                    .show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).execute();
            }
        });

        final FloatingActionButton saveWall = (FloatingActionButton) findViewById(R.id.savewall);
        saveWall.setColorNormal(Preferences.FABsave());
        saveWall.setColorPressed(Preferences.FABpressed());
        saveWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri object) {
                        fab.collapse();

                        MaterialDialog dialog = new MaterialDialog.Builder(context)
                                .title(getString(R.string.done))
                                .content(dialogContent)
                                .dismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        setFullScreen();
                                    }
                                })
                                .build();

                        dialog.setActionButton(DialogAction.NEGATIVE, R.string.close);
                        dialog.show();

                    }
                }).execute();

            }
        });

        final String wallName = convertWallName(wall);
        final File destLiveWallFile = new File(saveWallLocation + "/" + livePicName + wallName + ".png");
        final FloatingActionButton addToLW = (FloatingActionButton) findViewById(R.id.addToLW);

        isAddedToLWList = Preferences.isWallAddedToLWList(destLiveWallFile.getAbsolutePath());
        if(isAddedToLWList)
            addToLW.setTitle(getString(R.string.remFromLiveWallList));

        addToLW.setColorNormal(Preferences.FABaddLW());
        addToLW.setColorPressed(Preferences.FABpressed());
        addToLW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isAddedToLWList) {
                    fab.collapse();

                    new DownloadBitmap(context, wall, destLiveWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri object) {

                        addToLW.setTitle(getString(R.string.remFromLiveWallList));

                        isAddedToLWList = !isAddedToLWList;

                        new SnackBar.Builder(context)
                                .withMessageId(R.string.live_wall_pool_update_success)
                                .withActionMessageId(R.string.ok)
                                .withStyle(SnackBar.Style.ALERT)
                                .withDuration(SnackBar.SHORT_SNACK)
                                .show();
                    }
                }, false).execute();

                }
                // otherwise remove it from the pool and delete the wall from the sdcard
                else {
                    if(destLiveWallFile.exists())
                        destLiveWallFile.delete();
                    addToLW.setTitle(getString(R.string.addToLiveWallList));

                    fab.collapse();

                    new SnackBar.Builder(context)
                            .withMessageId(R.string.live_wall_pool_update_success)
                            .withActionMessageId(R.string.ok)
                            .withStyle(SnackBar.Style.ALERT)
                            .withDuration(SnackBar.SHORT_SNACK)
                            .show();
                }

            }
        });

        final FloatingActionButton cropWall = (FloatingActionButton) findViewById(R.id.cropwall);
        cropWall.setColorNormal(Preferences.FABcrop());
        cropWall.setColorPressed(Preferences.FABpressed());
        cropWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri uri) {
                        Intent setWall = new Intent(Intent.ACTION_ATTACH_DATA);
                        setWall.setDataAndType(uri, "image/*");
                        setWall.putExtra("png", "image/*");
                        startActivityForResult(Intent.createChooser(setWall, getString(R.string.set_as)), 1);
                        fab.collapse();
                    }
                }).execute();

            }
        });

        final FloatingActionButton editWall = (FloatingActionButton) findViewById(R.id.editwall);
        editWall.setColorNormal(Preferences.FABedit());
        editWall.setColorPressed(Preferences.FABpressed());
        editWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri uri) {
                        Intent editWall = new Intent(Intent.ACTION_EDIT);
                        editWall.setDataAndType(uri, "image/*");
                        editWall.putExtra("png", "image/*");
                        startActivityForResult(Intent.createChooser(editWall, getString(R.string.edit_wall)), 1);
                        fab.collapse();
                    }
                }).execute();

            }
        });

        final FloatingActionButton shareWall = (FloatingActionButton) findViewById(R.id.sharewall);
        shareWall.setColorNormal(Preferences.FABshare());
        shareWall.setColorPressed(Preferences.FABpressed());
        shareWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DownloadBitmap(context, wall, destWallFile, new Callback<Uri>() {
                    @Override
                    public void callback(Uri uri) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        WallsFragment.this.startActivityForResult(Intent.createChooser(shareIntent, "Share Via"), ACTIVITY_SHARE);
                        fab.collapse();

                    }
                }).execute();

            }
        });
    }

    private void setFullScreen() {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


    }

    public static String convertWallName(String link) {
        return (link
                .replaceAll("png", "")
                .replaceAll("jpg", "")
                .replaceAll("jpeg", "")
                .replaceAll("bmp", "")
                .replaceAll("[^a-zA-Z0-9\\p{Z}]", "")
                .replaceFirst("^[0-9]+(?!$)", "")
                .replaceAll("\\p{Z}", "_"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setFullScreen();
    }

    public static void scan(Context context, String volume) {
        Bundle args = new Bundle();
        args.putString("volume", volume);
        context.startService(
                new Intent().
                        setComponent(new ComponentName("com.android.providers.media", "com.android.providers.media.MediaScannerService")).
                        putExtras(args));
    }

    private static class DownloadBitmap extends AsyncTask<Void, Void, Uri> {

        private WeakReference<Activity> activity;
        private String url;
        private File dest;
        private Callback<Uri> callback;
        private boolean showSnackBar;

        public DownloadBitmap(Activity activity, String url, File dest, Callback<Uri> callback) {
            this(activity, url, dest, callback, true);
        }


        public DownloadBitmap(Activity activity, String url, File dest, Callback<Uri> callback, boolean showSnackBar) {
            this.url = url;
            this.dest = dest;
            this.activity = new WeakReference<>(activity);
            this.callback = callback;
            this.showSnackBar = showSnackBar;
        }


        @Override
        protected void onPreExecute() {
            if (showSnackBar) {
                new SnackBar.Builder(activity.get())
                        .withMessageId(R.string.wallpaper_downloading_wait)
                        .withActionMessageId(R.string.ok)
                        .withStyle(SnackBar.Style.ALERT)
                        .withDuration(SnackBar.SHORT_SNACK)
                        .show();
            }
        }

        @Override
        protected Uri doInBackground(Void... params) {

            try {

                dest.getParentFile().mkdirs();

                int i = 0;

                //We're trying to download file 5 times
                while (i < 5) {
                    i++;

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeFile(dest.getPath(), options);

                    if (options.outWidth > 0 && options.outHeight > 0) {
                        return Uri.fromFile(dest);
                    } else {
                        dest.delete();
                    }

                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder().url(url).get().build();

                    Response response = client.newCall(request).execute();

                    BufferedSink sink = Okio.buffer(Okio.sink(dest));
                    sink.writeAll(response.body().source());
                    sink.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (uri != null) {
                scan(activity.get(), "external");
                callback.callback(uri);
            } else {
                new SnackBar.Builder(activity.get())
                        .withMessage("Download failed")
                        .withActionMessageId(R.string.ok)
                        .withStyle(SnackBar.Style.ALERT)
                        .withDuration(SnackBar.SHORT_SNACK)
                        .show();
            }
        }
    }

}
