/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;//

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nuvolect.countercloud.data.DbProvider;
import com.nuvolect.countercloud.data.Persist;
import com.nuvolect.countercloud.license.LicensePersist;
import com.nuvolect.countercloud.util.CustomDialog;
import com.nuvolect.countercloud.util.DialogUtil;
import com.nuvolect.countercloud.util.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage a list of developer commands. This dialog is only displayed for users who enable it.
 */
public class DeveloperDialog {

    private static int notifNum = 0;

    private static Activity m_act;
    /**
     * True when the developers menu is disabled, ie demos and videos
     */
    private static boolean m_developerIsEnabled = true;

    public static boolean isEnabled() {
        return m_developerIsEnabled;
    }

    /**
     * Developer menu: in menu order.  Replaces '_' with ' ' on menu.
     */
    public static enum DevMenu {
        Decrement_app_version,
        Clear_data_close_app,
        Create_Notification,
        Test_RateThisApp,
        Test_MakeDonation,
    };

    public static void start(final Activity act) {

        m_act = act;

        final List<String> stringMenu = new ArrayList<String>();

        for( DevMenu menuItem : DevMenu.values()){

            String item = menuItem.toString().replace('_', ' ');
            stringMenu.add( item);
        }
        final CharSequence[] items = stringMenu.toArray(new CharSequence[stringMenu.size()]);

        final DialogUtil.DialogCallback deleteAllDialogCallback = new DialogUtil.DialogCallback() {
            @Override
            public void confirmed() { //TODO delete database

                Toast.makeText(m_act, "got there", Toast.LENGTH_SHORT).show();

                Persist.clearAll(m_act);
                LicensePersist.clearAll(m_act);
                DbProvider.deleteDatabase(m_act);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(m_act);
                pref.edit().clear().commit();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    act.finishAffinity();
                } else{
                    act.finish();
                    System.exit( 0 );
                }
            }

            @Override
            public void canceled() {
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_act);
        builder.setTitle("Developer Menu")
                .setItems( items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        DevMenu menuItem = DevMenu.values()[which];

                        switch( menuItem){

                            case Decrement_app_version:{

                                int appVersion = Persist.getAppVersion(m_act);
                                if( --appVersion < 1)
                                    appVersion = 1;
                                Persist.setAppVersion(m_act, appVersion);
                                Toast.makeText(m_act, "App version: " + appVersion, Toast.LENGTH_LONG).show();
                                break;
                            }
                            case Clear_data_close_app:{

                                DialogUtil.confirmDialog(
                                        m_act,
                                        "Your attention please",
                                        "Delete ALL app data?",
                                        "Cancel",
                                        "Yes, delete ALL", deleteAllDialogCallback);
                                break;
                            }
                            case Create_Notification:{

                                ++notifNum;
                                NotificationUtil.pushNotification( m_act,
                                        "Title part: " + notifNum, "Small text part: " + notifNum);
                                break;
                            }
                            case  Test_RateThisApp:{

                                CustomDialog.rateThisApp(m_act, true);
                                break;
                            }
                            case  Test_MakeDonation:{

                                CustomDialog.makeDonation(m_act, true);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
