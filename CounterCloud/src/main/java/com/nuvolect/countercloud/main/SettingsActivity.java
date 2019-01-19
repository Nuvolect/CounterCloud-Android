package com.nuvolect.countercloud.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.license.AppSpecific;
import com.nuvolect.countercloud.util.ActionBarUtil;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.PermissionManager;
import com.nuvolect.countercloud.util.ShowTips;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final boolean DEBUG = false;
    public static final String DELETE_DIALOG_SETTING = "pref_delete_dialog";
    public static final String NOTIFY_INSERT_SETTING = "pref_notify_insert";
    public static final String NOTIFY_UPDATE_SETTING = "pref_notify_update";
    public static final String NOTIFY_DELETE_SETTING = "pref_notify_delete";
    Activity m_act;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_act = this;
        setContentView(R.layout.settings_preference);

        ActionBarUtil.showTitleEnabled(m_act, true);
        ActionBarUtil.homeAsUpEnabled(m_act, true);

        SettingsFragment.startSettingsFragment( m_act, R.id.settings_fragmment_container);
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
    public void onPause() {
        super.onPause();
        if(DEBUG)LogUtil.log("SettingsFragmentActivity onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(DEBUG)LogUtil.log("SettingsFragmentActivity onResume");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(DEBUG)LogUtil.log("SettingsFragmentActivity onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        getMenuInflater().inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:{
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.menu_whats_new:{
                String url = CConst.BLOG_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_show_tips:{

                boolean showTipsCheckBox = false;
                ShowTips.getInstance(m_act).dialogShowTips(showTipsCheckBox);
                break;
            }
            case R.id.menu_help:{
                String url = AppSpecific.APP_HELP_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.menu_donate: {

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse( CConst.DONATE_URL));
                startActivity(i);
                break;
            }
            case R.id.menu_developer_feedback:{
                int appVersion = 0;
                try {
                    appVersion = m_act.getPackageManager().getPackageInfo(
                            m_act.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"team@nuvolect.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "CounterCloud Feedback, App Version: "+appVersion);
                i.putExtra(Intent.EXTRA_TEXT   , "Please share your thoughts or ask a question.");

                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(m_act, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if( ! notifySettingsEnabled( this))
            WorkerCommand.unregisterCloudObserver(this);
    }

    public static boolean deleteDialogSetting(Context ctx){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean dialog_setting = sharedPref.getBoolean(DELETE_DIALOG_SETTING, true);

        return dialog_setting;
    }
    public static boolean notifyInsertSetting(Context ctx){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean setting = sharedPref.getBoolean(NOTIFY_INSERT_SETTING, true);

        return setting;
    }
    public static boolean notifyUpdateSetting(Context ctx){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean setting = sharedPref.getBoolean(NOTIFY_UPDATE_SETTING, true);

        return setting;
    }
    public static boolean notifyDeleteSetting(Context ctx){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean setting = sharedPref.getBoolean(NOTIFY_DELETE_SETTING, true);

        return setting;
    }

    public static void setShowTips(Context ctx, boolean checked) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putBoolean(CConst.SHOW_TIPS, checked).apply();
    }
    public static boolean getShowTips(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(CConst.SHOW_TIPS, true);
    }
    /**
     * If any of the notification settings are enabled, return true.
     * @param ctx
     * @return
     */
    public static boolean notifySettingsEnabled(Context ctx){

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean insert = sharedPref.getBoolean(NOTIFY_INSERT_SETTING, true);
        boolean update = sharedPref.getBoolean(NOTIFY_UPDATE_SETTING, true);
        boolean delete = sharedPref.getBoolean(NOTIFY_DELETE_SETTING, true);

        return insert || update || delete;
    }
    /**
     * Used by the PermissionManager to refresh when user makes changes.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionManager.getInstance(m_act).refresh();
    }
}
