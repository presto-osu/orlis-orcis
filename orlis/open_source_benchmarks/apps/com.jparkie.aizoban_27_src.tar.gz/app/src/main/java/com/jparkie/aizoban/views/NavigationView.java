package com.jparkie.aizoban.views;

import com.jparkie.aizoban.views.base.BaseAbsListViewView;
import com.jparkie.aizoban.views.base.BaseContextView;

public interface NavigationView extends BaseContextView, BaseAbsListViewView {
    public void initializeSourceTextView(String source);

    public void setThumbnail(String url);

    public void highlightPosition(int position);
}
