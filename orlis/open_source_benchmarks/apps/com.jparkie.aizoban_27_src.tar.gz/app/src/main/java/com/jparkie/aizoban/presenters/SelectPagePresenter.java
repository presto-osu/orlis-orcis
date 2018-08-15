package com.jparkie.aizoban.presenters;

import android.os.Bundle;

public interface SelectPagePresenter {
    public void handleInitialArguments(Bundle arguments);

    public void initializeViews();

    public void saveState(Bundle outState);

    public void restoreState(Bundle savedState);

    public void onItemSelected(int pageNumber);

    public void onOkButtonClick();

    public void onCancelButtonClick();
}
