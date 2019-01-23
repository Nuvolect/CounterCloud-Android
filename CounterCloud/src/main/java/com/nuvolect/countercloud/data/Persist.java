/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.nuvolect.countercloud.main.CloudManagerFragment.CloudManagerMode;

public class Persist {

    private static final String PERSIST_NAME           = "cc_persist";

    // Persist keys
    private static final String APP_VERSION            = "app_version";
	private static final String CLOUD_MGR_MODE         = "cloud_mgr_mode";
    private static final String SHOW_TIP_CURRENT       = "show_tip_current";
    private static final String TIME_LAST_UPDATE       = "time_last_update";

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    /**
     * Simple get.  Return empty string if not found
     * @param ctx
     * @param key
     * @return
     */
    public static String get(Context ctx, String key) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getString(key, "");
    }

    /**
     * Simple put value with the given key.
     * @param ctx
     * @param key
     * @param value
     */
    public static void put(Context ctx, String key, String value){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putString(key, value).commit();
    }

    public static void setAppVersion(Context ctx, int appVersion) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putInt(APP_VERSION, appVersion).commit();
    }

    public static int getAppVersion(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(APP_VERSION, 0);
    }

    public static void setTimeLastUpdate(Context ctx, Long time) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putLong(TIME_LAST_UPDATE, time).commit();
    }

	public static long getTimeLastUpdate(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getLong(TIME_LAST_UPDATE, 0L);
	}

    /** Save the current mode of the Cloud manager */
    public static CloudManagerMode getCloudManagerMode(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);

        // Stored as an int, get saved value and restore to the enum type
        int spinType = pref.getInt(CLOUD_MGR_MODE, CloudManagerMode.RAW_CONTACTS.ordinal());
        return CloudManagerMode.values()[spinType];
    }

    /** Return the current mode of the Cloud Manager */
    public static void setCloudManagerMode(Context ctx, CloudManagerMode managerMode){

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putInt(CLOUD_MGR_MODE, managerMode.ordinal()).commit();
    }

    public static void setCurrentTip(Context ctx, int tipIndex){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(SHOW_TIP_CURRENT, tipIndex).commit();
    }
    public static int getCurrentTip(Context ctx){
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return pref.getInt(SHOW_TIP_CURRENT, -1);
    }
}

