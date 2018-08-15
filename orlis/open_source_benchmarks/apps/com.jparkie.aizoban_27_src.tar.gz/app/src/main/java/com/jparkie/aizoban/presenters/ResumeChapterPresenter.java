package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface ResumeChapterPresenter {
    public void handleInitialArguments(Bundle arguments);

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void onYesButtonClick();

    public void onNoButtonClick();
}
