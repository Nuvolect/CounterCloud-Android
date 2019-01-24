/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.license;

import android.content.Context;
import android.content.pm.PackageManager;

import com.nuvolect.countercloud.data.Persist;

public class LicenseUtil {

    /**
     * Detect if the app has been upgraded.
     * A new installation returns false, i.e, is not an upgrade,
     * it is a new installation.
     * @param ctx
     * @return  True for an app upgrade, false when not upgraded and for new installation.
     */
    public static boolean appUpgraded(Context ctx) {

        int appVersion = 0;
        try {
            appVersion = ctx.getPackageManager().getPackageInfo(
                    ctx.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int previousAppVersion = Persist.getAppVersion( ctx);

        if( appVersion > previousAppVersion) {

            Persist.setAppVersion( ctx, appVersion);

            if (previousAppVersion > 0)
                return true;  // App is upgraded and not a new install
        }
        return false;
    }
}
