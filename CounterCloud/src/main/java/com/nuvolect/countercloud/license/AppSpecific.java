/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.license;

import android.net.Uri;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.main.CConst;

/**
 * Details of the license specific to this app. Keep separate from other license classes
 * to enable plug-in-play ease of maintenance.
 */
public class AppSpecific {

    public static final String APP_NAME                   = "CounterCloud";

    public static final String APP_SIMPLE_CRYPTO_CRYP_SEED= "#4!YrErd2#AvXOcL51SS";//FIXME obscure cryp key
    public static final String APP_CRYP_SEED_HEX          = "4f516651137db395a95151886f027fd8";//FIXME obscure cryp key
    public final static int SMALL_ICON = R.drawable.cc_small_icon;// 96 x 96
    public final static String TOC_HREF_URL               = CConst.CC_TOC_HREF_URL;
    public final static String PP_HREF_URL                = CConst.CC_PP_HREF_URL;
    public static final String APP_HELP_URL               = "https://nuvolect.com/countercloud_help/";
}