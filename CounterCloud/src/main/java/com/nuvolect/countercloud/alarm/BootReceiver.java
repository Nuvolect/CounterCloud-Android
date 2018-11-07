/*******************************************************************************
 * Copyright (c) 2011 - 2014, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC. 
 * Any unauthorized use is strictly prohibited.
 ******************************************************************************/
package com.nuvolect.countercloud.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nuvolect.countercloud.main.WorkerCommand;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent intent) {

	    WorkerCommand.registerCloudObserver( ctx);
		AlarmUtil.setHeartbeatAlarm( ctx);
	}
}
