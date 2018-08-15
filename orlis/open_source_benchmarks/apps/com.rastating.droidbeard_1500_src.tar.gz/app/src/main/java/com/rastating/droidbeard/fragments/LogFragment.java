/*
     DroidBeard - a free, open-source Android app for managing SickBeard
     Copyright (C) 2014-2015 Robert Carr

     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with this program.  If not, see http://www.gnu.org/licenses/.
*/

package com.rastating.droidbeard.fragments;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rastating.droidbeard.R;
import com.rastating.droidbeard.net.ApiResponseListener;
import com.rastating.droidbeard.net.FetchLogsTask;
import com.rastating.droidbeard.net.SickBeardException;
import com.rastating.droidbeard.net.SickbeardAsyncTask;
import com.rastating.droidbeard.ui.CrossFader;
import com.rastating.droidbeard.ui.LoadingAnimation;

import java.io.InputStream;

public class LogFragment extends DroidbeardFragment implements ApiResponseListener<String[]> {
    private WebView mWebView;
    private View mErrorContainer;
    private ImageView mLoadingImage;
    private TextView mErrorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    public LogFragment() {
        setTitle(R.string.title_logs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_log, null, false);
        mWebView = (WebView) root.findViewById(R.id.web_view);
        mErrorContainer = root.findViewById(R.id.error_container);
        mLoadingImage = (ImageView) root.findViewById(R.id.loading);
        mErrorMessage = (TextView) root.findViewById(R.id.error_message);
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                onRefreshButtonPressed();

                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.materialPrimaryDark, R.color.materialPrimary, R.color.navigation_list_item_selected, R.color.unaired_episode_background);

        showLoadingAnimation();
        onRefreshButtonPressed();

        return root;
    }

    @Override
    public void onApiRequestFinished(SickbeardAsyncTask sender, String[] result) {
        if (activityStillExists()) {
            if (result != null) {
                String entries = "";
                for (String entry : result) {
                    entries += entry + "<br>";
                }

                try {
                    InputStream stream = getResources().openRawResource(R.raw.log_template);
                    byte[] b = new byte[stream.available()];
                    stream.read(b);
                    String html = new String(b);
                    mWebView.loadData(html.replace("{{logs}}", entries), "text/html", null);
                    stream.close();
                    showWebView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                showError(getString(R.string.error_fetching_logs), sender.getLastException());
            }
        }
    }

    @Override
    public void onRefreshButtonPressed() {
        //showLoadingAnimation();
        FetchLogsTask task = new FetchLogsTask(getActivity());
        task.addResponseListener(this);
        task.start();

        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    protected void showError(String message, SickBeardException e) {
        mErrorMessage.setText(message);
        mErrorContainer.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        mLoadingImage.clearAnimation();
        mLoadingImage.setVisibility(View.GONE);
    }

    protected void showLoadingAnimation() {
        mErrorContainer.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mLoadingImage.setAlpha(1.0f);
        mLoadingImage.setVisibility(View.VISIBLE);
        mLoadingImage.startAnimation(new LoadingAnimation());
    }

    protected void showWebView() {
        mErrorContainer.setVisibility(View.GONE);
        new CrossFader(mLoadingImage, mWebView, 500).start();
    }
}