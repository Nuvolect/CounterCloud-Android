/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.data;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.nuvolect.countercloud.util.LogUtil;

public class Inventory {

    private static ContentResolver contentResolver;

    static class DbPair{
        Uri uri;
        String description;

        DbPair(Uri u, String d){

            this.uri = u;
            this.description = d;
        }
    }

    public static ArrayList<DbPair> dbPair = new ArrayList<DbPair>();

    @SuppressLint("NewApi")
    public static void initializeData(){

        dbPair.add( new DbPair( ContactsContract.Data.CONTENT_URI, "Data"));
        dbPair.add( new DbPair( ContactsContract.RawContacts.CONTENT_URI, "RawContacts"));
        dbPair.add( new DbPair( ContactsContract.AggregationExceptions.CONTENT_URI, "AggregationExceptions"));
        dbPair.add( new DbPair( ContactsContract.Contacts.CONTENT_URI,"Contacts"));
//        dbPair.add( new DbPair( ContactsContract.DataUsageFeedback.DELETE_USAGE_URI,"DataUsageFeedback.delete"));
//        dbPair.add( new DbPair( ContactsContract.DataUsageFeedback.FEEDBACK_URI,"DataUsageFeedback.feedback"));

// ERROR on android 4.0.3, samsung-sph-d700, noClassDefFoundError android.provider.ContactsContract$DeletedContacts
//        dbPair.add( new DbPair( ContactsContract.DeletedContacts.CONTENT_URI,"DeletedContacts"));

        dbPair.add( new DbPair( ContactsContract.Directory.CONTENT_URI,"Directory"));
//        dbPair.add( new DbPair( ContactsContract.DisplayPhoto.CONTENT_URI,"DisplayPhoto"));
//        dbPair.add( new DbPair( ContactsContract.PhoneLookup.CONTENT_FILTER_URI,"PhoneLookup"));
        dbPair.add( new DbPair( ContactsContract.Presence.CONTENT_URI,"Presence"));
//        dbPair.add( new DbPair( ContactsContract.Profile.CONTENT_URI,"Profile"));
//        dbPair.add( new DbPair( ContactsContract.ProfileSyncState.CONTENT_URI,"ProfileSyncState"));
        dbPair.add( new DbPair( ContactsContract.RawContactsEntity.CONTENT_URI,"RawContactsEntity"));
        dbPair.add( new DbPair( ContactsContract.Settings.CONTENT_URI,"Settings"));
        dbPair.add( new DbPair( ContactsContract.StatusUpdates.CONTENT_URI,"StatusUpdates"));
        dbPair.add( new DbPair( ContactsContract.SyncState.CONTENT_URI,"SyncState"));

// ERROR on android 4.0.3, samsung-sph-d700, noClassDefFoundError android.provider.ContactsContract$DeletedContacts
//        dbPair.add( new DbPair( ContactsContract.CommonDataKinds.Contactables.CONTENT_URI, "CDK.Contactables"));

        dbPair.add( new DbPair( ContactsContract.CommonDataKinds.Email.CONTENT_URI, "CDK.Email"));
    }




    public static void getInventory(Context ctx){

        contentResolver = ctx.getContentResolver();
        initializeData();
        LogUtil.log("");
        
        for (DbPair p : dbPair){
            
            try {
                countItemsUri( p.uri, p.description);
            } catch (Exception e) {
                LogUtil.log("Request rejected: "+p.description);
            }
        }
    }
    public static int countItemsUri( Uri uri, String description){

        Cursor cursor = contentResolver.query(uri, null,null, null, null);  
        int count = cursor.getCount();
        cursor.close();

        LogUtil.log("Inventory: "+count+", "+description);
        return count;
    }
}
