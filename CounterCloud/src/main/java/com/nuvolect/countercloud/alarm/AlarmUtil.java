/*
 * Copyright (c) 2011 - 2015, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC.
 * Any unauthorized use is strictly prohibited.
 */
package com.nuvolect.countercloud.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.nuvolect.countercloud.util.LogUtil;
import com.nuvolect.countercloud.util.TimeUtil;

import java.util.Calendar;


public class AlarmUtil {

    /**
     * Set a heartbeat to fire the same time each day.
     * The heartbeat will publish app meta-data.
     * @param ctx
     */
    public static void setHeartbeatAlarm(Context ctx){

        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.SECOND, 30);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(ctx, HeartbeatAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(ctx,
                123, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        LogUtil.log(LogUtil.LogType.BOOT, "setHeartbeatAlarm: "
                + TimeUtil.isoTimeString(cal.getTimeInMillis()));
    }
}
