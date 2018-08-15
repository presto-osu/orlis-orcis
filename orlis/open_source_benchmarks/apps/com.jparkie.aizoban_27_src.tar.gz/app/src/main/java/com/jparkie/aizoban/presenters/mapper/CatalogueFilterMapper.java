package com.jparkie.aizoban.presenters.mapper;

import com.jparkie.aizoban.presenters.base.BaseAdapterMapper;
import com.jparkie.aizoban.presenters.base.BasePositionStateMapper;

import java.util.List;

public interface CatalogueFilterMapper extends BaseAdapterMapper, BasePositionStateMapper {
    public List<String> getSelectedGenres();

    public void setSelectedGenres(List<String> selectedGenres);

    public String getSelectedStatus();

    public void setSelectedStatus(String selectedStatus);

    public String getSelectedOrderBy();

    public void setSelectedOrderBy(String selectedOrderBy);
}
