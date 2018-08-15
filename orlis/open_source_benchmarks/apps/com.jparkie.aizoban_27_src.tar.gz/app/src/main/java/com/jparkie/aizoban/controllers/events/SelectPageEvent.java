package com.jparkie.aizoban.controllers.events;

public class SelectPageEvent {
    private int mSelectedPage;

    public SelectPageEvent(int selectedPage) {
        mSelectedPage = selectedPage;
    }

    public int getSelectPage() {
        return mSelectedPage;
    }
}
