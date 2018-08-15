package org.pulpdust.lesserpad;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class forICS {
	public void setUiOptions(int uio, Activity av){
		av.getWindow().setUiOptions(uio);
	}
}
