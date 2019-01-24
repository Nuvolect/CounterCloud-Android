/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

public class Util {

    public static String TAG = "CounterCloud";

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    @SuppressLint("NewApi")
    public static int generateViewId(){

        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)

            return Util.preApi17_generateViewId();
        else
            return View.generateViewId();
    }

    /**
     * Generate a value suitable for use in {@link #generateViewId()} (int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int preApi17_generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static int unitsDpEquiv(Activity act, int unitsDp){

        int equivDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                unitsDp, act.getResources().getDisplayMetrics());

        return equivDp;
    }

    /**
     * Dump to log and return a string with the description of a cursor.
     * @param cursor
     * @return
     */
    public static String dumpCursorDescription(Cursor cursor, String tag){

        String description = tag + " ";
        String newLine = "";

        if( cursor == null)
            description += "cursor is null";
        else
        if( cursor.isClosed())
            description += "cursor is closed";
        else{
            description += "count(): "+cursor.getCount()+"\n";
            for( int columnIndex=0; columnIndex< cursor.getColumnCount(); columnIndex++){

                String columnDesc = columnIndex+": "+cursor.getColumnName(columnIndex);
                description = description + newLine + columnDesc;
                newLine = "\n";
            }
        }
        LogUtil.log(description);
        return description;
    }

    /**
     * Lock the screen in the current orientation.  It will remain locked in this orientation
     * until unlocked.
     * @param act
     */
    public static void lockScreenOrientation(Activity act) {
        int currentOrientation = act.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * Unlock the screen orientation that was locked with lockScreenOrientation.
     * @param act
     */
    public static void unlockScreenOrientation(Activity act) {
        act.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}