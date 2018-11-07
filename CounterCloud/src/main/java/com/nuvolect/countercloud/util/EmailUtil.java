package com.nuvolect.countercloud.util;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.nuvolect.countercloud.data.DbProvider;
import com.nuvolect.countercloud.util.LogUtil.LogType;

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

            Uri uri = FileProvider.getUriForFile( act, "com.nuvolect.countercloud.files", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);

            act.startActivity(Intent.createChooser(intent, "Send email..."));

        } catch (Exception e) {
            LogUtil.logException(act, LogType.EXPORT_EVENT_LOG, e);
        }
    }
}
