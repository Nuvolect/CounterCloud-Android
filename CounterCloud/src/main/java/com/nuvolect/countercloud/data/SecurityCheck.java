/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.nuvolect.countercloud.main.CConst;
import com.nuvolect.countercloud.data.DbProvider.SSTab;
import com.nuvolect.countercloud.util.LogUtil;

public class SecurityCheck {

    public static String[] search_list = {//TODO expand securityCheck search items
        "account",
        "best friend",
        "city born",
        "code",
        "door",
        "favorite food",
        "favorite place",
        "first car",
        "gate",
        "highschool graduated",
        "identification",
        "maiden name",
        "passcode",
        "passphrase",
        "password",
        "pin",
        "security question",
        "social security number",
        "ssn",
        "year graduated",
    };

    public static boolean search_enabled = true;


    public static int populateResultsTable(Context ctx, String account) {

        SQLiteDatabase db = DbProvider.mOpenHelper.getWritableDatabase();
        int results = 0;

        /*
         * Start with an empty table each time
         */
        DbProvider.deleteSecureSearchTable();

        ContentResolver cr = ctx.getContentResolver();

        for( String search : search_list){

            String where = "";
            String[] args = null;

            if( ! account.contains(CConst.ALL_ACCOUNTS)){
                where = "account_name LIKE ?";
                args = new String[]{ "%" + account + "%"};
            }
            String search_where = "data1 LIKE ?";
            String[] search_args = new String[]{ "%" + search + "%"};

            where = DatabaseUtils.concatenateWhere( where, search_where);
            args = DatabaseUtils.appendSelectionArgs( args, search_args);

            String noteWhere = ContactsContract.Data.MIMETYPE + " = ?";
            String[] noteArgs = new String[]{ ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

            where = DatabaseUtils.concatenateWhere( where, noteWhere);
            args = DatabaseUtils.appendSelectionArgs( args, noteArgs);

            /*
             * Pull a cursor for each search term
             */
            Cursor c = cr.query(
                    ContactsContract.Data.CONTENT_URI,
                    new String[]{ // projection
                            ContactsContract.Data._ID,         // 0 - not used
                            ContactsContract.Data.CONTACT_ID,  // 1
                            ContactsContract.Data.DATA1,       // 2
                            ContactsContract.Data.DISPLAY_NAME,// 3
                            ContactsContract.Data.MIMETYPE,    // 4
                    },
                    where, // selection
                    args,  // selection params
                    ContactsContract.Data.DISPLAY_NAME + " ASC"
                    );

            /*
             * Iterate through the search results and build a separate table
             */
            while( c.moveToNext()){

                long _id = c.getLong(0);

                String targetWhere = "_id = ? ";
                String[] targetArgs = new String[]{String.valueOf( _id )};

                Cursor targetC = db.query(DbProvider.SECURE_SEARCH_TABLE,
                        null, targetWhere, targetArgs, null, null, null );

                int count = targetC.getCount();
                targetC.close();

                if( count == 0){

                    try {
                        db.beginTransactionNonExclusive();

                        ContentValues values = new ContentValues();
                        values.put(SSTab._id.toString(),         _id);
                        values.put(SSTab.contact_id.toString(),  c.getLong   (1));
                        values.put(SSTab.data1.toString(),       c.getString (2));
                        values.put(SSTab.display_name.toString(),c.getString (3));
                        values.put(SSTab.mimetype.toString(),    c.getString (4));

                        long row = db.insert(DbProvider.SECURE_SEARCH_TABLE, null, values);

                        LogUtil.log("Secure search added: "+row+", :"+search+", values: "+values.toString());

                        if( row > 0){
                            ++results;
                            db.setTransactionSuccessful();
                        }

                    } finally {
                        db.endTransaction();
                    }
                }
            }
            c.close();
        }
        return results;
    }


    public static Cursor getResultsCursor(Context m_ctx) {

        SQLiteDatabase db = DbProvider.mOpenHelper.getWritableDatabase();
        Cursor c = db.query(DbProvider.SECURE_SEARCH_TABLE, null, null, null, null, null,
                SSTab.display_name+" ASC");

        return c;
    }
}
