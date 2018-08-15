package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface MarkReadPresenter {
    public void handleInitialArguments(Bundle arguments);

    public void initializeViews();

    public void initializeDataFromDatabase();

    public void overrideDialogButtons();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void destroyAllSubscriptions();

    public void releaseAllResources();

    public void onQueueButtonClick();

    public void onToggleButtonClick();
}
