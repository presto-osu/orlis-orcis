package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface RecentChapterPresenter {
    public void initializeViews();

    public void initializeSearch();

    public void initializeDataFromDatabase();

    public void registerForEvents();

    public void unregisterForEvents();

    public void onResume();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onRecentChapterClick(int position);

    public void onQueryTextChange(String query);

    public void onOptionToTop();

    public void onOptionDelete();

    public void onOptionSelectAll();

    public void onOptionClear();
}
