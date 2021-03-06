/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.data.DbProvider;
import com.nuvolect.countercloud.util.LogUtil.LogType;

import java.io.File;

public class EmailUtil {

    public static void emailEventLog(Activity act){

        try {
            String userMessage = "EventLog from CounterCloud";

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/xml");

            intent.putExtra(Intent.EXTRA_SUBJECT, "CounterCloud Event Log");
            intent.putExtra(Intent.EXTRA_TEXT, userMessage);

            String fileName = "event_log.csv";
            DbProvider.writeEventLog( act, fileName);

            // full path to the attachment
            String path = act.getFilesDir().getPath() ;
            File file = new File( path, fileName);
            
            // Must match "authorities" in Manifest provider definition
            String authorities = act.getResources().getString(R.string.app_authorities)+".provider";

            Uri uri = FileProvider.getUriForFile( act, authorities, file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            act.startActivity(Intent.createChooser(intent, "Send email..."));

        } catch (Exception e) {
            LogUtil.logException(act, LogType.EXPORT_EVENT_LOG, e);
        }
    }
}
