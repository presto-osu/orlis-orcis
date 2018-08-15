package com.jparkie.aizoban.views;

import android.content.Intent;

import com.jparkie.aizoban.views.base.BaseContextView;
import com.jparkie.aizoban.views.base.BaseEmptyRelativeLayoutView;
import com.jparkie.aizoban.views.base.BaseToolbarView;

public interface ChapterView extends BaseContextView, BaseToolbarView, BaseEmptyRelativeLayoutView {
    public void initializeHardwareAcceleration();

    public void initializeSystemUIVisibility();

    public void initializeViewPager();

    public void initializeButtons();

    public void initializeTextView();

    public void initializeFullscreen();

    public void enableFullscreen();

    public void disableFullscreen();

    public void setTitleText(String title);

    public void setSubtitleProgressText(int imageUrlsCount);

    public void setSubtitlePositionText(int position);

    public void setImmersivePositionText(int position);

    public void setOptionDirectionText(boolean isRightToLeftDirection);

    public void setOptionOrientationText(boolean isLockOrientation);

    public void setOptionZoomText(boolean isLockZoom);

    public void toastNotInitializedError();

    public void toastChapterError();

    public void toastNoPreviousChapter();

    public void toastNoNextChapter();

    public void finishAndLaunchActivity(Intent launchIntent, boolean isFadeTransition);
}
