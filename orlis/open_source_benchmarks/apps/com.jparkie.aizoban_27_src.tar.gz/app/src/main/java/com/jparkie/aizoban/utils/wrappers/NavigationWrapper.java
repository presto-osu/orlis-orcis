package com.jparkie.aizoban.utils.wrappers;

public class NavigationWrapper {
    private int mIconResource;
    private int mTitleResource;

    public NavigationWrapper(int iconResource, int titleResource) {
        mIconResource = iconResource;
        mTitleResource = titleResource;
    }

    public int getIconResource() {
        return mIconResource;
    }

    public int getTitleResource() {
        return mTitleResource;
    }
}
