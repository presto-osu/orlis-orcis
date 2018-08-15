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
package com.nagopy.android.disablemanager2.support;

import com.nagopy.android.disablemanager2.AppData;

import java.util.Comparator;

public class AppDataComparator implements Comparator<AppData> {

    private static final AppDataComparator instance = new AppDataComparator();

    public static AppDataComparator getInstance() {
        return instance;
    }

    private AppDataComparator() {
    }

    @Override
    public int compare(AppData lhs, AppData rhs) {
        if (lhs.isInstalled != rhs.isInstalled) {
            // 「未インストール」は最後に
            return lhs.isInstalled ? -1 : 1;
        }

        String label0 = lhs.label;
        String label1 = rhs.label;

        int ret = label0.compareToIgnoreCase(label1);
        // ラベルで並び替え、同じラベルがあったらパッケージ名で
        if (ret == 0) {
            String pkgName0 = lhs.packageName;
            String pkgName1 = rhs.packageName;
            ret = pkgName0.compareToIgnoreCase(pkgName1);
        }
        return ret;
    }
}
