package com.nuvolect.countercloud.survey;//

import android.graphics.drawable.Drawable;

//TODO create class description
//
public class AppItem {

    public boolean appWritePriv;
    public boolean appReadPriv;
    public String appName;
    public Drawable appIcon;
    public String appPackageName;

    public AppItem(){
        appWritePriv = false;
        appReadPriv = false;
        appName = "";
        appIcon = null;
        appPackageName = "";
    }
}
