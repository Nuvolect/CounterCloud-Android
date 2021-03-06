/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {

    public static boolean DEBUG = false;
    public static boolean VERBOSE = false;
    
    public enum LogType {
        NIL,
        APP_SURVEY,
        APP_SURVEY_FRAGMENT, 
        BETTER_CRYPTO, 
        BOOT, 
        CLOUD_MAIN_ACTIVITY,
        CLOUD_MAIN_FRAGMENT,
        CLOUD_MANAGER_FRAGMENT,
        CLOUD_MANAGER_CA,
        CRYPT,
        EVENT_LOG_FRAGMENT,
        EXPORT_EVENT_LOG,
        IN_APP_PAYMENT,
        JSON, 
        LICENSE_MANAGER, 
        LICENSE_UTIL,
        REST, 
        SHOW_TIPS, 
        UTIL, 
        WEB_SERVER, 
        WHATS_NEW, 
        WORKER,;
    }

    public static void setVerbose(boolean verbose){

        VERBOSE = verbose;
        DEBUG = verbose;
    }
    /**
     * Post a message to the developer console if VERBOSE is enabled.
     * @param log
     */
    public static void log(String log){

        if(VERBOSE)
            Log.v(Util.TAG, log);
    }

    public static void log(LogType tag, String log){

        if(VERBOSE)
            Log.v(Util.TAG, tag.toString()+", "+log);
    }

    /**
     * Put exception in Android Logcat.
     *
     * @param ctx
     * @param logType
     * @param e
     */
    public static void logException(Context ctx, LogType logType, Exception e) {

        logException( logType, e);
    }
    /**
     * Put exception in Android Logcat
     *
     * @param logType
     * @param e
     */
    public static void logException(LogType logType, Exception e) {

        e.printStackTrace(System.err);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log( logType,  "ERROR Exception: "+sw.toString());
    }

    public static void e(String tag, String string) {

        if(VERBOSE)
            Log.e( Util.TAG, tag+", "+ string);
    }
}
