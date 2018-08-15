package com.jparkie.aizoban.views;

import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface MainView extends BaseContextView, BaseToolbarView {
    public void initializeDrawerLayout();

    public void closeDrawerLayout();

    public int getNavigationLayoutId();

    public int getMainLayoutId();
}
