package com.nuvolect.countercloud.survey;//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.license.AppSpecific;
import com.nuvolect.countercloud.main.CConst;
import com.nuvolect.countercloud.main.SettingsActivity;

//TODO create class description
//
public class AppSurveyActivity extends FragmentActivity {

    Activity m_act;
    Context m_ctx;
    AppSurveyFragment m_as_fragment;

    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        m_act = this;
        m_ctx = getApplicationContext();

        setContentView(R.layout.app_survey_activity);

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            m_as_fragment = startAppSurveyFragment();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_survey_menu, menu);

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

    private AppSurveyFragment startAppSurveyFragment() {

        AppSurveyFragment fragment = new AppSurveyFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.app_survey_container, fragment, CConst.APP_SURVEY_FRAGMENT_TAG);
        ft.commit();
        return fragment;
    }
}
