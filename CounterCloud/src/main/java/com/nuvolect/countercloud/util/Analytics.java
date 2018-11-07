/*******************************************************************************
 * Copyright (c) 2011 - 2014, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC.
 * Any unauthorized use is strictly prohibited.
 ******************************************************************************/
package com.nuvolect.countercloud.util;

import android.app.Activity;
import android.content.Context;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.nuvolect.countercloud.util.LogUtil.LogType;

public class Analytics {

    static EasyTracker easyTracker;

    public static final String ADAPTER             = "adapter";
    public static final String ADDED               = "added";
    public static final String APP_SURVEY          = "app_Survey";
    public static final String BUTTON              = "button";
    public static final String CANCEL              = "cancel";
    public static final String CHECKED             = "checked";
    public static final String CLOSE_TIPS          = "close_tips";
    public static final String CLOUD_ITEM          = "cloud_item";
    public static final String COUNT               = "count";
    public static final String DELETE_CANCEL       = "delete_cancel";
    public static final String DELETE_LOG          = "delete_log";
    public static final String DELETED             = "deleted";
    public static final String DONT_ASK_AGAIN      = "dont_ask_again";
    public static final String EMAIL_LOG           = "email_log";
    public static final String EVENT_LOG           = "event_log";
    public static final String HELP                = "help";
    public static final String HIDE_TIPS           = "hide_tips" ;
    public static final String MAIN                = "main";
    public static final String MAIN_CLICK          = "main_click";
    public static final String MAIN_MENU           = "main_menu";
    public static final String MAKE_DONATION       = "make_donation";
    public static final String MAKE_MODEL_JSON_04  = "make_model_json_04";
    public static final String MANAGER             = "manager";
    public static final String MANAGER_CLICK       = "manager_click";
    public static final String MANAGER_MENU        = "manager_menu";
    public static final String NEXT_TIP            = "next_tip";
    public static final String OBSERVER            = "observer";
    public static final String OK                  = "ok";
    public static final String PREVIOUS_TIP        = "previous_tip";
    public static final String RATE_THIS_APP       = "rate_this_app";
    public static final String RAW_CONTACTS        = "raw_contacts";
    public static final String RAW_DATA            = "raw_data";
    public static final String REFRESH             = "refresh";
    public static final String SECURITY_CHECK      = "security_check";
    public static final String SERV                = "serv_";
    public static final String SETTINGS            = "settings";
    public static final String SEARCH              = "search";
    public static final String SHOW_TIP            = "show_tip";
    public static final String SURVEY              = "survey";
    public static final String SURVEY_CLICK        = "survey_click";
    public static final String SURVEY_JSON_04      = "survey_json_04";
    public static final String SURVEY_MENU         = "survey_menu";
    public static final String SURVEY_TOTAL        = "survey_total";
    public static final String UNCHECKED           = "unchecked";
    public static final String UPDATED             = "updated";

    /**
     * Generate Google Analytics for events
     *
     * @param easyTracker
     * @param category - String
     * @param action - String
     * @param label - String
     * @param value - Long
     */
    public static void send(Context ctx, String category,
            String action, String label, long value) {

        try {
            if( easyTracker == null ){

                easyTracker = EasyTracker.getInstance(ctx);
                LogUtil.log(LogType.ANALYTICS, "Analytics.send, restored from NULL");
            }

            // Post an "event" for next day analysis
            easyTracker.send(MapBuilder.createEvent(category, action, label, value).build());

        } catch (Exception e) {
            LogUtil.log(LogType.ANALYTICS, "exception in Analytics.send");
            LogUtil.logException( ctx, LogType.ANALYTICS, e);
        }
    }

    /**
     * Publish Google Analytics activity state, i.e., screen
     * @param activity
     */
    public static void start(Activity activity) {

        easyTracker = EasyTracker.getInstance(activity);
        easyTracker.activityStart(activity);
    }

    /**
     * Publish Google Analytics activity state, i.e., screen
     * @param activity
     */
    public static void stop(Activity activity) {

        easyTracker.activityStop(activity);
    }
}
