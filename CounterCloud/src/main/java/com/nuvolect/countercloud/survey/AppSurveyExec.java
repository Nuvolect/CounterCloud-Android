/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.survey;//

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Utilities to generate app permission metrics.
 */
public class AppSurveyExec {

    public static int getContactsAppsTotal(Context ctx) {

        int appsAccessingContacts = 0;

        PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                String[] requestedPermissions = packageInfo.requestedPermissions;

                if(requestedPermissions != null) {

                    for (String requestedPermission : requestedPermissions) {

                        if (requestedPermission.contains("CONTACTS")) {

                            ++appsAccessingContacts;
                            break;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return appsAccessingContacts;
    }

    /**
     * Read the app database and return it as a JSON object.
     * @param ctx
     * @return
     */
    public static JSONObject getAppDb(Context ctx){

        /**
         * Load packages from raw resources into a JSON object
         */
        String fileContents = "";
        StringBuilder sb = new StringBuilder();
        JSONObject jsonObject = new JSONObject();

        try {
            InputStream is = ctx.getResources().openRawResource(R.raw.app_survey_data);

            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {

                String s = new String( buffer, 0, len, "UTF-8");
                sb.append( s );
            }
            fileContents = sb.toString();

            if( is != null)
                is.close();
        } catch (FileNotFoundException e) {
            LogUtil.logException(LogUtil.LogType.APP_SURVEY, e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * Build the JSON array
         */
        try {

            jsonObject= new JSONObject( fileContents );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
