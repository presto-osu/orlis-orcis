package com.jparkie.aizoban.views;

import com.jparkie.aizoban.views.base.BaseAbsListViewView;
import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseEmptyRelativeLayoutView;
import com.jparkie.aizoban.views.base.BaseSwipeRefreshLayoutView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface LatestMangaView extends BaseContextView, BaseToolbarView, BaseSwipeRefreshLayoutView, BaseEmptyRelativeLayoutView, BaseAbsListViewView {
    public void toastLatestError();
}
