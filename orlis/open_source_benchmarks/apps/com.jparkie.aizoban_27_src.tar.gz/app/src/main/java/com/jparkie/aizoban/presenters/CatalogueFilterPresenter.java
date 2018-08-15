package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface CatalogueFilterPresenter {
    public void handleInitialArguments(Bundle arguments);

    public void initializeDataFromPreferenceSource();

    public void overrideDialogButtons();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void onFilterButtonClick();

    public void onClearButtonClick();
}
