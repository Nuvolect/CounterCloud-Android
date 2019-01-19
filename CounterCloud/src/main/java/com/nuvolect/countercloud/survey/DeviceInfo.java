/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.survey;//

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Query details about a device.
 */
public class DeviceInfo {

    /**
     * Return a unique string for the device.  This string only changes when you wipe the device
     * and reinstall Android.
     * @param context
     * @return unique device ID string
     */
    public static String getUniqueInstallId(Context context) {

        String deviceId = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }
    /**
     * Retrieves phone make and model
     * @return
     */
    public static String getMakeModelName() {
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    //Used for the phone model
    private static String capitalize(String s) {

        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    public static long getMemorySize(Context m_ctx) {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){

                String str1 = "/proc/meminfo";
                String str2;
                String[] arrayOfString;
                long initial_memory = 0;
                try {
                    FileReader localFileReader = new FileReader(str1);
                    BufferedReader localBufferedReader = new BufferedReader(    localFileReader, 8192);
                    str2 = localBufferedReader.readLine();//meminfo
                    arrayOfString = str2.split("\\s+");
//                    for (String num : arrayOfString) {
//                        Log.i(str2, num + "\t");
//                    }
                    //total Memory
                    initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
                    localBufferedReader.close();
                    return initial_memory;
                }
                catch (IOException e)
                {
                    return -1;
                }
        }else

        {
            ActivityManager actManager = (ActivityManager) m_ctx.getSystemService(m_ctx.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            long totalMemory = memInfo.totalMem;
            return  totalMemory;
        }
    }

    public static long getInternalStorageSize(){

        long bytesAvailable = 0;
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            bytesAvailable = (long)stat.getBlockSize() * (long)stat.getBlockCount();
            long megAvailable = bytesAvailable / 1048576;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytesAvailable;
    }

    public static String getCpuInfo(){

        String arch = System.getProperty("os.arch");
        return arch;
    }

    public static boolean hasNfc(Context ctx){

        boolean hasNfc = false;

        try {
            hasNfc = ctx.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_NFC);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  hasNfc;
    }
}
