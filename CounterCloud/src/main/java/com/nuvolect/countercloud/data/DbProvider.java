/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */
package com.nuvolect.countercloud.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.LogUtil.LogType;
import com.nuvolect.countercloud.main.WorkerCommand;
import com.nuvolect.countercloud.main.WorkerService.CcEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Stores basic information about the Guardian's Angels locally. This information
 * will persist across sessions.
 */
public class DbProvider extends ContentProvider{

    private static final String TAG = "DbProvider";
    static DatabaseHelper mOpenHelper ;

    private static final String DATABASE_NAME = "app.db";
    private static final int DATABASE_VERSION = 2;
    private static final String RAW_CONTACT_TABLE   = "raw_contact";
    private static final String EVENT_LOG_TABLE     = "event_log";
    static final String SECURE_SEARCH_TABLE = "secure_search";

    public static final String[] RAW_CONTACT_COLUMNS =
        {"_id","contact_id","deleted","display_name","version"};
    public enum RCTab
    /**/{ _id,  contact_id,  deleted,  display_name,  version};

    public static final String[] SECURE_SEARCH_COLUMNS =
        {"_id","contact_id","data1","display_name","mimetype"};
    public enum SSTab
    /**/{ _id,  contact_id,  data1,  display_name,  mimetype};

    public static final String[] EVENT_LOG_COLUMNS =
        {"_id","contact_id","time","event","display_name"};
    public enum ELTab
    /**/{ _id,  contact_id,  time,  event,  display_name};

