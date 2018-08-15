package com.nagopy.android.disablemanager2.support;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 無効化可能アプリを判定するためのクラス.
 */
public class DisableableFilter {

    private DevicePolicyManagerWrapper devicePolicyManagerWrapper;
    private PackageManager packageManager;
    private List<String> homePackages;
    private static final int FLAGS = PackageManager.GET_DISABLED_COMPONENTS | PackageManager.GET_UNINSTALLED_PACKAGES
            | PackageManager.GET_SIGNATURES;

    public DisableableFilter(Context context) {
        packageManager = context.getPackageManager();
        devicePolicyManagerWrapper = new DevicePolicyManagerWrapper(context);

        // ホームアプリのリスト作成
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // 4,3以下
            homePackages = getHomePackages();
        } else {
            // 4.4以上
            homePackages = getHomePackagesApi19();
        }
    }

    /**
     * ホームアプリのパッケージ名を取得するためのAPI19未満のメソッド.
     *
     * @return ホームアプリのパッケージ名
     */
    protected List<String> getHomePackages() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> homeActivities = packageManager.queryIntentActivities(intent, 0);
        List<String> homePackages = new ArrayList<>();
        for (ResolveInfo ri : homeActivities) {
            final String activityPkg = ri.activityInfo.packageName;
            homePackages.add(activityPkg);
        }
        return homePackages;
    }

    /**
     * ホームアプリのパッケージ名を取得するためのAPI19以上のメソッド.
     *
     * @return ホームアプリのパッケージ名
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected List<String> getHomePackagesApi19() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> homeActivities = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        List<String> homePackages = new ArrayList<>();
        for (ResolveInfo ri : homeActivities) {
            final String activityPkg = ri.activityInfo.packageName;
            homePackages.add(activityPkg);

            // Also make sure to include anything proxying for the home app
            final Bundle metadata = ri.activityInfo.metaData;
            if (metadata != null) {
                final String metaPkg = metadata.getString(ActivityManager.META_HOME_ALTERNATE);
                if (signaturesMatch(metaPkg, activityPkg)) {
                    homePackages.add(metaPkg);
                }
            }
        }
        return homePackages;
    }

    /**
     * InstalledAppDetailsのメソッド
     */
    private boolean signaturesMatch(String pkg1, String pkg2) {
        if (pkg1 != null && pkg2 != null) {
            try {
                final int match = packageManager.checkSignatures(pkg1, pkg2);
                if (match >= PackageManager.SIGNATURE_MATCH) {
                    return true;
                }
            } catch (Exception e) {
                // e.g. named alternate package not found during lookup;
                // this is an expected case sometimes
            }
        }
        return false;
    }

    /**
     * 無効化可能かを判定する.
     *
     * @param packageName パッケージ名
     * @return 無効化可能ならtrue
     */
    public boolean isDisableable(String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, FLAGS);
        } catch (PackageManager.NameNotFoundException e) {
            DebugUtil.errorLog("Package not found:" + packageName);
            return false;
        }

        if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            // 非システムアプリは無効化不可
            return false;
        }

        if (homePackages.contains(packageInfo.applicationInfo.packageName)) {
            // ホームアプリの場合は無効化不可
            return false;
        }

        if (devicePolicyManagerWrapper.isThisASystemPackage(packageInfo)) {
            // Coreなシステムアプリの場合は無効化不可
            return false;
        }

        if (devicePolicyManagerWrapper.packageHasActiveAdmins(packageInfo.packageName)) {
            // 停止・アンインストール不可アプリの場合は無効化不可
            return false;
        }

        // 上記試練を乗り越えたアプリは無効化可能
        return true;
    }

    public static class DevicePolicyManagerWrapper {

        private DevicePolicyManager devicePolicyManager;
        /**
         * システムのPackageInfo
         */
        private PackageInfo mSystemPackageInfo;
        private boolean enableSystemPackageInfo;

        private Method packageHasActiveAdmins;
        private boolean enablePackageHasActiveAdminsMethod;

        public DevicePolicyManagerWrapper(Context context) {
            devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                packageHasActiveAdmins = DevicePolicyManager.class.getDeclaredMethod("packageHasActiveAdmins", String.class);
                enablePackageHasActiveAdminsMethod = true;
            } catch (NoSuchMethodException e) {
                DebugUtil.errorLog("リフレクション失敗:" + e.getMessage());
                enablePackageHasActiveAdminsMethod = false;
            }

            try {
                mSystemPackageInfo = context.getPackageManager().getPackageInfo("android", PackageManager.GET_SIGNATURES);
                enableSystemPackageInfo = true;
            } catch (PackageManager.NameNotFoundException e) {
                DebugUtil.errorLog("システムのシグネチャ取得に失敗:" + e.getMessage());
                enableSystemPackageInfo = false;
            }
        }

        /**
         * {@link android.app.admin.DevicePolicyManager}のpackageHasActiveAdminsメソッドを実行する
         *
         * @param packageName パッケージ名
         * @return packageHasActiveAdminsの結果を返す。<br>
         * エラーがあった場合はfalseを返す。
         */
        public boolean packageHasActiveAdmins(String packageName) {
            try {
                return enablePackageHasActiveAdminsMethod &&
                        (boolean) packageHasActiveAdmins.invoke(devicePolicyManager, packageName);
            } catch (IllegalAccessException | InvocationTargetException e) {
                DebugUtil.errorLog("実行失敗:" + e.getMessage());
                return false;
            }
        }

        /**
         * {@link android.app.admin.DevicePolicyManager}のisThisASystemPackageメソッドと同じ内容.<br>
         * 4.4以下で使用。
         *
         * @param packageInfo 判定したいpackageinfo
         * @return isThisASystemPackageの結果をそのまま返す。<br>
         * エラーがあった場合はfalseを返す。
         */
        protected boolean isThisASystemPackage(PackageInfo packageInfo) {
            return enableSystemPackageInfo &&
                    (packageInfo != null && packageInfo.signatures != null && mSystemPackageInfo != null && mSystemPackageInfo.signatures[0]
                            .equals(packageInfo.signatures[0]));
        }
    }

}
