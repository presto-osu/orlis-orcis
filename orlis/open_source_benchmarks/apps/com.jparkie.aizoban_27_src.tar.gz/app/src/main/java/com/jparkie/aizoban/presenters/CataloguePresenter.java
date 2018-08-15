package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface CataloguePresenter {
    public void initializeViews();

    public void initializeSearch();

    public void initializeDataFromPreferenceSource();

    public void registerForEvents();

    public void unregisterForEvents();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onMangaClick(int position);

    public void onQueryTextChange(String query);

    public void onPreviousClick();

    public void onNextClick();

    public void onOptionFilter();

    public void onOptionToTop();
}
