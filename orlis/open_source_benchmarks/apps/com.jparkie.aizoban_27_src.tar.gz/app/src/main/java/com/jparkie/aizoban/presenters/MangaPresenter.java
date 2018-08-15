package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.os.Bundle;

public interface MangaPresenter {
    public void handleInitialArguments(Intent arguments);

    public void initializeViews();

    public void initializeDataFromUrl();

    public void registerForEvents();

    public void unregisterForEvents();

    public void onResume();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onApplyColorChange(int color);

    public void onSwipeRefresh();

    public void onChapterClick(int position);

    public void onFavourite();

    public void onOptionRefresh();

    public void onOptionMarkRead();

    public void onOptionDownload();

    public void onOptionToTop();

    public void onOptionDelete();

    public void onOptionSelectAll();

    public void onOptionClear();
}
