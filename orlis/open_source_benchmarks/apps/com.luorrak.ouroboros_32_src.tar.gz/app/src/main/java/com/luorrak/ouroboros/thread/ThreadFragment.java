package com.luorrak.ouroboros.thread;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;
import android.widget.ProgressBar;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.gallery.GalleryFragment;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Media;
import com.luorrak.ouroboros.util.NetworkHelper;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.ArrayList;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
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
public class ThreadFragment extends Fragment implements MenuItemCompat.OnActionExpandListener {
    // Construction ////////////////////////////////////////////////////////////////////////////////
    private final String LOG_TAG = ThreadFragment.class.getSimpleName();
    private InfiniteDbHelper infiniteDbHelper;
    private NetworkHelper networkHelper = new NetworkHelper();
    private RecyclerView recyclerView;
    private ThreadAdapter threadAdapter;
    private LinearLayoutManager layoutManager;
    private ThreadNetworkFragment networkFragment;
    private ProgressBar progressBar;
    private String resto;
    private String boardName;
    private int threadPosition;
    private boolean firstRequest;
    private Parcelable savedLayoutState ;
    private boolean isStatusCheckIsRunning;
    private Handler handler;

    private String oldJsonString;
    private int pollingInterval;

    //Get thread number from link somehow
    public ThreadFragment newInstance(String resto, String boardName){
        return newInstance(resto, boardName, 0);
    }

