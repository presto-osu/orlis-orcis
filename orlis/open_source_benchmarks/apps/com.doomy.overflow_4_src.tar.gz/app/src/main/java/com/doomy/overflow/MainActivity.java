/**
 * Copyright (C) 2013 Damien Chazoule
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.doomy.overflow;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.EventListener;

import java.util.ArrayList;

public class MainActivity extends Activity {

    // Declaring your view and variables
    private static final String TAG = "MainActivity";
    private ActionBar mActionBar;
    private static DataBase mDB;
    private static RecyclerView mRecyclerView;
    private static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static ArrayList<Message> mMessage;
    private static MainActivity mActivity;
    private FloatingActionButton mFAB;
    private static RelativeLayout mRelativeLayout;
    private static TextView mTextViewBegin;
    private static TextView mTextViewBeginning;
    private boolean mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        mActionBar = getActionBar();
        mActionBar.setTitle(getString(R.string.history));

        mDB = new DataBase(this);

        // Open "Hello" dialog at the first launch
        openFirstDialog();

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        mTextViewBegin = (TextView) findViewById(R.id.textViewBegin);
        mTextViewBegin.setText(getString(R.string.begin));
        mTextViewBeginning = (TextView) findViewById(R.id.textViewBeginning);
        mTextViewBeginning.setText(getString(R.string.beginning));

        mFAB = (FloatingActionButton) findViewById(R.id.searchContact);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(mIntent);
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessage.clear();
            }
        });

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mMessage = new ArrayList<>();

        initRows();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, SendService.class));
    }

    public static void initRows() {

        if (mDB.getRowsCount() > 0) {
            mRelativeLayout.setAlpha(0);
            for (int i = 0; i < mDB.getRowsCount(); i++) {
                mMessage.add(mDB.showOne(mDB.getRowsCount() - i));
            }
        } else {
            mRelativeLayout.setAlpha(1);
        }

        mAdapter = new CardViewAdapter(mMessage);
        mRecyclerView.setAdapter(mAdapter);
    }

    public static void syncRows() {

        mActivity.invalidateOptionsMenu();

        mMessage.clear();

        mRelativeLayout.setAlpha(0);
        for (int i = 0; i < mDB.getRowsCount(); i++) {
            mMessage.add(mDB.showOne(mDB.getRowsCount() - i));
        }

        mAdapter = new CardViewAdapter(mMessage);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void deleteRows() {

        killNotification();

        showSnackBar();

        invalidateOptionsMenu();

        mMessage.clear();

        mAdapter = new CardViewAdapter(mMessage);
        mRecyclerView.setAdapter(mAdapter);

        mDB.deleteAll();

        MainActivity.mRelativeLayout.setAlpha(1);
    }

    private void killNotification() {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem mItem = menu.findItem(R.id.action_clear);

        if (mMessage.size() == 0) {
            mItem.setVisible(false);
        } else {
            mItem.setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml.
         */
        int id = item.getItemId();

        // NoInspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            openAboutDialog();
            return true;
        }
        if (id == R.id.action_clear) {
            openDeleteDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Create AlertDialog for delete messages
    private void openDeleteDialog() {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);

        String mText = getString(R.string.dialog_message);

        if (mDB.getRowsCount() > 1) {
            mText = getString(R.string.dialog_messages);
        }

        mAlertDialog.setMessage(mText);
        mAlertDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteRows();
                    }
                });
        mAlertDialog.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        mAlertDialog.show();
    }

    // Create AlertDialog for the about view
    private void openAboutDialog() {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this);
        View mView = mLayoutInflater.inflate(R.layout.view_about, null);

        ImageView mImageViewMrDoomy = (ImageView) mView.findViewById(R.id.imageViewMrDoomy);
        ImageView mImageViewStudio = (ImageView) mView.findViewById(R.id.imageViewStudio);
        ImageView mImageViewGitHub = (ImageView) mView.findViewById(R.id.imageViewGitHub);
        Drawable mMrDoomy = mImageViewMrDoomy.getDrawable();
        Drawable mStudio = mImageViewStudio.getDrawable();
        Drawable mGitHub = mImageViewGitHub.getDrawable();
        mMrDoomy.setColorFilter(getResources().getColor(R.color.indigoDark), PorterDuff.Mode.SRC_ATOP);
        mStudio.setColorFilter(getResources().getColor(R.color.indigo), PorterDuff.Mode.SRC_ATOP);
        mGitHub.setColorFilter(getResources().getColor(R.color.greyMaterialDark), PorterDuff.Mode.SRC_ATOP);

        mImageViewGitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent();
                mIntent.setAction(Intent.ACTION_VIEW);
                mIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                mIntent.setData(Uri.parse(getString(R.string.url)));
                startActivity(mIntent);
            }
        });

        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);

        mAlertDialog.setTitle(getString(R.string.about));
        mAlertDialog.setView(mView);
        mAlertDialog.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog.show();
    }

    // Create AlertDialog for the first launch
    private void openFirstDialog() {
        mValue = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("mValue", true);

        if (mValue){
            AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme);

            mAlertDialog.setTitle(getString(R.string.title))
                    .setMessage(getString(R.string.message))
                    .setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mAlertDialog.show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("mValue", false).commit();
        }
    }

    public void showSnackBar() {

        String mText = getString(R.string.snackbar_message);

        if (mDB.getRowsCount() > 1) {
            mText = getString(R.string.snackbar_messages);
        }

        mFAB = (FloatingActionButton) findViewById(R.id.searchContact);
        SnackbarManager.show(
                Snackbar.with(this)
                        .text(mText)
                        .textColor(getResources().getColor(R.color.greyMaterialLight))
                        .color(getResources().getColor(R.color.greyMaterialDark))
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                mFAB.animate().translationY(-snackbar.getHeight());
                            }

                            @Override
                            public void onShowByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onShown(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                                mFAB.animate().translationY(0);
                            }

                            @Override
                            public void onDismissByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {

                            }
                        })
                        .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
    }
}