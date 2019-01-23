/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.license.LicensePersist;
import com.nuvolect.countercloud.util.ActionBarUtil;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.PermissionManager;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final boolean DEBUG = LogUtil.DEBUG;
    static Activity m_act;
    private View m_rootView;
    private String mLicenseSummary;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(DEBUG)LogUtil.log("SettingsFragment onCreate");

        m_act = getActivity();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);

        // Show the Up button in the action bar.
        ActionBarUtil.showActionBarUpButton(m_act);

        // Set license summary
        mLicenseSummary = LicensePersist.getLicenseSummary( m_act);
        Preference licensePref = findPreference(LicensePersist.APP_LICENSE);
        licensePref.setSummary(mLicenseSummary);

        updatePermissionsSummary();
    }

    private void updatePermissionsSummary(){

        // Display current permissions
        String permissions = PermissionManager.getInstance(m_act).getSummary();
        Preference permissionManagerPref = findPreference(CConst.PERMISSION_MANAGER);
        permissionManagerPref.setSummary("Enabled: " + permissions);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        m_rootView = inflater.inflate( R.layout.settings_preference, container, false);

        String version = "";
        try {
            PackageInfo pInfo = m_act.getPackageManager().getPackageInfo(m_act.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e1) { }

        TextView appVersionTv = (TextView) m_rootView.findViewById(R.id.settings_app_version);
        appVersionTv.setText("CounterCloud version " + version);

        appVersionTv.setOnClickListener( versionTextonClickListener );

        return m_rootView;
    }

    int userClicks = 0;
    View.OnClickListener versionTextonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if( ++userClicks >= 3){

                DeveloperDialog.start( m_act);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if(DEBUG)LogUtil.log("SettingsFragment onResume");
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(DEBUG)LogUtil.log("SettingsFragment onPause");
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Display the fragment in the provided container.
     * @param act
     * @param containerViewId
     */
    public static SettingsFragment startSettingsFragment(Activity act, int containerViewId){

        FragmentTransaction ft = act.getFragmentManager().beginTransaction();
        SettingsFragment frag = new SettingsFragment();
        ft.replace(containerViewId, frag);
        ft.commit();

        return frag;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if( preference.getKey().contentEquals( LicensePersist.APP_LICENSE)){

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse( CConst.DONATE_URL));
            startActivity(i);
        }
        if( preference.getKey().contentEquals( "open_source_license" )){

            DisplayOpenSourceInfoFragment frag = new DisplayOpenSourceInfoFragment();
            frag.show(getFragmentManager(), "display_open_source_info");
        }
        if( preference.getKey().contentEquals(CConst.PERMISSION_MANAGER)){

            PermissionManager.getInstance(m_act).showDialog(
                    new PermissionManager.PermissionMgrCallbacks() {
                        @Override
                        public void dialogOnCancel() {

                            updatePermissionsSummary();
                        }
                    }
            );
        }

        if( preference.getKey().contains("rate_app_google_play")){

            String url =
                    "https://play.google.com/store/apps/details?id=com.nuvolect.countercloud";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class DisplayOpenSourceInfoFragment extends DialogFragment {
        static DisplayOpenSourceInfoFragment newInstance() {
            return new DisplayOpenSourceInfoFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate( R.layout.open_source_license, container, false);

            setStyle( DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light);
            getDialog().setTitle("Software Licenses");

            return v;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch( requestCode ){

            default:
                if(DEBUG)LogUtil.log("onActivityResult: default action");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences pref, String key) {

        if(DEBUG)LogUtil.log("pref changed: "+key);

    }
}
