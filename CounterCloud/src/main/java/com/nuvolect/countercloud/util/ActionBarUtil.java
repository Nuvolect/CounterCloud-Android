/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;//

import android.app.ActionBar;
import android.app.Activity;

/**
 * Methods specific to using the ActionBar.
 */
public class ActionBarUtil {
    /**
     * Show the Up button in the action bar.
     */
    public static boolean showActionBarUpButton(Activity act){

        return homeAsUpEnabled(act, true);
    }
    public static boolean homeAsUpEnabled(Activity act, boolean state){

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(state);
            return true;
        } else{
            return false;
        }
    }
    public static boolean showTitleEnabled(Activity act, boolean b) {

        ActionBar actionBar = act.getActionBar();
        if( actionBar != null){
            actionBar.setDisplayShowTitleEnabled(b);
            return true;
        } else{
            return false;
        }
    }
}
