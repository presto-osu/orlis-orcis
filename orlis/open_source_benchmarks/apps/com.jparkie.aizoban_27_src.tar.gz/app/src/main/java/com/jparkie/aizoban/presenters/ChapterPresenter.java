package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public interface ChapterPresenter {
    public void handleInitialArguments(Intent arguments);

    public void initializeViews();

    public void initializeOptions();

    public void initializeMenu();

    public void initializeDataFromUrl(FragmentManager fragmentManager);

    public void registerForEvents();

    public void unregisterForEvents();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void saveChapterToRecentChapters();

    public void destroyAllSubscriptions();

    public void onTrimMemory(int level);

    public void onLowMemory();

    public void onPageSelected(int position);

    public void onFirstPageOut();

    public void onLastPageOut();

    public void onPreviousClick();

    public void onNextClick();

    public void onOptionParent();

    public void onOptionRefresh();

    public void onOptionSelectPage();

    public void onOptionDirection();

    public void onOptionOrientation();

    public void onOptionZoom();

    public void onOptionHelp();
}
