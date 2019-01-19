package com.nuvolect.countercloud.util;

import android.content.Context;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {

    public static final boolean DEBUG = false;
    public static final boolean VERBOSE = DEBUG;
    public enum LogType {
        NIL,
        ANALYTICS,
        APP_SURVEY, 
        APP_SURVEY_FRAGMENT, 
        BETTER_CRYPTO, 
        BOOT, 
        CLOUD_MAIN_ACTIVITY,
        CLOUD_MAIN_FRAGMENT,
        CLOUD_MANAGER_FRAGMENT,
        CRYPT,
        EVENT_LOG_FRAGMENT,
        EXPORT_EVENT_LOG,
        HEARTBEAT,
        IN_APP_PAYMENT,
        JSON, 
        LICENSE_MANAGER, 
        LICENSE_UTIL,
        REST, 
        SHOW_TIPS, 
        UTIL, 
        WEB_SERVER, 
        WHATS_NEW, 
        WORKER, 
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
