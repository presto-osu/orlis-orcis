package com.jparkie.aizoban.presenters.mapper;

import android.support.v4.view.PagerAdapter;

public interface ChapterMapper {
    public void registerAdapter(PagerAdapter adapter);

    public int getPosition();

    public void setPosition(int position);

    public void applyViewSettings();

    public void applyIsLockOrientation(boolean isLockOrientation);

    public void applyIsLockZoom(boolean isLockZoom);
}
