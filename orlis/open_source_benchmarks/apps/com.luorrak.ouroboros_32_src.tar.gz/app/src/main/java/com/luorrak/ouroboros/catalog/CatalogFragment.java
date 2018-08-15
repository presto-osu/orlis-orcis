package com.luorrak.ouroboros.catalog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
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

import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.reply.ReplyCommentActivity;
import com.luorrak.ouroboros.util.ChanUrls;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;


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

public class CatalogFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // Construction ////////////////////////////////////////////////////////////////////////////////

    private final String LOG_TAG = CatalogFragment.class.getSimpleName();
    private CatalogAdapter catalogAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String boardName = null;
    private InfiniteDbHelper infiniteDbHelper;
    private CatalogNetworkFragment networkFragment;
    private ActionProvider shareActionProvider;
    private ProgressBar progressBar;

    public CatalogFragment() {
    }

    public CatalogFragment newInstance(String boardName) {
        CatalogFragment frag = new CatalogFragment();
        Bundle args = new Bundle();
        args.putString("boardName", boardName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        infiniteDbHelper = new InfiniteDbHelper(getActivity());
        View view = inflater.inflate(R.layout.fragment_catalog, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        recyclerView = (RecyclerView) view.findViewById(R.id.catalogList);
        int catalogViewType = SettingsHelper.getCatalogView(getActivity());

        if (catalogViewType == Util.CATALOG_LAYOUT_GRID){
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), SettingsHelper.getCatalogColumns(getActivity())));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        //if not first load
        if (savedInstanceState != null){
            Parcelable savedLayoutState = savedInstanceState.getParcelable("SavedLayout");
            recyclerView.getLayoutManager().onRestoreInstanceState(savedLayoutState);
            boardName = savedInstanceState.getString("boardName");
            setHasOptionsMenu(true);
        } else {
            infiniteDbHelper.deleteCatalogCache();
            if (getArguments() != null){
                boardName = getArguments().getString("boardName");
                setHasOptionsMenu(true);
            }
            getCatalogJson(getActivity(), boardName);
        }

        if (boardName != null) {
            setActionBarTitle("/" + boardName + "/");
        }
        networkFragment = (CatalogNetworkFragment) getFragmentManager().findFragmentByTag("Catalog_Task");
        if (networkFragment == null) {
            networkFragment = new CatalogNetworkFragment();
            getFragmentManager().beginTransaction().add(networkFragment, "Catalog_Task").commit();
        }

        catalogAdapter = new CatalogAdapter(
                getSortedCursor(),
                boardName,
                infiniteDbHelper,
                getActivity());
        recyclerView.setAdapter(catalogAdapter);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.catalog_swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    // Options Menu ////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_catalog, menu);
        MenuItem replyButton = menu.findItem(R.id.action_reply);
        MenuItem openExternalButton = menu.findItem(R.id.action_external_browser);
        MenuItem menuLayout = menu.findItem(R.id.action_menu_layout);
        MenuItem shareButton = menu.findItem(R.id.menu_item_share);
        MenuItem sortBy = menu.findItem(R.id.action_sort_by);

        replyButton.setVisible(true);
        openExternalButton.setVisible(true);
        menuLayout.setVisible(true);
        sortBy.setVisible(true);
        shareButton.setVisible(true);
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton);

        MenuItem searchButton = menu.findItem(R.id.action_search);
        searchButton.setVisible(true);
        SearchView searchView = (SearchView) searchButton.getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(LOG_TAG, "query=" + newText);
                catalogAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        return infiniteDbHelper.searchCatalogForThread(constraint.toString(), SettingsHelper.getSortByMethod(getContext()));
                    }
                });
                catalogAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_reply:{
                String resto = "0";
                Intent intent =  new Intent(getActivity(), ReplyCommentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Util.INTENT_THREAD_NO, resto);
                intent.putExtra(Util.INTENT_BOARD_NAME, boardName);
                getActivity().startActivity(intent);
                break;
            }
            case R.id.action_external_browser: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ChanUrls.getCatalogUrlExternal(boardName)));
                startActivity(browserIntent);
                break;
            }
            case R.id.menu_item_share: {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = ChanUrls.getCatalogUrlExternal(boardName);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                break;
            }
            case R.id.action_sort_by_bump_order:{
                SettingsHelper.setSortByMethod(getContext(), SettingsHelper.BUMP_ORDER);
                catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor(SettingsHelper.getSortByMethod(getContext())));
                break;
            }
            case R.id.action_sort_by_creation_date: {
                SettingsHelper.setSortByMethod(getContext(), SettingsHelper.CREATION_DATE);
                catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor(SettingsHelper.getSortByMethod(getContext())));
                break;
            }
            case R.id.action_sort_by_reply_count: {
                SettingsHelper.setSortByMethod(getContext(), SettingsHelper.REPLY_COUNT);
                catalogAdapter.changeCursor(infiniteDbHelper.getCatalogCursor(SettingsHelper.getSortByMethod(getContext())));
                break;
            }
            case R.id.action_layout_grid: {
                SettingsHelper.setCatalogView(getActivity(), Util.CATALOG_LAYOUT_GRID);
                CatalogFragment catalogFragment = new CatalogFragment().newInstance(boardName);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
                break;
            }
            case R.id.action_layout_list: {
                SettingsHelper.setCatalogView(getActivity(), Util.CATALOG_LAYOUT_LIST);
                CatalogFragment catalogFragment = new CatalogFragment().newInstance(boardName);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
                break;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private Cursor getSortedCursor(){
        String sortByMethod = SettingsHelper.getSortByMethod(getActivity());
        Cursor cursor = infiniteDbHelper.getCatalogCursor(sortByMethod);
        return cursor;
    }

    private void setActionBarTitle(String title){
        getActivity().setTitle(title);
    }

    // Life Cycle //////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("SavedLayout", recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putString("boardName", boardName);
        super.onSaveInstanceState(outState);
    }

    //https://stackoverflow.com/questions/27057449/when-switch-fragment-with-swiperefreshlayout-during-refreshing-fragment-freezes
    @Override
    public void onPause() {
        super.onPause();

        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public void onDestroy() {
        if (networkFragment != null){
            if (networkFragment.getStatus() == AsyncTask.Status.FINISHED){
                networkFragment.cancelTask();
            }
        }
        super.onDestroy();
    }

    // Loading Data ////////////////////////////////////////////////////////////////////////////////
    private void getCatalogJson(final Context context, final String boardName) {
        String catalogJsonUrl = ChanUrls.getCatalogUrl(boardName);
        progressBar.setVisibility(View.VISIBLE);
        Ion.with(context)
                .load(catalogJsonUrl)
                .setLogging(LOG_TAG, Log.DEBUG)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray jsonArray) {
                        if (e == null) {
                            networkFragment.beginTask(jsonArray, infiniteDbHelper, boardName, catalogAdapter);
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            swipeRefreshLayout.setRefreshing(false);
                            if (getActivity() != null){
                                Snackbar.make(getView(), "Error retrieving catalog", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        catalogAdapter.changeCursor(getSortedCursor());
                    }
                });
    }

    @Override
    public void onRefresh() {
        if (boardName != null){
            getCatalogJson(getActivity(), boardName);
        }
    }
}

