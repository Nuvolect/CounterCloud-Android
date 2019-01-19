package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.license.AppSpecific;

public class CloudManagerActivity extends FragmentActivity
        implements CloudManagerFragment.Callbacks {

    Activity m_act;
    Context m_ctx;
    CloudManagerFragment m_cloudMgrFrag;
    Bundle m_arguments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        m_ctx = getApplicationContext();

        // Action bar progress setup.  Needs to be called before setting the content view
//        m_act.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.cloud_manager_activity);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            // Create the fragment and add it to the activity using a fragment transaction.
            m_arguments = new Bundle();

            String account_key = getIntent().getStringExtra(CConst.ACCOUNT_KEY);
            m_arguments.putString(CConst.ACCOUNT_KEY, account_key );

            boolean security_check = getIntent().getBooleanExtra(CConst.SECURITY_CHECK, false);
            m_arguments.putBoolean(CConst.SECURITY_CHECK, security_check);

            m_cloudMgrFrag = startCloudManagerFragment( m_arguments);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cloud_manager_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:{
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.menu_event_log:{

                Intent intent = new Intent(this, EventLogActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.menu_settings:{

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                break;
            }
            case R.id.menu_help:{

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

    private CloudManagerFragment startCloudManagerFragment(Bundle arguments){

        CloudManagerFragment fragment = new CloudManagerFragment();
        fragment.setArguments(arguments);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.cloud_manager_container, fragment);
        ft.commit();
        return fragment;
    }

    @Override
    public void onRefreshFragment() {

        // Create the fragment and add it to the activity using a fragment transaction.
        m_arguments = new Bundle();
        m_arguments.putString(CConst.ACCOUNT_KEY,
                getIntent().getStringExtra(CConst.ACCOUNT_KEY));
        m_arguments.putBoolean(CConst.SECURITY_CHECK,
                getIntent().getBooleanExtra(CConst.SECURITY_CHECK, false));

        m_cloudMgrFrag = startCloudManagerFragment( m_arguments);
    }
}
