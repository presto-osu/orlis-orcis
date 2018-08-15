package com.jparkie.aizoban.presenters.base;

import android.os.Parcelable;

public interface BasePositionStateMapper {
    public Parcelable getPositionState();

    public void setPositionState(Parcelable state);
}