    /**
     * Set of local DB _id that us used for a single pass comparison
     */
    private static Set<Long> m_localSet = new HashSet<Long>();
    //    private static LogListenerCallback m_logListenerCallback = null;

    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + RAW_CONTACT_TABLE + " ("
                    + RCTab._id.toString()          + " long primary key,"
                    + RCTab.contact_id.toString()   + " long,"
                    + RCTab.deleted.toString()      + " integer,"
                    + RCTab.version.toString()      + " integer,"
                    + RCTab.display_name.toString() + " text not null"
                    + ");");

            db.execSQL("CREATE TABLE " + SECURE_SEARCH_TABLE + " ("
                    + SSTab._id.toString()          + " long primary key,"
                    + SSTab.contact_id.toString()   + " long,"
                    + SSTab.data1.toString()        + " text,"
                    + SSTab.display_name.toString() + " text,"
                    + SSTab.mimetype.toString()     + " text"
                    + ");");

            db.execSQL("CREATE TABLE " + EVENT_LOG_TABLE + " ("
                    + ELTab._id.toString()          + " long primary key,"
                    + ELTab.contact_id.toString()   + " long,"
                    + ELTab.time.toString()         + " long,"
                    + ELTab.event.toString()        + " text,"
                    + ELTab.display_name.toString() + " text not null"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + RAW_CONTACT_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SECURE_SEARCH_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + EVENT_LOG_TABLE);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {

        mOpenHelper = new DatabaseHelper(getContext());

        return true;
    }

    /**
     * Delete the database.
     */
    public static void deleteDatabase(Context ctx){

        ctx.deleteDatabase(DATABASE_NAME);
    }

    public static int getRawContactCount() {

        int rows=0;

        String selection = RCTab.display_name+" NOT NULL";
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            Cursor c = db.query(RAW_CONTACT_TABLE, RAW_CONTACT_COLUMNS, selection, null, null, null, null);

            rows = c.getCount();
            c.close();
        } finally {}

        return rows;
    }

    /**
     * Delete the contents of the secure search table, but keep the table definitions.
     */
    public static void deleteSecureSearchTable() {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(SECURE_SEARCH_TABLE, null, null);
    }

    /**
     * Delete the log and leave the table definitions in place.
     */
    public static void deleteLogTable() {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.delete(EVENT_LOG_TABLE, null, null);
    }

    public interface LogListenerCallback{

        public void logUpdated();
    }


    public static void logEvent(Context ctx, long contact_id, CcEvent ccEvent, String display_name){

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransactionNonExclusive();
        try {

            ContentValues values = new ContentValues();
            values.put(ELTab.contact_id.toString(), contact_id);
            values.put(ELTab.event.toString(), ccEvent.toString());
            values.put(ELTab.time.toString(), System.currentTimeMillis());
            values.put(ELTab.display_name.toString(), display_name);

            long row = db.insert(EVENT_LOG_TABLE, null, values);

            LogUtil.log("logEvent: "+ccEvent.toString()+", Description: "+display_name);

            if( row > 0)
                db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
        // Inform the listener that there was an update
        WorkerCommand.notifyLogUpdate(ctx);
    }

    public static Cursor getLogCursor( String search){

        String selection=null;
        String[] selectionArgs=null;

        if( ! search.isEmpty()){

            selection = ELTab.display_name + " LIKE ?";
            selectionArgs = new String[]{ "%" + search + "%" };
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor c = db.query(EVENT_LOG_TABLE, null, selection, selectionArgs, null, null, ELTab.time+" DESC");
        return c;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        return 0;
    }

    public static int cloneRawContactDb(Context m_ctx) {

        int size = 0;
        String selection = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+" NOT NULL";
        ContentResolver cr = m_ctx.getContentResolver();
        Cursor g_c = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ // projection
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID,
                        ContactsContract.RawContacts.DELETED,
                        ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.RawContacts.VERSION,
                },
                selection, // selection
                null, // selection params
                null);

        //		int account_name_index = c_google.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
        int g_id_index = g_c.getColumnIndex(ContactsContract.RawContacts._ID);
        int g_contact_id_index = g_c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
        int g_deleted_index = g_c.getColumnIndex(ContactsContract.RawContacts.DELETED);
        int g_display_name_index = g_c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
        int g_version_index = g_c.getColumnIndex(ContactsContract.RawContacts.VERSION);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        //TODO For debugging, delete entire table
        db.beginTransactionNonExclusive();
        int num_deleted = db.delete(RAW_CONTACT_TABLE, "1", null);
        db.setTransactionSuccessful();
        db.endTransaction();

        db.beginTransactionNonExclusive();
        try {

            while( g_c.moveToNext()){

                //				String account_name = c_google.getString(account_name_index);
                long _id = g_c.getLong(g_id_index);
                long contact_id = g_c.getLong(g_contact_id_index);
                int deleted = g_c.getInt(g_deleted_index);
                String display_name = g_c.getString(g_display_name_index);
                int version = g_c.getInt(g_version_index);

                ContentValues values = new ContentValues();
                values.put(RCTab._id.toString(), _id);
                values.put(RCTab.contact_id.toString(), contact_id);
                values.put(RCTab.deleted.toString(), deleted);
                values.put(RCTab.display_name.toString(), display_name);
                values.put(RCTab.version.toString(), version);

                db.insert(RAW_CONTACT_TABLE, null, values);
                ++size;
            }
            db.setTransactionSuccessful();
            g_c.close();
        } finally {
            db.endTransaction();
        }

        return size;
    }

    /**
     * Accumulate data on updates to the cloud
     */
    public static class CloudUpdate{

        public ArrayList<String> m_changeList;
        public int m_updated;
        public int m_added;
        public int m_deleted;

        public CloudUpdate() {
            super();

            m_changeList = new ArrayList<String>();
            m_updated = 0;
            m_added = 0;
            m_deleted = 0;
        }
    }

    /**
     * Iterate through the cloud contact database.  Identify additions, modifications and deletions.
     * Return a list of changes.
     * @param ctx
     * @return ArrayList String of changes
     */
    public static CloudUpdate compareCloudToLocalDb(Context ctx) {

        buildLocalDbSet( ctx);

        // Setup to iterate through all Google contacts in while loop
        CloudUpdate cloudUpdate = new CloudUpdate();
        try {
            String selection1 = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY+" NOT NULL";
            ContentResolver cr = ctx.getContentResolver();
            Cursor g_c = cr.query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{
                            ContactsContract.RawContacts._ID,
                            ContactsContract.RawContacts.CONTACT_ID,
                            ContactsContract.RawContacts.DELETED,
                            ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                            ContactsContract.RawContacts.VERSION,
                    },
                    selection1, // selection
                    null, // selection params
                    null);

            int g_id_index = g_c.getColumnIndex(ContactsContract.RawContacts._ID);
            int g_contact_id_index = g_c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
            int g_del_index = g_c.getColumnIndex(ContactsContract.RawContacts.DELETED);
            int g_display_name_index = g_c.getColumnIndex( ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
            int g_version_index = g_c.getColumnIndex( ContactsContract.RawContacts.VERSION);

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            Cursor db_c = null;

            while( g_c.moveToNext()){

                long g_id = g_c.getLong(g_id_index);

                String selection = RCTab._id.toString()+"=?";
                String[] selectionArgs = new String[]{String.valueOf( g_id )};

                db_c = db.query(RAW_CONTACT_TABLE, RAW_CONTACT_COLUMNS, selection, selectionArgs, null, null, null);
                int count = db_c.getCount();

                if( count == 0){    // Record added, i.e., insert transaction

                    db_c.close();

                    // Build string for return value
                    String g_display_name = g_c.getString( g_display_name_index);
                    cloudUpdate.m_changeList.add( ("Added: "+g_display_name).trim());
                    ++cloudUpdate.m_added;
                    long g_contact_id = g_c.getLong(g_contact_id_index);

                    DbProvider.logEvent( ctx, g_contact_id, CcEvent.ADD, g_display_name);

                    int g_del = g_c.getInt(g_del_index);
                    int g_version = g_c.getInt(g_version_index);

                    // Add the new record locally
                    ContentValues values = new ContentValues();
                    values.put(RCTab._id.toString(), g_id);
                    values.put(RCTab.contact_id.toString(), g_contact_id);
                    values.put(RCTab.deleted.toString(), g_del);
                    values.put(RCTab.version.toString(), g_version);
                    values.put(RCTab.display_name.toString(), g_display_name);

                    db.beginTransactionNonExclusive();
                    long row = db.insert(RAW_CONTACT_TABLE, null, values);
                    if( row > 0)
                        db.setTransactionSuccessful();
                    db.endTransaction();
                } else {
                    if( count == 1){  // Record exists, check for an update


                        int g_version = g_c.getInt(g_version_index);

                        db_c.moveToFirst();
                        int db_version = db_c.getInt(RCTab.version.ordinal());

                        if( g_version != db_version) {

                            cloudUpdate.m_changeList.add(( "Updated: "
                                    +db_c.getString(RCTab.display_name.ordinal())).trim());
                            ++cloudUpdate.m_updated;

                            db_c.close();

                            long g_contact_id = g_c.getLong(g_contact_id_index);
                            int g_del = g_c.getInt(g_del_index);
                            String g_display_name = g_c.getString( g_display_name_index);

                            DbProvider.logEvent( ctx, g_contact_id, CcEvent.UPDATE, g_display_name);

                            // Update local record
                            ContentValues values = new ContentValues();
                            values.put(RCTab.contact_id.toString(), g_contact_id);
                            values.put(RCTab.deleted.toString(), g_del);
                            values.put(RCTab.version.toString(), g_version);
                            values.put(RCTab.display_name.toString(), g_display_name);

                            db.beginTransactionNonExclusive();
                            int rows = db.update(RAW_CONTACT_TABLE, values, selection, selectionArgs);
                            if( rows > 0)
                                db.setTransactionSuccessful();
                            db.endTransaction();
                        }
                        else
                            db_c.close();
                        /**
                         * Remove from local set.  After processing the cloud list, anything remaining
                         * in the local set represents a cloud delete transaction.
                         */
                        m_localSet.remove( g_id);

                    } else {
                        if( count > 1){
                            // flag an error
                            LogUtil.log("DB sync error, count > 1: "+count);
                        }
                    }
                }
            }
            g_c.close();

            /**
             * Now that the cloud list has been iterated, see if there are any remaining
             * locally that have not be looked at.  These are delete transactions.
             */
            if( ! m_localSet.isEmpty()){

                for( Long db_id : m_localSet){

                    // Get local data and save to change list
                    String selection = RCTab._id.toString()+"=?";
                    String[] selectionArgs = new String[]{String.valueOf( db_id )};

                    db_c = db.query(RAW_CONTACT_TABLE, RAW_CONTACT_COLUMNS, selection, selectionArgs, null, null, null);
                    int count = db_c.getCount();

                    if( count == 1){    // Found deleted record

                        db_c.moveToFirst();
                        String display_name = db_c.getString(RCTab.display_name.ordinal()).trim();
                        cloudUpdate.m_changeList.add( "Deleted: "+display_name);
                        ++cloudUpdate.m_deleted;
                        db_c.close();

                        long contact_id_is_zero = 0;

                        DbProvider.logEvent( ctx, contact_id_is_zero, CcEvent.DELETE, display_name);

                        // Remove local copy
                        db.beginTransactionNonExclusive();
                        int rows = db.delete(RAW_CONTACT_TABLE, selection, selectionArgs);
                        if( rows > 0)
                            db.setTransactionSuccessful();
                        db.endTransaction();
                    } else {
                        // Error
                        db_c.close();
                        LogUtil.log("DB sync delete local error, count not 1: "+count);
                    }
                }
                // Release the memory and setup for next pass
                m_localSet.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cloudUpdate;
    }

    /**
     * Build a set of local IDs that can later be used to track
     * if a contact has been deleted from the database.
     * @param m_ctx
     */
    public static void buildLocalDbSet(Context m_ctx) {

        m_localSet.clear();

        // Setup to iterate through all local contacts
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor db_c = db.query(RAW_CONTACT_TABLE, RAW_CONTACT_COLUMNS, null, null, null, null, null);

        try {
            while( db_c.moveToNext()){

                long db_id = db_c.getLong(RCTab._id.ordinal());

                m_localSet.add(db_id);
            }
            db_c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dump the entire event log to a file
     * @param ctx
     * @param f1
     */
    public static boolean writeEventLog(Context ctx, String dumpFilename) {

        boolean success = true;
        try {
            Cursor c = getLogCursor("");

            //        int m_contact_idIndex = c.getColumnIndex(ELTab.contact_id.toString());
            int m_timeIndex = c.getColumnIndex(ELTab.time.toString());
            int m_eventIndex = c.getColumnIndex(ELTab.event.toString());
            int m_descriptionIndex = c.getColumnIndex(ELTab.display_name.toString());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            FileOutputStream fos = ctx.openFileOutput( dumpFilename, Context.MODE_PRIVATE);

            while( c.moveToNext()){

                //            long contact_id = c.getLong(m_contact_idIndex);
                String time = sdf.format( c.getLong( m_timeIndex));
                String event = c.getString( m_eventIndex );
                String description = c.getString( m_descriptionIndex );
                String line = time + ", "+ event + ", "+ description +"\n";

                fos.write( line.getBytes());

            }
            c.close();
            if( fos != null)
                fos.close();

        } catch (FileNotFoundException e) {
            success = false;
            LogUtil.logException(ctx, LogType.EXPORT_EVENT_LOG, e);
        } catch (IOException e) {
            success = false;
            LogUtil.logException(ctx, LogType.EXPORT_EVENT_LOG, e);
        }
        return success;
    }
}
