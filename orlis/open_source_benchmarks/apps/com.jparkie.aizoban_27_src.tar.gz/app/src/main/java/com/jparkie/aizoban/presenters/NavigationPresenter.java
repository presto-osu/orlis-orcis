package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface NavigationPresenter {
    public void handleInitialArguments(Bundle arguments);

    public void initializeViews();

    public void initializeNavigationFromResources();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void onNavigationItemClick(int position);
}
