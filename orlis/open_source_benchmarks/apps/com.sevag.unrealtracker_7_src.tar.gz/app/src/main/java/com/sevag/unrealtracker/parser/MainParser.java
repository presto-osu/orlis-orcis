package com.sevag.unrealtracker.parser;

import android.app.Activity;
import com.sevag.unrealtracker.network.NetworkState;
import com.sevag.unrealtracker.ui.StatusDisplayer;

/**
 * Created by sevag on 3/28/15.
 */
public class MainParser implements Runnable {

    private BlogParser blogParser;
    private MarketplaceParser marketplaceParser;
    private LiveBroadcastParser liveBroadcastParser;

    private Activity callingActivity;
    private StatusDisplayer statusDisplayer;

    public MainParser(Activity callingActivity, StatusDisplayer statusDisplayer) {
        this.callingActivity = callingActivity;
        this.statusDisplayer = statusDisplayer;
        blogParser = new BlogParser();
        marketplaceParser = new MarketplaceParser();
        liveBroadcastParser = new LiveBroadcastParser();
    }

    @Override
    public void run() {
        if (NetworkState.isConnected(callingActivity)) {
            blogParser.fetchUE4BlogPosts();
            blogParser.fetchUT5BlogPosts();
            marketplaceParser.fetchUE4MarketplacePosts();
            liveBroadcastParser.fetchUTBroadcastSchedule();
            statusDisplayer.doneLoading();
        } else {
            statusDisplayer.doneLoadingWithoutWifi();
        }
    }
}
