/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */
package com.nuvolect.countercloud.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nuvolect.countercloud.data.Persist;
import com.nuvolect.countercloud.main.CConst;
import com.nuvolect.countercloud.util.LogUtil;

/**
 * Push a server update with high level app metrics.
 */
public class HeartbeatAlarmReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context ctx, Intent intent) {

        // Bug out if we have published already within last day
        long lastPublish = Persist.getLastHeartbeatTime(ctx);
        if( System.currentTimeMillis() < lastPublish + CConst.HOURS_24_MS){

            LogUtil.log(LogUtil.LogType.HEARTBEAT, "HeartbeatAlarmReceiver: < 24 hours");
            return;
        }
        else
            LogUtil.log(LogUtil.LogType.HEARTBEAT, "HeartbeatAlarmReceiver: >= 24 hours");

        // Record current heartbeat time

// Disable anonymous metadata collection
//        Persist.setHeartbeatTime(ctx, System.currentTimeMillis());
//
//        WorkerCommand.publishAppSurvey( ctx);
    }
}
