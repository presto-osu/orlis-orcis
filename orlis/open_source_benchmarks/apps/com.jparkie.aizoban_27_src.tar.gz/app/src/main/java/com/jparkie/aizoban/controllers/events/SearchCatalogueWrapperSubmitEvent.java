package com.jparkie.aizoban.controllers.events;

import com.jparkie.aizoban.utils.wrappers.SearchCatalogueWrapper;

public class SearchCatalogueWrapperSubmitEvent {
    private SearchCatalogueWrapper mSearchCatalogueWrapper;

    public SearchCatalogueWrapperSubmitEvent(SearchCatalogueWrapper searchCatalogueWrapper) {
        mSearchCatalogueWrapper = searchCatalogueWrapper;
    }

    public SearchCatalogueWrapper getSearchCatalogueWrapper() {
        return mSearchCatalogueWrapper;
    }
}
