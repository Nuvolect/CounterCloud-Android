/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.survey;//

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.PermissionUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Survey apps running on the device that can read and write contacts.
 */
public class AppSurveyFragment extends Fragment {

    private static final boolean VERBOSE = LogUtil.VERBOSE;
    Activity m_act;
    Context m_ctx;

    private View m_rootView;
    private LayoutInflater m_inflater;
    private ViewGroup m_container;

    private ArrayList<AppItem> m_appItems;
    private float iconSizeDp = 50;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AppSurveyFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.log(LogUtil.LogType.APP_SURVEY_FRAGMENT, "onCreate()");

        m_act = getActivity();
        m_ctx = m_act.getApplicationContext();

        /**
         * Load known custom and google packages from raw resources into a JSON object
         */
        AppSurveyExec.getAppDb(m_act);//SPRINT delete
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.log( LogUtil.LogType.APP_SURVEY_FRAGMENT, "onResume()");

        new QueryAppsAsync().execute();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        LogUtil.log(LogUtil.LogType.APP_SURVEY_FRAGMENT, "onCreateView()");

        m_inflater = inflater;// Save for refresh
        m_container = container;
        m_rootView = inflater.inflate(R.layout.app_survey_fragment, container, false);

        return m_rootView;
    }

    private class QueryAppsAsync extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void...arg0) {

            generateAppSurvey();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            if( m_act == null || m_act.isFinishing())
                return;

            buildView();
        }
    }

    /**
     * Generate a survey or update the survey for all contacts apps of a single device.
     */
    private void generateAppSurvey() {

        PackageManager pm = m_ctx.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        /**
         * Save preferences.  Manage lists to have same number of elements.
         */
        m_appItems = new ArrayList<AppItem>();

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName, PackageManager.GET_PERMISSIONS);

                //Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;
                AppItem appItem = new AppItem();

                if(requestedPermissions != null) {

                    for (String requestedPermission : requestedPermissions) {

                        if (requestedPermission.contains("CONTACTS")) {

                            if (requestedPermission.contains("READ_CONTACTS")) {

                                appItem.appReadPriv = true;
                            }

                            if (requestedPermission.contains("WRITE_CONTACTS")) {

                                appItem.appWritePriv = true;
                            }
                        }
                    }
                    if( appItem.appReadPriv || appItem.appWritePriv){

                        appItem.appIcon = applicationInfo.loadIcon(pm);
                        appItem.appName = String.valueOf(applicationInfo.loadLabel(pm));
                        appItem.appPackageName = applicationInfo.packageName;
                        m_appItems.add(appItem);
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        LogUtil.log(LogUtil.LogType.APP_SURVEY_FRAGMENT, "Survey size: " + m_appItems.size());

        // Sort on App Name
        m_appItems = sortApps(m_appItems);
    }

    // On the UI thread, build the view
    private void buildView() {

        TableLayout tableLayout = (TableLayout) m_rootView.findViewById(R.id.surveyTable);
        tableLayout.removeAllViews();

        View headerRow = m_inflater.inflate(R.layout.app_survey_header, m_container, false);
        tableLayout.addView(headerRow);

        int iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                iconSizeDp, getResources().getDisplayMetrics());

        for( int appIndex = 0; appIndex < m_appItems.size(); ++appIndex){

            final AppItem appItem = m_appItems.get(appIndex);

            // Create a table row and add all the parts and add the row to the view
            TableRow tableRow = new TableRow(m_act);
            tableRow.setVerticalGravity(Gravity.CENTER_VERTICAL);
            tableRow.setTag(appIndex);
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = (int) v.getTag();
                    PermissionUtil.showInstalledAppDetails(m_act, appItem.appPackageName);
                }
            });

            ImageView iv = new ImageView(m_act);
            TableRow.LayoutParams params = new TableRow.LayoutParams(iconSize, iconSize);
            iv.setLayoutParams(params);

            if( appItem.appIcon != null)
                iv.setImageDrawable(appItem.appIcon);
            else
                iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));

            tableRow.addView(iv);

            TextView tv = customTextView(m_act, false);
            tv.setPadding(5, 0, 0, 0);
            if( appItem.appName != null){
                if( appItem.appName.length() > 25)
                    tv.setText( appItem.appName.substring(0,25)+"..");
                else
                    tv.setText( appItem.appName);
            }
            else
                tv.setText(" ");
            tableRow.addView(tv);

            tv = customTextView(m_act, true);
            tv.setText( appItem.appReadPriv ? "X" : " ");
            tableRow.addView(tv);

            tv = customTextView(m_act, true);
            tv.setText( appItem.appWritePriv ? "X" : " ");
            tableRow.addView(tv);

            // Finally add the row to the view
            tableLayout.addView(tableRow);
        }

        TextView tv = (TextView) m_rootView.findViewById(R.id.surveySummaryTv);
        tv.setText("Total: "+m_appItems.size()+" apps");
        tv = (TextView) m_rootView.findViewById(R.id.appTotalTv);
        tv.setText(": "+m_appItems.size());
    }

    /**
     *  Comparator for sorting Apps by name.
     */
    public static class CustomComparatorName implements Comparator<AppItem> {

        public int compare(AppItem a1, AppItem a2){
            String s1 = a1.appName;
            String s2 = a2.appName;
            return s1 .compareTo( s2);
        }
    }

    /**
     * Sort Apps by name
     * @param unsortedApps
     * @return sorted ArrayList of Apps
     */
    public static ArrayList<AppItem> sortApps(ArrayList<AppItem> unsortedApps){

        Collections.sort(unsortedApps, new CustomComparatorName());

        return unsortedApps;
    }

    private TextView customTextView(Activity act, boolean centerHorizontal) {

        TextView tv = new TextView(act);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        if(centerHorizontal)
            tv.setGravity( Gravity.CENTER_HORIZONTAL);

        return tv;
    }
}
