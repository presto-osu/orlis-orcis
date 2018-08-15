package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface LatestMangaPresenter {
    public void initializeViews();

    public void initializeDataFromPreferenceSource();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onMangaClick(int position);

    public void onSwipeRefresh();

    public void onOptionRefresh();

    public void onOptionToTop();
}
