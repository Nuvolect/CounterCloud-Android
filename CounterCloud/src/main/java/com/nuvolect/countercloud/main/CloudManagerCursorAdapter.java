/*
 * Copyright (c) 2019 Nuvolect LLC.
 * This software is offered for free under conditions of the GPLv3 open source software license.
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Identity;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.DbProvider.SSTab;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.Util;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CloudManagerCursorAdapter extends CursorAdapter {

    private final LayoutInflater m_inflater;
    private final int m_layout;
    CloudManagerFragment.CloudManagerMode mCloudManagerMode;
    private int m_id_index;
    private int m_starred_index;
    Set<Long> m_delete_set = new HashSet<Long>();
    private int m_display_name_raw_idx;
    private int m_account_name_raw_idx;
    private int m_account_type_idx;
    private int m_display_name_raw_data_idx;
    private int m_mimetype_idx;
    private int m_data1_idx;
    private int m_times_contacted_idx;
    private int m_last_time_contacted_idx;
    private Cursor m_cursor;
    private Context m_ctx;
    private String m_account;
    private static CloudManagerCaCallbacks m_listener;
    private int m_deleted;

    public interface CloudManagerCaCallbacks {

        public void itemClick( long contact_id);
    }

    public CloudManagerCursorAdapter(Context ctx, Cursor c, int flags, int layout,
                                     CloudManagerFragment.CloudManagerMode managerMode, String account, CloudManagerCaCallbacks listener) {
        super(ctx, c, flags);

        m_ctx = ctx;
        m_layout = layout;
        m_inflater=LayoutInflater.from(ctx);
        mCloudManagerMode = managerMode;
        m_account = account;
        m_id_index = c.getColumnIndex( "_id");
        m_starred_index = c.getColumnIndex( "starred");
        m_delete_set.clear();
        m_cursor = c;
        m_listener = listener;

        switch( mCloudManagerMode){

            case RAW_CONTACTS:{

                m_display_name_raw_idx = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
                m_account_name_raw_idx = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
                m_account_type_idx = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE);
                m_times_contacted_idx = c.getColumnIndex(ContactsContract.RawContacts.TIMES_CONTACTED);
                m_last_time_contacted_idx = c.getColumnIndex(ContactsContract.RawContacts.LAST_TIME_CONTACTED);
                break;
            }
            case RAW_DATA:{

                m_display_name_raw_data_idx = c.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
                m_data1_idx = c.getColumnIndex(ContactsContract.Data.DATA1);
                m_mimetype_idx = c.getColumnIndex(ContactsContract.Data.MIMETYPE);
                break;
            }
            case SECURITY_CHECK:{

                m_display_name_raw_data_idx = c.getColumnIndex(SSTab.display_name.toString());
                m_data1_idx = c.getColumnIndex(SSTab.data1.toString());
                m_mimetype_idx = c.getColumnIndex(SSTab.mimetype.toString());
                break;
            }
        }
    }

    @Override
    public void bindView(View view, Context ctx, Cursor c) {

        long _id = c.getLong( m_id_index );
        long contact_id = c.getLong( 1 );// CONTACT_ID or RAW_CONTACT_ID
        final CheckBox m_cb = (CheckBox) view.findViewById(R.id.delete_cb);
        m_cb.setTag(_id);

        if( m_delete_set.contains( _id))
            m_cb.setChecked( true );
        else
            m_cb.setChecked( false );

        m_cb.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                CheckBox cb = (CheckBox) v;
                long _id = (Long) cb.getTag();

                if( m_delete_set.contains( _id)){

                    // Un-select it
                    m_delete_set.remove( _id);
                    m_cb.setChecked( false );

                }else{

                    // Select it
                    m_delete_set.add( _id );
                    m_cb.setChecked( true );
                }
            }});
        String s ="";

        switch (mCloudManagerMode){

            case RAW_CONTACTS:{

                long time_last_contacted = c.getLong(m_last_time_contacted_idx);
                int times_contacted = c.getInt(m_times_contacted_idx);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

                s = c.getString( m_display_name_raw_idx );
                if( times_contacted > 0)
                    s = s+", times contacted: "+ times_contacted;
                if( time_last_contacted > 0)
                    s = s+", time last contacted: "+ sdf.format(time_last_contacted);

                if( m_account.contentEquals(CConst.ALL_ACCOUNTS))
                    s = s+", account: "+ c.getString( m_account_name_raw_idx);
                s = s+", source: "+ c.getString( m_account_type_idx );
                break;
            }
            case SECURITY_CHECK:
            case RAW_DATA:{

                String mimetype = fetchMimeType( c,  m_mimetype_idx).toString()+": ";
                String data1 = c.getString( m_data1_idx );

                if( data1 != null && ! data1.isEmpty())
                    s = mimetype + data1;
                else
                    s = mimetype + c.getString( m_display_name_raw_data_idx );
                break;
            }
        }
        TextView nameTv = (TextView) view.findViewById(R.id.cloud_item);
        nameTv.setText( s);
        nameTv.setTag(contact_id);

        nameTv.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {

                TextView nameTv2 = (TextView) v.findViewById(R.id.cloud_item);
                long contact_id = (Long) nameTv2.getTag();

                m_listener.itemClick(contact_id);
            }
        });
    }

    public enum MIME_TYPE {
        CONTACT_JOT,
        CONTACT_MISC,
        EMAIL,
        EMPTY,
        EXTERNAL_ID,
        EVENT,
        GROUP,
        IDENTITY,
        IM,
        LINKEDIN,
        NICKNAME,
        NOTE,
        NULL,
        ORGANIZATION,
        PHONE,
        PHOTO,
        POSTAL,
        RELATION,
        SIP_ADDRESS,
        STRUCTURED_NAME,
        UNKNOWN,
        USER_DEFINED,
        WEBSITE,
    };

    public MIME_TYPE getMimeType(String mimeType){

        if( mimeType == null)
            return MIME_TYPE.NULL;
        else
        if( mimeType.isEmpty())
            return MIME_TYPE.EMPTY;
        else
        if(mimeType.contains(StructuredName.CONTENT_ITEM_TYPE))
            return MIME_TYPE.STRUCTURED_NAME;
        else
        if(mimeType.contains(Phone.CONTENT_ITEM_TYPE))
            return MIME_TYPE.PHONE;
        else
        if(mimeType.contains(Email.CONTENT_ITEM_TYPE))
            return MIME_TYPE.EMAIL;
        else
        if(mimeType.contains(Photo.CONTENT_ITEM_TYPE))
            return MIME_TYPE.PHOTO;
        else
        if(mimeType.contains(Organization.CONTENT_ITEM_TYPE))
            return MIME_TYPE.ORGANIZATION;
        else
        if(mimeType.contains(Im.CONTENT_ITEM_TYPE))
            return MIME_TYPE.IM;
        else
        if(mimeType.contains(Nickname.CONTENT_ITEM_TYPE))
            return MIME_TYPE.NICKNAME;
        else
        if(mimeType.contains(Note.CONTENT_ITEM_TYPE))
            return MIME_TYPE.NOTE;
        else
        if(mimeType.contains(StructuredPostal.CONTENT_ITEM_TYPE))
            return MIME_TYPE.POSTAL;
        else
        if(mimeType.contains(GroupMembership.CONTENT_ITEM_TYPE))
            return MIME_TYPE.GROUP;
        else
        if(mimeType.contains(Website.CONTENT_ITEM_TYPE))
            return MIME_TYPE.WEBSITE;
        else
        if(mimeType.contains(Event.CONTENT_ITEM_TYPE))
            return MIME_TYPE.EVENT;
        else
        if(mimeType.contains(Relation.CONTENT_ITEM_TYPE))
            return MIME_TYPE.RELATION;
        else
        if(mimeType.contains(SipAddress.CONTENT_ITEM_TYPE))
            return MIME_TYPE.SIP_ADDRESS;
        else
        if(mimeType.contains(Identity.CONTENT_ITEM_TYPE))
            return MIME_TYPE.IDENTITY;
        else
        if(mimeType.contains("vnd.com.google.cursor.item/contact_misc"))
            return MIME_TYPE.CONTACT_MISC;
        else
        if(mimeType.contains("vnd.com.google.cursor.item/contact_external_id"))
            return MIME_TYPE.EXTERNAL_ID;
        else
        if(mimeType.contains("vnd.com.google.cursor.item/contact_jot"))
            return MIME_TYPE.CONTACT_JOT;
        else
        if(mimeType.contains("vnd.com.linkedin.android.profile"))
            return MIME_TYPE.LINKEDIN;
        else
        if(mimeType.contains("vnd.com.google.cursor.item/contact_user_defined_field"))
            return MIME_TYPE.USER_DEFINED;

        LogUtil.log("Cloud Manager unknown mimetype: "+mimeType);

        return MIME_TYPE.UNKNOWN;
    }

    public MIME_TYPE fetchMimeType(Cursor c, int column_index){

        String mimetype="";
        try {
            mimetype = c.getString( column_index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getMimeType(mimetype);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return m_inflater.inflate(m_layout, null);
    }

    public void selectAllButton() {

        Cursor c = m_cursor;
        c.moveToPosition(-1);
        m_delete_set.clear();

        while( c.moveToNext()){

            Long _id = c.getLong( m_id_index);
            m_delete_set.add(_id);
        }
        notifyDataSetChanged();
        Toast.makeText(m_ctx, "Selected: "+m_delete_set.size(), Toast.LENGTH_SHORT).show();
    }

    public void selectStarred() {

        Cursor c = m_cursor;
        c.moveToPosition(-1);
        m_delete_set.clear();

        while( c.moveToNext()){

            String starred = c.getString(m_starred_index);

            if( starred.contentEquals("1")){

                Long _id = c.getLong( m_id_index);
                m_delete_set.add( _id );
            }
        }
        notifyDataSetChanged();
        Toast.makeText(m_ctx, "Selected: "+m_delete_set.size(), Toast.LENGTH_SHORT).show();
    }

    public void selectUnstarred(){

        Cursor c = m_cursor;
        c.moveToPosition(-1);
        m_delete_set.clear();

        while( c.moveToNext()){

            String starred = c.getString(m_starred_index);

            if( starred.contentEquals("0")){

                Long _id = c.getLong( m_id_index);
                m_delete_set.add( _id );
            }
        }
        notifyDataSetChanged();
        Toast.makeText(m_ctx, "Selected: "+m_delete_set.size(), Toast.LENGTH_SHORT).show();
    }

    public void selectNoneButton() {

        m_delete_set.clear();
        notifyDataSetChanged();
        Toast.makeText(m_ctx, "Selected: "+m_delete_set.size(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Return the count in the current delete set.
     * @return
     */
    public int getDeleteCount(){

        return m_delete_set.size();
    }

    /**
     * Make a short summary of what will be deleted
     * @return
     */
    public String getDeleteSummary(){

        String item = " items?";
        if( m_delete_set.size() == 1)
            item = " item?";

        String summary =
                "Are you sure you want to delete "+m_delete_set.size()+item
                        +" This information will be permanently removed from the cloud.";

        return summary;
    }
    ProgressDialog progressDialog;
    boolean userCanceled = false;

    public interface CloudDeleteCallback{

        public void deleteSuccess(int itemsDeleted);
//        public void deleteFail(String error);
    }

    /**
     * Execute actions of the delete button
     */
    public int deleteButton(final Activity act, final CloudDeleteCallback listener) {

        /**
         * Lock screen to avoid lifecycle issues, unlock at end.
         */
        Util.lockScreenOrientation(act);

        Toast.makeText(m_ctx, "Deleting: "+m_delete_set.size()+", please wait...", Toast.LENGTH_SHORT).show();

        userCanceled = false;
        progressDialog = new ProgressDialog( m_ctx);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Deleting " + m_delete_set.size() + " cloud items");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(m_delete_set.size());
        
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                "Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                        userCanceled = true;
                        if( progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();

                    }
                });
        progressDialog.setProgress(0);
        progressDialog.show();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                m_deleted = 0;
                LogUtil.log("doInBackground start: "+m_deleted);
                ContentResolver cr = m_ctx.getContentResolver();


                for( long _id : m_delete_set){

                    if( userCanceled)
                        break;

                    progressDialog.setProgress( m_deleted);
                    LogUtil.log("doInBackground: "+m_deleted);

                    String where = "_ID = ? ";
                    String[] args = new String[]{String.valueOf( _id )};
                    int response=-1;

                    switch (mCloudManagerMode){

                        case RAW_CONTACTS:{

                            try {
                                // Data table content process uri.
                                Uri dataContentUri = ContactsContract.Data.CONTENT_URI;

                                // Create data table where clause.
                                StringBuffer dataWhereClauseBuf = new StringBuffer();
                                dataWhereClauseBuf.append(ContactsContract.Data.RAW_CONTACT_ID);
                                dataWhereClauseBuf.append(" = ");
                                dataWhereClauseBuf.append( _id );

                                // Delete all contact related data in data table.
                                response = cr.delete( dataContentUri, dataWhereClauseBuf.toString(), null);

                            } catch (Exception e) {
                                LogUtil.logException(LogUtil.LogType.CLOUD_MANAGER_CA, e);
                                response = -2;
                            }
                            break;
                        }
                        case SECURITY_CHECK:
                        case RAW_DATA:{

                            try {
                                // Data table content process uri.
                                Uri dataContentUri = ContactsContract.Data.CONTENT_URI;

                                // Create data table where clause.
                                StringBuffer dataWhereClauseBuf = new StringBuffer();
                                dataWhereClauseBuf.append(ContactsContract.Data.CONTENT_URI);
                                dataWhereClauseBuf.append(" = ");
                                dataWhereClauseBuf.append( _id );

                                // Delete all contact related data in data table.
                                response = cr.delete( dataContentUri, dataWhereClauseBuf.toString(), null);
                                
                            } catch (Exception e) {
                                LogUtil.logException(LogUtil.LogType.CLOUD_MANAGER_CA, e);
                                response = -2;
                            }
                            break;
                        }
                    }
                    switch ( response ){

                        case -2:
                            LogUtil.log("ID: "+_id+" exception thrown");
                            break;
                        case -1:
                            LogUtil.log("ID: "+_id+" no execution");
                            break;
                        case 0:
                            LogUtil.log("ID: "+_id+" NOT deleted");
                            Toast.makeText(m_ctx, "Delete failed", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            LogUtil.log("ID: "+_id+" confirmed one row deleted");
                            ++m_deleted;
                            break;
                        default:
                            LogUtil.log("ID: "+_id+" rows deleted: "+response);
                            m_deleted += response;
                            break;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                m_delete_set.clear();
                notifyDataSetChanged();
                if( progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Util.unlockScreenOrientation(act);
                listener.deleteSuccess(m_deleted);
            }
        }.execute();

        return m_deleted;
    }
}
