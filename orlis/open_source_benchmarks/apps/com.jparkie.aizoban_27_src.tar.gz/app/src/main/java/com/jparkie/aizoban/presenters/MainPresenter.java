package com.jparkie.aizoban.presenters;

import android.content.Intent;
import android.os.Bundle;

public interface MainPresenter {
    public void initializeViews();

    public void initializeMainLayout(Intent argument);

    public void initializeNavigationLayout();

    public void registerForEvents();

    public void unregisterForEvents();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();
}
