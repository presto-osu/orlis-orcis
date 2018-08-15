package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface QueuePresenter {
    public void initializeViews();

    public void initializeDataFromDatabase();

    public void registerForEvents();

    public void unregisterForEvents();

    public void onResume();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onOptionStartDownloader();

    public void onOptionStopDownloader();

    public void onOptionToTop();

    public void onOptionCancel();

    public void onOptionSelectAll();

    public void onOptionClear();
}
