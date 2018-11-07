package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.DbProvider;
import com.nuvolect.countercloud.license.AppSpecific;
import com.nuvolect.countercloud.util.Analytics;
import com.nuvolect.countercloud.util.EmailUtil;
import com.nuvolect.countercloud.util.LogUtil;

import java.lang.ref.WeakReference;

/**
 * An activity representing a view and manager of cloud data.
 * <p>
 * See also {@link CloudManagerFragment}.
 */
public class EventLogActivity extends FragmentActivity {

    Activity m_act;
    Context m_ctx;
    EventLogFragment m_el_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        m_ctx = getApplicationContext();

        setContentView(R.layout.event_log_activity);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            m_el_fragment = startEventLogFragment();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Stop the communications framework.
        doUnbindService();
    }
    @Override
    protected void onResume() {

        super.onResume();

        // Starts the communications framework.
        doBindService();
    }

    @Override
    public void onStart() {
        super.onStart();

        Analytics.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        Analytics.stop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_log_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Toast.makeText(getApplicationContext(), "qts: "+s, Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchString) {

                if( m_el_fragment != null)
                    m_el_fragment.updateSearch(searchString);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        // Respond to the action bar's Up/Home button
        case android.R.id.home:{
            finish();
            return true;
        }

        case R.id.menu_delete_log:{
            DbProvider.deleteLogTable();
            m_act.recreate();

            Analytics.send(getApplicationContext(),
                    Analytics.EVENT_LOG,
                    Analytics.DELETE_LOG,
                    Analytics.COUNT, 1);
            break;
        }
        case R.id.menu_email_log:{

            EmailUtil.emailEventLog(m_act);
            Analytics.send(getApplicationContext(),
                    Analytics.EVENT_LOG,
                    Analytics.EMAIL_LOG,
                    Analytics.COUNT, 1);
            break;
        }
        case R.id.menu_settings:{

            Analytics.send(getApplicationContext(),
                    Analytics.EVENT_LOG,
                    Analytics.SETTINGS,
                    Analytics.COUNT, 1);

            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            break;
        }
        case R.id.menu_help:{

            Analytics.send(getApplicationContext(),
                    Analytics.EVENT_LOG,
                    Analytics.HELP,
                    Analytics.COUNT, 1);

            String url = AppSpecific.APP_HELP_URL;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            break;
        }
        default:
        }
        return super.onOptionsItemSelected(item);
    }

    private EventLogFragment startEventLogFragment() {

        EventLogFragment fragment = new EventLogFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace( R.id.event_log_container, fragment, CConst.EVENT_LOG_FRAGMENT_TAG);
        ft.commit();
        return fragment;
    }

    /**
     * Handler of incoming messages from service.
     */
    static class IncomingHandler extends Handler {

        WeakReference<EventLogActivity> mEventLogActivity;

        public IncomingHandler(EventLogActivity incomingHandler) {
            mEventLogActivity = new WeakReference<EventLogActivity>(incomingHandler);
        }

        @Override
        public void handleMessage(Message msg) {
            if( mEventLogActivity.get() == null ){

                EventLogActivity contactListActivity = new EventLogActivity();
                contactListActivity._handleMessage(msg);
            }else
                mEventLogActivity.get()._handleMessage(msg);

            super.handleMessage(msg);
        }
    }

    IncomingHandler mHandler = new IncomingHandler( this );

    /**
     * This class and method receives message commands and the message handler
     * on a separate thread. You can enter messages from any thread.
     */
    public void _handleMessage(Message msg) {

        //        Bundle bundle = msg.getData();
        WorkerService.Command cmd = WorkerService.Command.values()[msg.what];
        LogUtil.log("EventLogActivity _handleMessage: "+cmd.toString());

        switch (cmd) {

        case NOTIFY_LOG_UPDATE:
            if( m_el_fragment != null)
                m_el_fragment.notifyLoggerUpdate();
            break;
        case CLONE_CLOUD_DB:
        case INTERRUPT_PROCESSING:
        case INTERRUPT_PROCESSING_AND_STOP:
        case PROCESS_CLOUD_UPDATES:
        case REGISTER_CLOUD_OBSERVER:
        case UNREGISTER_CLOUD_OBSERVER:

        default:
            break;
        }
    }
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler( null));
    private Messenger mService = null;
    private boolean mIsBound;

    /**
     * Class for interacting with the main interface of WorkerService.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, WorkerService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(getApplicationContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    /**
     * Starts the communications framework.
     */
    void doBindService() {

        // Establish a connection with the service. We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent( getApplicationContext(), WorkerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Stops the communications framework.
     */
    void doUnbindService() {

        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, WorkerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
}
