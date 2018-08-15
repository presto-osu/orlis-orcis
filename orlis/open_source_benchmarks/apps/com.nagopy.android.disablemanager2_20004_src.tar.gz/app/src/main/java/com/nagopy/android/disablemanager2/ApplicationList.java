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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;

import com.nagopy.android.disablemanager2.support.AppDataComparator;
import com.nagopy.android.disablemanager2.support.DebugUtil;
import com.nagopy.android.disablemanager2.support.DisableableFilter;
import com.nagopy.android.disablemanager2.support.FieldReflectWrapper;
import com.nagopy.android.disablemanager2.support.Logic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationList {

    private static final ApplicationList instance = new ApplicationList();

    protected List<AppData> appList;
    Set<ApplicationLoadListener> listeners;

    final Object lockObject = new Object();

    private ApplicationList() {
        appList = Collections.emptyList();
        listeners = new LinkedHashSet<>();
    }

    public static synchronized ApplicationList getInstance() {
        return instance;
    }

    public void loadApplicationList(Context context, ApplicationLoadListener listener) {
        DebugUtil.verboseLog("loadApplicationList start");
        synchronized (lockObject) {
            DebugUtil.verboseLog("loadApplicationList lock start");
            if (appList == null || appList.size() == 0) {
                listeners.add(listener);
                new ApplicationLoadTask(this).execute(context);
                DebugUtil.verboseLog("loader execute");
            } else {
                DebugUtil.verboseLog("onLoaded(" + appList + ")");
                listener.onLoaded(appList);
            }
            DebugUtil.verboseLog("loadApplicationList lock end");
        }
        DebugUtil.verboseLog("loadApplicationList end");
    }

    private static class ApplicationLoadTask extends AsyncTask<Context, Void, Void> {

        private WeakReference<ApplicationList> applicationListWeakReference;

        public ApplicationLoadTask(ApplicationList applicationList) {
            applicationListWeakReference = new WeakReference<>(applicationList);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        protected Void doInBackground(Context... params) {
            ApplicationList applicationList = applicationListWeakReference.get();
            if (applicationList == null) {
                return null;
            }
            DebugUtil.verboseLog("doInBackground start");
            synchronized (applicationList.lockObject) {
                DebugUtil.verboseLog("doInBackground lockObject start");
                final Context context = params[0];

                Map<String, List<String>> runningProcessMap = applicationList.getRunningStatusMap(context);

                // 取得する際のフラグ
                // 設定画面のフラグ＋無効化可能判定用にシグネチャ
                int mRetrieveFlags = Logic.getRetrieveFlags() | PackageManager.GET_SIGNATURES;

                // ApplicationInfo.enabledSettingをリフレクションで取得する準備
                FieldReflectWrapper enabledSettingField = new FieldReflectWrapper(ApplicationInfo.class, "enabledSetting");

                DisableableFilter disableableFilter = new DisableableFilter(context);
                PackageManager packageManager = context.getPackageManager();
                List<ApplicationInfo> applicationInfo = packageManager.getInstalledApplications(mRetrieveFlags);
                List<AppData> appList = new ArrayList<>(applicationInfo.size());
                for (ApplicationInfo info : applicationInfo) {
                    if (!info.enabled) {
                        // 無効になっていて、かつenabledSettingが3でないアプリは除外する
                        int enabledSetting = enabledSettingField.getInt(info);
                        if (enabledSetting
                                != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
                            DebugUtil.verboseLog("skip " + info.packageName);
                            continue;
                        }
                    }

                    AppData appData = new AppData(packageManager, disableableFilter, info);
                    List<String> processList = runningProcessMap.get(info.packageName);
                    if (processList != null) {
                        Collections.sort(processList);
                        appData.process = processList;
                    } else {
                        appData.process = Collections.emptyList();
                    }
                    appList.add(appData);
                }
                Collections.sort(appList, AppDataComparator.getInstance());

                applicationList.appList = appList;
                for (ApplicationLoadListener listener : applicationList.listeners) {
                    listener.onLoaded(appList);
                }
                applicationList.listeners.clear();
                DebugUtil.verboseLog("doInBackground lockObject end");
            }
            DebugUtil.verboseLog("doInBackground end");
            return null;
        }
    }

    public interface ApplicationLoadListener {
        void onLoaded(List<AppData> appList);
    }

    protected Map<String, List<String>> runningStatusMap;

    public synchronized Map<String, List<String>> getRunningStatusMap(Context context) {
        if (runningStatusMap == null) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList = activityManager.getRunningAppProcesses();

            runningStatusMap = new HashMap<>();
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfoList) {
                String[] pkgList = runningAppProcessInfo.pkgList;
                for (String pkg : pkgList) {
                    List<String> list = runningStatusMap.get(pkg);
                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    if (pkg.equals(runningAppProcessInfo.processName)) {
                        list.add("[" + Logic.getStatusText(runningAppProcessInfo.importance) + "]");
                    } else {
                        list.add(runningAppProcessInfo.processName + " [" + Logic.getStatusText(runningAppProcessInfo.importance) + "]");
                    }
                    runningStatusMap.put(pkg, list);
                }
            }
        }
        return runningStatusMap;
    }


}
