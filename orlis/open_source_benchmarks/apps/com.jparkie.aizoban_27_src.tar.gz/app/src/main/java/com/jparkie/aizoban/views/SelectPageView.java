package com.jparkie.aizoban.views;


import android.widget.BaseAdapter;

import com.jparkie.aizoban.views.base.BaseContextView;

public interface SelectPageView extends BaseContextView {
    public void initializeSpinner(BaseAdapter adapter);

    public void setSpinnerPageNumber(int pageNumber);
}
