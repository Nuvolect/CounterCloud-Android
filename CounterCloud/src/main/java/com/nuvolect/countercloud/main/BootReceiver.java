/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */
package com.nuvolect.countercloud.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nuvolect.countercloud.main.WorkerCommand;

/**
 * Start the app in the background when the device boots.
 * This is necessary to track item addition, deletion and update.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent intent) {

	    WorkerCommand.registerCloudObserver( ctx);
	}
}
