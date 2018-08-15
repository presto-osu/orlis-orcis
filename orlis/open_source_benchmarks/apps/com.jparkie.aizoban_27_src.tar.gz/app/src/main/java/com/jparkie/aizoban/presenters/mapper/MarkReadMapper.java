package com.jparkie.aizoban.presenters.mapper;

import com.jparkie.aizoban.presenters.base.BaseAdapterMapper;
import com.jparkie.aizoban.presenters.base.BasePositionStateMapper;
import com.jparkie.aizoban.presenters.base.BaseSelectionMapper;

public interface MarkReadMapper extends BaseAdapterMapper, BasePositionStateMapper, BaseSelectionMapper {
    public int getCheckedItemCount();
}
