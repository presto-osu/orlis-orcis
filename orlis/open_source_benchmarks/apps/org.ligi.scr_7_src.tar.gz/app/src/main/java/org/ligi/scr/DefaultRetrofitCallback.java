package org.ligi.scr;

import android.app.Activity;
import android.app.AlertDialog;

import org.ligi.axt.listeners.ActivityFinishingOnClickListener;
import org.ligi.axt.listeners.DialogDiscardingOnClickListener;

import retrofit.Callback;

abstract class DefaultRetrofitCallback<T> implements Callback<T> {

    private final boolean fatal;
    private final Activity activity;

    DefaultRetrofitCallback(boolean fatal, Activity activity) {
        this.fatal = fatal;
        this.activity = activity;
    }

    @Override
    public void onFailure(Throwable error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(error.getMessage());

        if (fatal) {
            builder.setPositiveButton(android.R.string.ok, new ActivityFinishingOnClickListener(activity));
        } else {
            builder.setPositiveButton(android.R.string.ok, new DialogDiscardingOnClickListener());
        }

        builder.show();
    }

}
