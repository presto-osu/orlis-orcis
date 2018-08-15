package org.itishka.pointim.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import org.itishka.pointim.R;

/**
 * Created by Tishka17 on 05.05.2016.
 */
public class CustomDialog extends MaterialDialog {
    protected CustomDialog(Builder builder) {
        super(builder);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        ((ViewGroup)getCustomView()).addView(view, params);
    }

    @Override
    public void setContentView(View view) {
        ((ViewGroup)getCustomView()).addView(view);
    }

    public static class Builder extends MaterialDialog.Builder {
        public Builder(@NonNull Context context) {
            super(context);
        }

        public Builder enableSetContentView() {
            return (Builder) customView(R.layout.dialog_custom, false);
        }

        @UiThread
        public CustomDialog build() {
            return new CustomDialog(this);
        }
    }
}