    public ThreadFragment newInstance(String resto, String boardName, int threadPosition){
        ThreadFragment threadFragment = new ThreadFragment();
        Bundle args = new Bundle();
        args.putString("resto", resto);
        args.putString("boardName", boardName);
        args.putInt("threadPosition", threadPosition);
        threadFragment.setArguments(args);
        return threadFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isStatusCheckIsRunning = false;
        if (savedInstanceState != null){
            savedLayoutState = savedInstanceState.getParcelable("savedLayout");
            resto = savedInstanceState.getString("resto");
            boardName = getArguments().getString("boardName");
            firstRequest = getArguments().getBoolean("firstRequest");
        } else {
            firstRequest = true;
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        networkFragment = (ThreadNetworkFragment) getFragmentManager().findFragmentByTag("Thread_Task");
        View view = inflater.inflate(R.layout.fragment_thread, container, false);
        getActivity().invalidateOptionsMenu();
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        layoutManager = new LinearLayoutManager(getActivity()){
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        recyclerView = (RecyclerView) view.findViewById(R.id.postList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getLayoutManager().onRestoreInstanceState(savedLayoutState);


        if (getArguments() != null) {
            resto = getArguments().getString("resto");
            boardName = getArguments().getString("boardName");
            threadPosition = getArguments().getInt("threadPosition");
        }

        if (networkFragment == null) {
            networkFragment = new ThreadNetworkFragment();
            getFragmentManager().beginTransaction().add(networkFragment, "Thread_Task").commit();
        }

        if (boardName != null){
            handler = new Handler();
            pollingInterval = 10000;

            view.post(new Runnable() {
                @Override
                public void run() {
                    int h = recyclerView.getHeight();
                    int w = recyclerView.getWidth();
                    threadAdapter = new ThreadAdapter(infiniteDbHelper.getThreadCursor(resto), getFragmentManager(), boardName, getActivity(), infiniteDbHelper, w, h);
                    threadAdapter.setHasStableIds(true);
                    threadAdapter.hasStableIds();
                    recyclerView.setAdapter(threadAdapter);
                }
            });
        }

        return view;
    }

    // Life Cycle //////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onPause() {
        stopStatusCheck();
        super.onPause();
    }

    @Override
    public void onResume() {
        startStatusCheck();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (savedLayoutState == null){
            savedLayoutState = layoutManager.onSaveInstanceState();
        }
        outState.putParcelable("savedLayout", savedLayoutState);
        outState.putString("boardName", boardName);
        outState.putString("resto", resto);
        outState.putBoolean("firstRequest", firstRequest);
    }

    // Options Menu ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem goToBottomButton = menu.findItem(R.id.action_scroll_bottom);
        MenuItem goToTopButton = menu.findItem(R.id.action_scroll_top);
        MenuItem replyButton = menu.findItem(R.id.action_reply);
        MenuItem watchlistButton = menu.findItem(R.id.action_add_watchlist);
        MenuItem refreshButton = menu.findItem(R.id.action_refresh);
        MenuItem galleryButton = menu.findItem(R.id.action_gallery);
        MenuItem saveAllImagesButton = menu.findItem(R.id.action_save_all_images);
        MenuItem openExternalButton = menu.findItem(R.id.action_external_browser);
        MenuItem shareButton = menu.findItem(R.id.menu_item_share);
        MenuItem menuLayout = menu.findItem(R.id.action_menu_layout);

        MenuItem searchButton = menu.findItem(R.id.action_search);
        searchButton.setVisible(true);
        final SearchView searchView = (SearchView) searchButton.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                threadAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        return infiniteDbHelper.searchThreadForString(constraint.toString(), resto);
                    }
                });
                threadAdapter.getFilter().filter(newText);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchButton, this);

        refreshButton.setVisible(true);
        goToBottomButton.setVisible(true);
        goToTopButton.setVisible(true);
        replyButton.setVisible(true);
        galleryButton.setVisible(true);
        saveAllImagesButton.setVisible(true);
        openExternalButton.setVisible(true);
        shareButton.setVisible(true);
        watchlistButton.setVisible(true);
        menuLayout.setVisible(true);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        stopStatusCheck();
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        int backStackCount = getFragmentManager().getBackStackEntryCount();
        if (backStackCount > 0) {
            FragmentManager.BackStackEntry backEntry = getFragmentManager().getBackStackEntryAt(backStackCount - 1);
            String str = backEntry.getName();
            if (str.equals("threadDialog")){
                getActivity().onBackPressed();
                return false;
            }
        }
        startStatusCheck();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:{
                getThread(resto, boardName);
                break;
            }
            case R.id.action_scroll_bottom:{
                recyclerView.scrollToPosition(threadAdapter.getItemCount() - 1);
                break;
            }
            case R.id.action_scroll_top: {
                recyclerView.scrollToPosition(0);
                break;
            }
            case R.id.action_reply:{
                Intent intent =  new Intent(getActivity(), ReplyCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Util.INTENT_THREAD_NO, resto);
                intent.putExtra(Util.INTENT_BOARD_NAME, boardName);
                getActivity().startActivity(intent);
                break;
            }
            case R.id.action_gallery:{
                GalleryFragment galleryFragment = new GalleryFragment().newInstance(boardName, resto);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.placeholder_card, galleryFragment)
                        .addToBackStack("galleryfragment")
                        .commit();
                break;
            }
            case R.id.action_save_all_images: {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.REQUEST_STORAGE_PERMISSION);
                } else {
                    showDownloadAllDialog();
                }
                break;
            }
            case R.id.action_external_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ChanUrls.getThreadUrlExternal(boardName, resto)));
                startActivity(browserIntent);
                break;
            }
            case R.id.menu_item_share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = ChanUrls.getThreadUrlExternal(boardName, resto);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            }
            case R.id.action_add_watchlist: {
                Cursor cursor = infiniteDbHelper.getWatchlistCursor();
                int count = cursor.getCount();
                byte[] serializedMediaList;
                cursor.close();

                Cursor threadcursor = infiniteDbHelper.getThreadCursor(resto);
                serializedMediaList = (threadcursor.getCount() > 0 ) ? threadcursor.getBlob(threadcursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_MEDIA_FILES)) : null;

                infiniteDbHelper.insertWatchlistEntry(String.valueOf(getActivity().getTitle()), boardName, resto, serializedMediaList, count);
                ((ThreadActivity) getActivity()).updateWatchlist();
                Snackbar.make(getView(), "Thread Added To Watchlist", Snackbar.LENGTH_LONG).show();
                threadcursor.close();

                break;
            }
            case R.id.action_layout_vertical: {
                SettingsHelper.setThreadView(getActivity(), Util.THREAD_LAYOUT_VERTICAL);
                ThreadFragment threadFragment = new ThreadFragment().newInstance(resto, boardName);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.placeholder_card, threadFragment).commit();
                break;
            }
            case R.id.action_layout_horizontal: {
                SettingsHelper.setThreadView(getActivity(), Util.THREAD_LAYOUT_HORIZONTAL);
                ThreadFragment threadFragment = new ThreadFragment().newInstance(resto, boardName);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.placeholder_card, threadFragment).commit();
                break;
            }
        }
        return true;
    }

    // Loading Data ////////////////////////////////////////////////////////////////////////////////

    private void getThread(String threadNo, String boardName){
        if (getActivity() != null){
            getThreadJson(getActivity(), boardName, threadNo);
        }
    }

    private void getThreadJson(final Context context, final String boardName, final String threadNumber){
        progressBar.setVisibility(View.VISIBLE);
        final String threadJsonUrl = ChanUrls.getThreadUrl(boardName, threadNumber);

        Ion.with(context)
                .load(threadJsonUrl)
                .setLogging(LOG_TAG, Log.DEBUG)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {

                    @Override
                    public void onCompleted(Exception e, JsonObject jsonObject) {
                        if (getActivity() != null){
                            if (e == null) {
                                if (jsonObject.toString().equals(oldJsonString)) {
                                    pollingInterval = pollingInterval + pollingInterval / 2;
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    restartStatusCheck();
                                    oldJsonString = jsonObject.toString();
                                    networkFragment.beginTask(jsonObject, infiniteDbHelper, boardName, resto, threadPosition, firstRequest, recyclerView, threadAdapter);
                                }
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                Snackbar.make(getView(), "Error retrieving thread", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        firstRequest = false;
                    }
                });
    }
    public void showDownloadAllDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Download All Images")
                .setMessage("Are you sure you want to download all images?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startDownload();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    public void startDownload(){
        Cursor imageCursor = infiniteDbHelper.getGalleryCursor(resto);
        do {
            ArrayList<Media> mediaArrayList = (ArrayList<Media>) Util.deserializeObject(imageCursor.getBlob(imageCursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_MEDIA_FILES)));
            for (Media mediaItem : mediaArrayList){
                networkHelper.downloadFile(boardName, mediaItem.fileName, mediaItem.ext, getActivity());
            }
        } while (imageCursor.moveToNext());
        imageCursor.close();
    }

    private Runnable statusCheck = new Runnable() {
        @Override
        public void run() {
            getThread(resto, boardName);
            handler.postDelayed(statusCheck, pollingInterval);
        }
    };

    private void startStatusCheck() {
        if (!isStatusCheckIsRunning){
            isStatusCheckIsRunning = true;
            statusCheck.run();
        }
    }

    private void stopStatusCheck() {
        isStatusCheckIsRunning = false;
        handler.removeCallbacks(statusCheck);
    }

    private void restartStatusCheck(){
        stopStatusCheck();
        pollingInterval = 10000;
        startStatusCheck();
    }
}
