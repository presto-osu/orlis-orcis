/*
   Copyright (C) 2014  Samsung Electronics Polska Sp. z o.o.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU AFFERO General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    You may obtain a copy of the License at

                http://www.gnu.org/licenses/agpl-3.0.txt

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.samsung.srpol.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.XmlResourceParser;
import android.support.v4.content.AsyncTaskLoader;

import com.samsung.srpol.R;
import com.samsung.srpol.data.Category;
import com.samsung.srpol.data.Subcategory;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AppListLoader extends AsyncTaskLoader<List<AppDetails>> {
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String PHONE = "com.android.phone";

    public static final String PREF_INCLUDE_SYSTEM_APPS = "include_system_apps";

    private final PackageManager mPm;

    private static List<AppDetails> mAppDetailsList = null;
    private static List<Category> mCategories;
    private static ArrayList<Subcategory> mSubcategories;
    private static HashMap<String, Integer> mAllPermissionsHash;

    private PackageIntentReceiver mPackageObserver;
    private boolean mWasDataReloaded = true;
    private static OnAppRemoveListener mChangeListener = null;

    public AppListLoader(Context context) {
        super(context);

        if (mCategories == null || mSubcategories == null
                || mAllPermissionsHash == null)
            initCategories(context);

        mPm = getContext().getPackageManager();
    }

    private void initCategories(Context context) {
        mCategories = new ArrayList<Category>();
        mSubcategories = new ArrayList<Subcategory>();
        Subcategory.resetGenerator();

        // Reading "data send" subcategory as it is common
        try {
            XmlResourceParser parser = context.getResources().getXml(
                    R.xml.data_send_subcategory);
            int eventType = 0;
            while (eventType != XmlResourceParser.END_DOCUMENT
                    && eventType != XmlResourceParser.START_TAG)
                eventType = parser.next();
            if (eventType != XmlResourceParser.END_DOCUMENT) {
                Subcategory subcategory = readSubCategory(parser);
                mSubcategories.add(subcategory);
            }
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Reading all categories and subcategories from xml
        try {
            XmlResourceParser parser = context.getResources().getXml(
                    R.xml.categories);
            int eventType = 0;
            while (eventType != XmlResourceParser.END_DOCUMENT
                    && eventType != XmlResourceParser.START_TAG)
                eventType = parser.next();
            if (eventType != XmlResourceParser.END_DOCUMENT) {
                parser.require(XmlResourceParser.START_TAG, null, "container");
                while (parser.next() != XmlResourceParser.END_TAG) {
                    if (parser.getEventType() != XmlResourceParser.START_TAG)
                        continue;
                    if (!parser.getName().equals("category"))
                        continue;
                    Category cat = readCategory(parser);
                    mCategories.add(cat);
                    mSubcategories.addAll(cat.getSubCategories());
                }
            }
        } catch (XmlPullParserException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mAllPermissionsHash = new HashMap<String, Integer>();
        for (Subcategory subcat : mSubcategories) {
            for (String perm : subcat.getPermissions()) {
                Integer value = mAllPermissionsHash.get(perm);
                if (value == null) {
                    mAllPermissionsHash.put(perm, Integer.valueOf(subcat.getId()));
                } else {
                    value |= subcat.getId();
                    mAllPermissionsHash.put(perm, value);
                }
            }
        }
    }

    private Category readCategory(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlResourceParser.START_TAG, null, "category");
        String title = null, header = null, description = null, shortDescription = null, link = null;
        int icon = 0;
        boolean dataSend = false;
        ArrayList<Subcategory> subCategories = new ArrayList<Subcategory>();

        while (parser.next() != XmlResourceParser.END_TAG) {
            if (parser.getEventType() != XmlResourceParser.START_TAG)
                continue;
            String name = parser.getName();
            if (name.equals("title")) {
                title = readTextElement(parser, name);
            } else if (name.equals("header")) {
                header = readTextElement(parser, name);
            } else if (name.equals("short_description")) {
                shortDescription = readTextElement(parser, name);
            } else if (name.equals("description")) {
                description = readTextElement(parser, name);
            } else if (name.equals("icon")) {
                icon = getContext().getResources().getIdentifier(
                        readTextElement(parser, name), "drawable",
                        getContext().getPackageName());
            } else if (name.equals("link")) {
                link = readTextElement(parser, name);
            } else if (name.equals("dataSend")) {
                dataSend = Boolean.parseBoolean(readTextElement(parser, name));
            } else if (name.equals("subcategories")) {
                while (parser.next() != XmlResourceParser.END_TAG) {
                    subCategories.add(readSubCategory(parser));
                }
            } else
                skip(parser);
        }
        return new Category(getContext(), title, header, shortDescription,
                description, icon, link, dataSend, subCategories);
    }

    private Subcategory readSubCategory(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        parser.require(XmlResourceParser.START_TAG, null, "subcategory");
        String header = null, description = null, icon = null;
        ArrayList<String> permissions = new ArrayList<String>();

        while (parser.next() != XmlResourceParser.END_TAG) {
            if (parser.getEventType() != XmlResourceParser.START_TAG)
                continue;
            String name = parser.getName();
            if (name.equals("header")) {
                header = readTextElement(parser, name);
            } else if (name.equals("description")) {
                description = readTextElement(parser, name);
            } else if (name.equals("icon")) {
                icon = readTextElement(parser, name);
            } else if (name.equals("permissions")) {
                while (parser.next() != XmlResourceParser.END_TAG) {
                    permissions.add(readTextElement(parser, "item"));
                }
            } else
                skip(parser);
        }

        return new Subcategory(getContext(), header, description, icon,
                permissions);
    }

    private String readTextElement(XmlResourceParser parser, String element)
            throws XmlPullParserException, IOException {
        parser.require(XmlResourceParser.START_TAG, null, element);
        String title = readText(parser);
        parser.require(XmlResourceParser.END_TAG, null, element);
        return title;
    }

    private String readText(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        String result = "";
        if (parser.next() == XmlResourceParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlResourceParser parser) throws XmlPullParserException,
            IOException {
        if (parser.getEventType() != XmlResourceParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
            case XmlResourceParser.END_TAG:
                depth--;
                break;
            case XmlResourceParser.START_TAG:
                depth++;
                break;
            }
        }
    }

    /**
     * Get Signature keys of given package
     * 
     * @param packageName
     *            package name to retrieve signature keys
     * @return Signature[] containing given package name signature keys
     */
    private Signature[] getSignature(String packageName) {

        try {
            PackageInfo packageInfo = getPm().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);
            Signature[] sigs = packageInfo.signatures;
            return sigs;
        } catch (NameNotFoundException e) {
            return null;
        }

    }

    /**
     * Get Signature keys for given PackageInfo
     * 
     * @param packageInfo
     *            PackageInfo to retrieve signature keys
     * @return Signature[] containing given PackageInfo signature keys
     */
    @SuppressWarnings("unused")
    private Signature[] getSignature(PackageInfo packageInfo) {

        Signature[] sigs = packageInfo.signatures;
        return sigs;

    }

    /**
     * Get Platform signature key. we check signature of SystemUI process or
     * Phone process
     * 
     * @return Signature platform sign key or null if not found
     */
    @SuppressWarnings("unused")
    private Signature getPlatformSignature() {
        Signature sig = getSignature(SYSTEM_UI)[0];
        if (sig == null) {
            sig = getSignature(PHONE)[0];
        }
        return sig;
    }

    /**
     * Filter given ApplicationInfo list only to apps that are not platform
     * signed
     * 
     * @param appList
     *            List<ApplicationInfo> with a list of apps to filter
     * @return List<ApplicationInfo> filtred app list
     */
    private List<PackageInfo> filterToNotPlatformKeyAppList(List<PackageInfo> packageList) {
        if (packageList != null) {

            List<PackageInfo> noPlatformKeyAppList = new ArrayList<PackageInfo>();
            for (PackageInfo packageInfo : packageList) {
                try {
                    packageInfo = mPm.getPackageInfo(packageInfo.packageName,
                            PackageManager.GET_ACTIVITIES
                                    | PackageManager.GET_PERMISSIONS
                                    | PackageManager.GET_SIGNATURES);
                } catch (NameNotFoundException e) {
                    continue;
                }
                // Check signature match with SystemUI
                if (((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || (packageInfo.activities != null && packageInfo.activities.length > 0))
                        && getPm().checkSignatures(SYSTEM_UI,
                                packageInfo.packageName) != PackageManager.SIGNATURE_MATCH) {
                    noPlatformKeyAppList.add(packageInfo);
                }
            }
            return noPlatformKeyAppList;
        } else {
            return null;
        }
    }

    private PackageInfo getInstalledAppDetails(String packageName) {
        PackageInfo info = null;
        try {
            info = getPm().getPackageInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                            | PackageManager.GET_PERMISSIONS
                            | PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    public List<AppDetails> loadInBackground() {
        List<PackageInfo> appList = mPm.getInstalledPackages(0);

        appList = filterToNotPlatformKeyAppList(appList);

        HashMap<String, AppDetails> appDetailsHash = new HashMap<String, AppDetails>(
                appList.size());
        if (mSubcategories.size() == 0) {
            for (PackageInfo pInfo : appList) {
                appDetailsHash.put(pInfo.packageName, new AppDetails(this,
                        pInfo));
            }
        } else {
            for (PackageInfo packageInfo : appList) {
                String[] requestedPermissions = packageInfo.requestedPermissions;
                if (requestedPermissions != null) {
                    AppDetails appDetails = appDetailsHash.get(packageInfo.packageName);
                    for (String permissionName : requestedPermissions) {
                        Integer subcategories = mAllPermissionsHash.get(permissionName);
                        if (subcategories != null 
                                && (appDetails == null || !appDetails.isInAllSubcategories(subcategories))
                                && getPm().checkPermission(permissionName,
                                        packageInfo.packageName) == PackageManager.PERMISSION_GRANTED) {
                            if (appDetails == null) {
                                appDetails = new AppDetails(this, packageInfo);
                                appDetailsHash.put(packageInfo.packageName,
                                        appDetails);
                            }
                            appDetails.addSubcategory(subcategories);
                        }
                    }
                }
            }
        }
        // Sort the list.
        List<AppDetails> list = new ArrayList<AppDetails>(
                appDetailsHash.values());
        Collections.sort(list, AppDetails.SMART_COMPARATOR);

        for (Category category : mCategories)
            category.assignAppsToCategory(list);

        mWasDataReloaded = true;
        return list;
    }

    /**
     * Called when there is new data to deliver to the client. The super class
     * will take care of delivering it; the implementation here just adds a
     * little more logic.
     */
    @Override
    public void deliverResult(List<AppDetails> apps) {
        if (isReset()) {
            apps = null;
        }
        mAppDetailsList = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (mAppDetailsList != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mAppDetailsList);
        }

        // Start watching for changes in the app data.
        if (mPackageObserver == null) {
            mPackageObserver = new PackageIntentReceiver(this);
        }

        if (takeContentChanged() || mAppDetailsList == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {

        // Ensure the loader is stopped
        onStopLoading();

        // Stop monitoring for changes.
        if (mPackageObserver != null) {
            getContext().unregisterReceiver(mPackageObserver);
            mPackageObserver = null;
        }
    }

    /**
     * Gets AppDetails from mAppDetailsList for a given PackageName
     * 
     * @param packageName
     *            PackageName for which to get AppDetails
     * @return AppDetails for given PackageName
     */
    public static AppDetails getAppDetails(String packageName) {
        if (packageName != null && mAppDetailsList != null) {
            for (AppDetails appDetails : mAppDetailsList) {
                if (packageName.equals(appDetails.getAppPackageName())) {
                    return appDetails;
                }
            }
        }
        return null;
    }

    public static void setOnChangeListener(OnAppRemoveListener listener) {
        mChangeListener = listener;
    }

    /**
     * @return the mAppDetailsList
     */
    public static List<AppDetails> getAppDetailsList() {
        return mAppDetailsList;
    }

    /**
     * @return the mCategories
     */
    static public List<Category> getCategories() {
        return mCategories;
    }

    public static List<Subcategory> getSubcategoriesOfMask(int subcategoriesMask) {
        ArrayList<Subcategory> list = new ArrayList<Subcategory>();
        for (Subcategory subcategory : mSubcategories) {
            if ((subcategoriesMask & subcategory.getId()) > 0) {
                list.add(subcategory);
            }
        }
        return list;
    }

    public PackageManager getPm() {
        return mPm;
    }

    public interface OnAppRemoveListener {
        public void onPackageRemoved(String packageName);
    }

    /**
     * Helper class to look for interesting changes to the installed apps so
     * that the loader can be updated.
     */
    public static class PackageIntentReceiver extends BroadcastReceiver {
        private final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader) {
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);
            // Register for events related to sdcard installation.
            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)) {
                mLoader.onPackagesAdded(intent
                        .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST));
            } else if (intent.getAction().equals(
                    Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE)) {
                mLoader.onPackagesRemoved(intent
                        .getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST));
            } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
                mLoader.onPackageRemoved(intent.getData()
                        .getSchemeSpecificPart(), true);
            else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED))
                mLoader.onPackageAdded(
                        intent.getData().getSchemeSpecificPart(), true);
            else if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED))
                mLoader.onPackageChanged(intent.getData()
                        .getSchemeSpecificPart());
        }
    }

    public void onPackageRemoved(String packageName, boolean deliverResult) {
        if (mChangeListener != null)
            mChangeListener.onPackageRemoved(packageName);
        for (AppDetails details : mAppDetailsList) {
            if (details.getAppPackageName().equals(packageName)) {
                mAppDetailsList.remove(details);
                for (Category category : mCategories)
                    category.removeAppFromList(details);
                if (deliverResult)
                    deliverResult(new ArrayList<AppDetails>(mAppDetailsList));
                break;
            }
        }
    }

    public void onPackagesRemoved(String[] packageNames) {
        for (String packageName : packageNames) {
            onPackageRemoved(packageName, false);
        }
        deliverResult(new ArrayList<AppDetails>(mAppDetailsList));
    }

    public void onPackageChanged(String packageName) {
        for (AppDetails details : mAppDetailsList) {
            if (details.getAppPackageName().equals(packageName)) {
                PackageInfo packageInfo = getInstalledAppDetails(packageName);
                if (packageInfo == null)
                    return;
                details.setEnabled(packageInfo.applicationInfo.enabled);
                deliverResult(new ArrayList<AppDetails>(mAppDetailsList));
                break;
            }
        }
    }

    public void onPackageAdded(String packageName, boolean deliverResult) {
        PackageInfo packageInfo = getInstalledAppDetails(packageName);
        if (packageInfo == null)
            return;
        AppDetails newPackage = new AppDetails(this, packageInfo);

        String[] requestedPermissions = packageInfo.requestedPermissions;
        if (requestedPermissions != null && mSubcategories != null) {
            for (String permissionName : requestedPermissions) {
                Integer subcategories = mAllPermissionsHash.get(permissionName);
                if (subcategories != null
                        && getPm().checkPermission(permissionName,
                                packageInfo.packageName) == PackageManager.PERMISSION_GRANTED) {
                    newPackage.addSubcategory(subcategories);
                }
            }
        }
        mAppDetailsList.add(newPackage);

        for (Category category : mCategories)
            category.addApplicationToCategory(newPackage);
        if (deliverResult)
            deliverResult(new ArrayList<AppDetails>(mAppDetailsList));
    }

    public void onPackagesAdded(String[] packageNames) {
        for (String packageStr : packageNames) {
            onPackageAdded(packageStr, false);
        }

        deliverResult(new ArrayList<AppDetails>(mAppDetailsList));
    }

    public boolean wasDataReloaded() {
        return mWasDataReloaded;
    }

    public void resetWasDataReloaded() {
        mWasDataReloaded = false;
    }
}