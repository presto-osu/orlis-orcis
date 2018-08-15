package com.jparkie.aizoban.views;

import com.jparkie.aizoban.views.base.BaseAbsListViewView;
import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseEmptyRelativeLayoutView;
import com.jparkie.aizoban.views.base.BaseSelectionView;
import com.jparkie.aizoban.views.base.BaseSwipeRefreshLayoutView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface MangaView extends BaseContextView, BaseToolbarView, BaseSwipeRefreshLayoutView, BaseEmptyRelativeLayoutView, BaseAbsListViewView, BaseSelectionView {
    public void initializeDeletionListView();

    public void initializeFavouriteButton(boolean isFavourite);

    public void showListViewIfHidden();

    public void showChapterStatusError();

    public void hideChapterStatusError();

    public void setTitle(String title);

    public void setName(String name);

    public void setDescription(String description);

    public void setAuthor(String author);

    public void setArtist(String artist);

    public void setGenre(String genre);

    public void setIsCompleted(boolean isCompleted);

    public void setThumbnail(String url);

    public void setFavouriteButton(boolean isFavourite);

    public int getHeaderViewsCount();

    public void toastMangaError();
}
