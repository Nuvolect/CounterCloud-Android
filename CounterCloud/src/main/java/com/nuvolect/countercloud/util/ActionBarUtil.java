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
