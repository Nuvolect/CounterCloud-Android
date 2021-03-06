/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nuvolect.countercloud.main.EventLogCursorAdapter.EventLogCaCallbacks;
import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.CloudContacts;
import com.nuvolect.countercloud.data.DbProvider;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.LogUtil.LogType;
import com.nuvolect.countercloud.util.Util;

public class EventLogFragment extends ListFragment {

    //    private static final boolean DEBUG = false;
    Activity m_act;
    EventLogCursorAdapter m_eventLogCursorAdapter;
    Cursor   m_cursor = null;
    String m_searchString = "";
    private EventLogCaCallbacks m_adapterCallbacks;
    private View m_root_view;
    private Context m_ctx;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventLogFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = getActivity();
        m_ctx = m_act.getApplicationContext();
        LogUtil.log("EventLogFragment onCreate");

        m_adapterCallbacks = new EventLogCaCallbacks(){

            @Override
            public void itemClick(long contact_id) {

                Uri contactUri = CloudContacts.getContactUri(m_ctx, contact_id);

                if( contactUri == null)
                    Toast.makeText(m_ctx, "Contact deleted or URI invalid", Toast.LENGTH_SHORT).show();
                else{
                    LogUtil.log( LogType.CLOUD_MANAGER_FRAGMENT,
                            "itemClick: "+ contact_id+", "+contactUri.toString());

                    QuickContact.showQuickContact(m_act, m_root_view, contactUri,
                            QuickContact.MODE_MEDIUM, null);
                }
            }
        };
    }
    @Override
    public void onResume() {

        super.onResume();
        LogUtil.log("EventLogFragment onResume");
    }
    @Override
    public void onPause() {

        super.onPause();
        LogUtil.log("EventLogFragment onPause");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LogUtil.log("EventLogFragment onDestroy");

        if( m_cursor != null && !m_cursor.isClosed())
            m_cursor.close();
        m_cursor = null;

        m_eventLogCursorAdapter = null;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        LogUtil.log("EventLogFragment onCreateView");

        // Inflate the layout for this fragment
        View root_view = inflater.inflate(R.layout.event_log_fragment, container, false);

        updateAdapter(m_searchString);

        m_root_view = root_view;
        return root_view;
    }

    TextWatcher searchStringWatcher = new TextWatcher(){

        @Override
        public void afterTextChanged(Editable s) {

            m_searchString = s.toString().trim();
            updateAdapter(m_searchString);
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
    };

    public void updateAdapter(String search){

        Cursor cursor = DbProvider.getLogCursor(search);
        Util.dumpCursorDescription(cursor, "updateAdapter");

        if( m_eventLogCursorAdapter == null){

            // First time, create an adapter, cursor may still be null
            m_eventLogCursorAdapter = new EventLogCursorAdapter( getActivity(),
                    cursor,
                    0,  // flags, not using
                    R.layout.event_log_item,
                    m_adapterCallbacks
            );
            setListAdapter( m_eventLogCursorAdapter);
            LogUtil.log( LogType.EVENT_LOG_FRAGMENT, "adapter created");
        }else{
            m_eventLogCursorAdapter.changeCursor(cursor);
            m_eventLogCursorAdapter.notifyDataSetChanged();
            LogUtil.log( LogType.EVENT_LOG_FRAGMENT, "adapter updated");
        }
        // Save the cursor so it can be closed in onDestroy
        m_cursor = cursor;
    }

    public void updateSearch(String searchString) {

        m_searchString = searchString;
        updateAdapter( m_searchString );
    }


    public void notifyLoggerUpdate() {

        this.updateAdapter(m_searchString);
    }
}
