package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface DownloadMangaPresenter {
    public void initializeViews();

    public void initializeSearch();

    public void initializeDataFromDatabase();

    public void onResume();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onDownloadMangaClick(int position);

    public void onQueryTextChange(String query);

    public void onOptionToTop();
}
