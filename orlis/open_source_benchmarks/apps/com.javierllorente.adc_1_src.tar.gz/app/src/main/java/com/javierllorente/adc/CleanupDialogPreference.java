/*
 * Copyright (C) 2016 Javier Llorente <javier@opensuse.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.javierllorente.adc;

import android.app.Activity;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.webkit.WebView;

public class CleanupDialogPreference extends DialogPreference {

    public CleanupDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        super.onDialogClosed(positiveResult);
        if(positiveResult) {
            Context mainContext = App.getContext();
            WebFragment webFragment = (WebFragment) ((Activity) mainContext).getFragmentManager().findFragmentById(R.id.webFragment);
            WebView mWebView = webFragment.getWebView();
            mWebView.clearCache(true);
        }
    }
}