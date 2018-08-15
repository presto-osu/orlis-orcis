package org.pixmob.freemobile.netstat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.pixmob.freemobile.netstat.ui.Netstat;

public class PermissionsManager {
    public static int checkSelfPermissions(@NonNull Context context, final String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return PackageManager.PERMISSION_DENIED;
            }
        }

        return PackageManager.PERMISSION_GRANTED;
    }

    public static int checkRequiredPermissions(@NonNull Context context) {
        return checkSelfPermissions(context, Netstat.REQUIRED_PERMISSIONS);
    }

    public static boolean shouldShowRequestPermissionsRationale(@NonNull Activity activity, final String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }

        return false;
    }
}
