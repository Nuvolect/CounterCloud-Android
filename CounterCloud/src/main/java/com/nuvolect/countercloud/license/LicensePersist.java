/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.license;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.nuvolect.countercloud.main.CConst;
import com.nuvolect.countercloud.util.BetterCrypto;
import com.nuvolect.countercloud.util.Cryp;
import com.nuvolect.countercloud.util.JsonUtil;
import com.nuvolect.countercloud.util.TimeUtil;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("CommitPrefEdits")
public class LicensePersist {

    private static final String PERSIST_NAME           = "license_persist";

    // Persist keys
    private static final String LAST_PITCH             = "last_pitch";
    private static final String LEGAL_AGREE            = "legal_agree";
    private static final String LEGAL_AGREE_TIME       = "legal_agree_time";
    private static final String LICENSE_ACCOUNT_NAME   = "license_account_name";
    private static final String LICENSE_RESULT         = "license_result";
    private static final String LAST_NAG_TIME          = "last_nag_time";
    public static final CharSequence APP_LICENSE       = "app_license";// match settings.xml

    /**
     * Remove all persistent data.
     */
    public static void clearAll(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();
    }

    public static boolean getLegalAgree(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        return pref.getBoolean(LEGAL_AGREE, false);
    }

    public static void setLegalAgree(Context ctx, boolean legalAgree){

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putBoolean(LEGAL_AGREE, legalAgree).commit();
        pref.edit().putLong(LEGAL_AGREE_TIME, System.currentTimeMillis()).commit();
    }

    public static void setLicenseAccount(Context ctx, String accountName){
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        String cryptAccount = BetterCrypto.encrypt(ctx, accountName);
        pref.edit().putString(LICENSE_ACCOUNT_NAME, cryptAccount).commit();
    }

    /**
     * Get account that is associated with the license
     */
    public static String getLicenseAccount(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        String cryptAccount = pref.getString(LICENSE_ACCOUNT_NAME, "");
        if( cryptAccount.isEmpty())
            return CConst.DEFAULT_ACCOUNT;
        else{
            String clearText = BetterCrypto.decrypt(ctx, cryptAccount);

            return clearText;
        }
    }

    /**
     * Return the human readable summary of the current license.
     * @param ctx
     * @return
     */
    public static String getLicenseSummary(Context ctx) {

        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        long legalAgreeTime = pref.getLong(LEGAL_AGREE_TIME, 0L);

        LicenseManager.LicenseResult licenseResult = getLicenseResult(ctx);
        String licenseAccount = LicensePersist.getLicenseAccount(ctx);
        String summary = "";

        switch( licenseResult){

        case NIL:
            summary = "ERROR NIL license type";
            break;
        case PREMIUM_USER:
            summary = "License: Premium"
                    +"\nUser accepted terms "+ TimeUtil.friendlyTimeString(legalAgreeTime);
            break;
        case REJECTED_TERMS:
            summary = "User rejected terms "+TimeUtil.friendlyTimeString(legalAgreeTime);
            break;
        case WHITELIST_USER:
            summary = "License: Whitelist"
                    +"\nUser accepted terms "+TimeUtil.friendlyTimeString(legalAgreeTime);
            break;
        default:
            break;
        }
        return summary;
    }

    public static void setLicenseResult(Context ctx, LicenseManager.LicenseResult licenseResult) {
        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME,  Context.MODE_PRIVATE);
        pref.edit().putInt(LICENSE_RESULT, licenseResult.ordinal()).commit();
    }
    public static LicenseManager.LicenseResult getLicenseResult(Context ctx) {
        final SharedPreferences pref = ctx.getSharedPreferences( PERSIST_NAME, Context.MODE_PRIVATE);
        return LicenseManager.LicenseResult.values()[pref.getInt(LICENSE_RESULT, 0)];
    }

    /**
     * Keep track of when you are nagging the user on various issues.
     * Return false when current time is within a sincePeriod, it is not time to nag.
     * This method can can be called multiple times and will continue to return false until outside
     * of the pest period when it returns true a single time.
     *
     * @param ctx
     * @param key key to find last nag time
     * @param noNagPeriodMs  How long the period is in ms
     * @return
     */
    public static boolean timeToNagUser(Context ctx, String key, long noNagPeriodMs) {

        long currentTime = System.currentTimeMillis();

        final SharedPreferences pref = ctx.getSharedPreferences(PERSIST_NAME, Context.MODE_PRIVATE);
        try {

            JSONObject object = new JSONObject( pref.getString(LAST_NAG_TIME, "{}"));
            if( object.has( key )){

                long timeSinceLastNag = currentTime - JsonUtil.getLong(key, object);

                /**
                 * Check if we are outside the no-nag period
                 */
                if( timeSinceLastNag > noNagPeriodMs){

                    /**
                     * Time to nag the user again.
                     */
                    object.put(key, currentTime);
                    pref.edit().putString(LAST_NAG_TIME, object.toString()).commit();

                    return true;
                }else{
                    /**
                     * You are a pest and within the pest period, return true
                     */
                    return false;
                }
            }else{
                /**
                 * First time, definately time to nag the user
                 */
                object.put(key, currentTime);
                pref.edit().putString(LAST_NAG_TIME, object.toString()).commit();

                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Returns the current app version or zero when the app version has never been set.
     * @param ctx
     * @return
     */
    public static int getAppVersion(Context ctx) {

        return Cryp.getInt(ctx, Cryp.APP_VERSION);
    }

    public static void setAppVersion(Context ctx, int appVersion) {

        Cryp.putInt( ctx, Cryp.APP_VERSION, appVersion);
    }
}
