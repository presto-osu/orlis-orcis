package com.jparkie.aizoban.views;

import com.jparkie.aizoban.views.base.BaseAbsListViewView;
import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseEmptyRelativeLayoutView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface CatalogueView extends BaseContextView, BaseToolbarView, BaseEmptyRelativeLayoutView, BaseAbsListViewView {
    public void initializeButtons();

    public void setSubtitlePositionText(int position);

    public void toastNoPreviousPage();

    public void toastNoNextPage();
}
