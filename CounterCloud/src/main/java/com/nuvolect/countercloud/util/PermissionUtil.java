/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;//

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Support user permission decisions.
 */
public class PermissionUtil {

    public static boolean canAccessReadContacts(Context ctx){

        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestReadContacts(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {

            act.requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS}, responseId);
        }else{

            ActivityCompat.requestPermissions(act,
                    new String[]{Manifest.permission.READ_CONTACTS}, responseId);
        }
    }
    public static void requestReadWriteContacts(Activity act, int responseId){

        if (Build.VERSION.SDK_INT >= 23) {

            act.requestPermissions(
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS},
                    responseId);
        }else{

            ActivityCompat.requestPermissions(act,
                    new String[]{
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS},
                    responseId);
        }
    }

    private static final String SCHEME = "package";
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 = "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
    }
}
