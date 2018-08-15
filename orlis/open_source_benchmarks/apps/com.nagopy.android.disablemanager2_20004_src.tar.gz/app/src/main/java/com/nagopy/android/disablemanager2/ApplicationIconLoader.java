/*
 * Copyright (C) 2015 75py
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nagopy.android.disablemanager2;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

import com.nagopy.android.disablemanager2.support.DebugUtil;
import com.nagopy.android.disablemanager2.support.Logic;

import java.lang.ref.WeakReference;

/**
 * アイコン読み込みを非同期で行うためのクラス.
 */
public class ApplicationIconLoader extends AsyncTask<AppData, Void, AppData> {
    private final WeakReference<PackageManager> packageManagerWeakReference;
    private final WeakReference<TextView> textViewWeakReference;
    private final int iconSize;
    private static final int RETRIEVE_FLAGS = Logic.getRetrieveFlags();
    private final String packageName;

    /**
     * コンストラクタ.
     *
     * @param packageName    対象パッケージ名
     * @param packageManager {@link android.content.pm.PackageManager}
     * @param iconSize       ランチャーのアイコンサイズ（px）
     * @param textView       表示対象のView
     */
    public ApplicationIconLoader(String packageName, PackageManager packageManager, int iconSize, TextView textView) {
        this.packageName = packageName;
        this.packageManagerWeakReference = new WeakReference<>(packageManager);
        this.iconSize = iconSize;
        this.textViewWeakReference = new WeakReference<>(textView);
    }

    /**
     * 読み込み処理.<br>
     * キャッシュが存在しない場合は読み込みを行う。
     *
     * @param params 対象アプリ情報（可変長だが一つ目のみ使用する）
     * @return パラメータで渡されたものと同じインスタンス
     */
    @Override
    protected AppData doInBackground(AppData... params) {
        AppData appData = params[0];
        TextView textView = textViewWeakReference.get();
        PackageManager packageManager = packageManagerWeakReference.get();
        if (textView == null || packageManager == null) {
            return null;
        }
        DebugUtil.verboseLog("doInBackground :" + appData.packageName);

        if (appData.icon != null && appData.icon.get() != null) {
            DebugUtil.verboseLog("use cache icon :" + appData.packageName);
            return appData;
        }

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(appData.packageName, RETRIEVE_FLAGS);
            Drawable icon = applicationInfo.loadIcon(packageManager);
            appData.icon = new WeakReference<>(icon);
            DebugUtil.verboseLog("load icon complete :" + appData.packageName);
            return appData;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("ApplicationInfoの取得に失敗 packageName=" + appData.packageName, e);
        }
    }

    /**
     * 後処理.<br>
     * TextViewがこのクラスが変更するパッケージのものの場合、アイコンを反映する
     * （ListViewではViewが使いまわされるため、アイコン読み込み後には別アプリの表示用Viewに変わっている場合がある）。<br>
     * TextViewの変更時は、対象TextViewの操作をロックする。
     *
     * @param appData アプリ情報
     */
    @Override
    protected void onPostExecute(AppData appData) {
        TextView textView = textViewWeakReference.get();
        if (textView == null) {
            return;
        }
        Drawable icon = appData.icon.get();
        if (icon == null) {
            return;
        }
        synchronized (textView.getTag()) {
            if (packageName.equals(textView.getTag(R.id.tag_package_name))) {
                DebugUtil.verboseLog("onPostExecute updateIcon :" + packageName);
                Logic.setIcon(textView, icon, iconSize);
            }
        }
    }
}