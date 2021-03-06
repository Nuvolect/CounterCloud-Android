/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.Persist;
import com.nuvolect.countercloud.license.LicenseManager;
import com.nuvolect.countercloud.license.LicensePersist;
import com.nuvolect.countercloud.license.LicenseUtil;
import com.nuvolect.countercloud.survey.AppSurveyActivity;
import com.nuvolect.countercloud.util.ActionBarUtil;
import com.nuvolect.countercloud.util.DialogUtil;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.LogUtil.LogType;
import com.nuvolect.countercloud.util.PermissionUtil;

/**
 * An activity representing a summary view of cloud data.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link CloudManagerFragment}.
 */
public class CloudMainActivity extends FragmentActivity
        implements CloudMainFragment.Callbacks {

    private static final int REQUEST_ID_READ_CONTACTS = 321;
    static Activity m_act;
    static Context m_ctx;
    CloudMainFragment m_main_frag;
    public static boolean m_appUpgraded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        m_ctx = getApplicationContext();

        /**
         * Load build-dependent data into static variables that can be accessed without context.
         */
        LogUtil.setVerbose( Boolean.valueOf( m_ctx.getString(R.string.verbose_logging)));
        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "onCreate()");

        setContentView(R.layout.cloud_main_activity);

        LicenseManager.getInstance(m_act).checkLicense(m_act, mLicenseManagerListener);
    }

    LicenseManager.LicenseCallbacks mLicenseManagerListener = new LicenseManager.LicenseCallbacks(){

        @Override
        public void licenseResult(LicenseManager.LicenseResult license) {

            LogUtil.log("License result: "+license.toString());
            LicensePersist.setLicenseResult(m_ctx, license);

            switch ( license) {
                case NIL:
                    break;
                case REJECTED_TERMS:
                    m_act.finish();
                    break;
                case PREMIUM_USER:
                    if(PermissionUtil.canAccessReadContacts(m_act))
                        startGui();
                    else {

                        DialogUtil.confirmDialog(m_act,
                                "Permission Request",
                                "CounterCloud requires permission to access contacts for the purpose of generating a cloud data report.",
                                "Exit", "Permit access",
                                new DialogUtil.DialogCallback() {
                                    @Override
                                    public void confirmed() {
                                        PermissionUtil.requestReadWriteContacts(m_act, REQUEST_ID_READ_CONTACTS);
                                    }
                                    @Override
                                    public void canceled() {
                                        m_act.finish();
                                    }
                                }
                        );
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch( requestCode){

            case REQUEST_ID_READ_CONTACTS:{

                if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startGui();
                }
                break;
            }
            default:
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "onSaveInstanceState()");
    }

    protected void onResume() {
        super.onResume();
        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "onResume()");

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
    public void onDestroy() {
        super.onDestroy();
        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "onPause()");
    }

    void startGui(){

        // Show the Up button in the action bar.
        ActionBarUtil.homeAsUpEnabled(m_act, false);

        if( Persist.getTimeLastUpdate( m_ctx) == 0){

            // Starting for first time, clone google db to track changes
            WorkerCommand.cloneGoogleDb(m_act);

            // Set default settings
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);

            // Set time
            Persist.setTimeLastUpdate(m_ctx, System.currentTimeMillis());
        }
        WorkerCommand.registerCloudObserver(getApplicationContext());

        ActionBarUtil.showTitleEnabled(m_act, true);

        m_main_frag = startCloudMainFragment();

        /**
         * Detect app upgrade and provide a placeholder for managing upgrades, database changes, etc.
         */
        m_appUpgraded = LicenseUtil.appUpgraded(m_act);

        if( m_appUpgraded ) {

            Toast.makeText(getApplicationContext(), "Application upgraded", Toast.LENGTH_LONG).show();

            // Execute upgrade methods
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cloud_main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_refresh:{

                if( m_main_frag == null)
                    m_main_frag = startCloudMainFragment();
                m_main_frag.resetCloudSummary();
                Toast.makeText(m_act, "Refresh", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.menu_security_check:{

                Intent intent = new Intent(this, CloudManagerActivity.class);
                intent.putExtra(CConst.ACCOUNT_KEY, CConst.ALL_ACCOUNTS);
                intent.putExtra(CConst.SECURITY_CHECK, true);
                startActivity(intent);
                break;
            }
            case R.id.menu_event_log:{

                Intent intent = new Intent(this, EventLogActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_app_survey:{

                Intent intent = new Intent(this, AppSurveyActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_settings:{

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.menu_help:{

                String url = CConst.APP_HELP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }

            default:
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "onActivityResult()");

        switch( requestCode ){

            default:
                LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "ERROR, CloudMainActivity invalid requestCode: "+requestCode);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Callback method indicating that the item with the given ID was selected.
     */
    @Override
    public void onAccountEditSelected(String myAccount) {

        Intent intent = new Intent(this, CloudManagerActivity.class);
        intent.putExtra(CConst.ACCOUNT_KEY, myAccount);
        startActivity(intent);
    }
    /**
     * Callback method from {@link CloudMainFragment} indicating that
     * data has changed and the fragment should be refreshed
     */
    @Override
    public void onRefreshFragment() {

        if( m_main_frag == null)
            m_main_frag = startCloudMainFragment();
        else
            refreshCloudMainFragment();
    }

    private void refreshCloudMainFragment(){

        if( m_main_frag == null)
            m_main_frag = startCloudMainFragment();
        FragmentTransaction ft = m_main_frag.getFragmentManager().beginTransaction();
        ft.detach(m_main_frag);
        ft.commit();
        ft = m_main_frag.getFragmentManager().beginTransaction();
        ft.attach(m_main_frag);
        ft.commitAllowingStateLoss();
    }
    private CloudMainFragment startCloudMainFragment(){

        LogUtil.log(LogType.CLOUD_MAIN_ACTIVITY, "startCloudMainFragment()");

        CloudMainFragment fragment = new CloudMainFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.cloud_main_container, fragment);
        ft.commitAllowingStateLoss();
        return fragment;
    }

    public void appSurveySummaryOnClick(View view) {

        Intent intent = new Intent(this, AppSurveyActivity.class);
        startActivity(intent);
    }
}
