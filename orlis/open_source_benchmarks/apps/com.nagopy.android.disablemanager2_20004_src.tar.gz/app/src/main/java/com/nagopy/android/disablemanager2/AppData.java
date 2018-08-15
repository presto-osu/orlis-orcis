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
import android.os.Build;

import com.nagopy.android.disablemanager2.support.DisableableFilter;

import java.lang.ref.WeakReference;
import java.util.List;

public class AppData {
    /**
     * ラベル名
     */
    public String label;

    /**
     * パッケージ名
     */
    public String packageName;

    /**
     * 有効かどうか
     */
    public boolean isEnabled;

    /**
     * システムアプリかどうか
     */
    public boolean isSystem;

    /**
     * 無効化が可能かどうか
     */
    public boolean isDisableable;

    /**
     * アプリアイコン
     */
    public WeakReference<Drawable> icon;

    /**
     * 実行中のプロセスの情報
     */
    public List<String> process;

    /**
     * 実行ユーザーでインストールされているか.
     * API17以上で使用するフラグ。
     */
    public boolean isInstalled = true;

    public AppData(PackageManager packageManager, DisableableFilter filter, ApplicationInfo applicationInfo) {
        this.label = applicationInfo.loadLabel(packageManager).toString();
        this.packageName = applicationInfo.packageName;
        this.isEnabled = applicationInfo.enabled;
        this.isSystem = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0
                || (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        this.isDisableable = filter.isDisableable(applicationInfo.packageName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 4.2以上。4.1以下は無条件でtrue
            this.isInstalled = (applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0;
        }
    }

    @Override
    public String toString() {
        return packageName
                + ", label=" + label
                + ", enabled=" + isEnabled
                + ", system=" + isSystem
                + ", disableable=" + isDisableable
                + ", installed=" + isInstalled;
    }
}
