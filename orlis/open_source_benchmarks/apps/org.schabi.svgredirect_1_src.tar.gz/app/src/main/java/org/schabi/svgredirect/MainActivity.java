package org.schabi.svgredirect;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import com.rbricks.appsharing.ChooserArrayAdapter;

/**
 * Created by Christian Schabesberger on 13.03.16.
 *
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * MainActivity.java is part of NewPipe.
 *
 * SVG redirect is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SVG redirect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SVG redirect.  If not, see <http://www.gnu.org/licenses/>.
 */

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.toString();

    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        Intent intent = getIntent();

        shareDialog(intent.getData());
    }

    public void shareDialog(final Uri data){
        final List<String> packages=new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://test.com/test.html"));
        final List<ResolveInfo> resInfosNew=new ArrayList<>();
        final List<ResolveInfo> resInfos = getPackageManager().queryIntentActivities(intent, 0);
        resInfosNew.addAll(resInfos);
        if(!resInfos.isEmpty()) {
            for (ResolveInfo resInfo : resInfos) {
                String packageName=resInfo.activityInfo.packageName;
                packages.add(packageName);
            }
        } else {
            Log.e(TAG, "No fitting activity found");
            return;
        }

        if (packages.size() > 1) {
            ArrayAdapter<String> adapter = new ChooserArrayAdapter(this, android.R.layout.select_dialog_item, android.R.id.text1, packages);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.open_with)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item ) {
                            invokeApplication(packages.get(item),resInfosNew.get(item), data);
                            finish();
                        }
                    })
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                finish();
                            }
                            return true;
                        }
                    })
                    .show();
        } else if (packages.size() == 1) {
            invokeApplication(packages.get(0), resInfos.get(0), data);
        }
    }

    private void invokeApplication(String packageName, ResolveInfo resolveInfo, Uri data) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(data);
        intent.setPackage(packageName);

        startActivity(intent);
    }
}
