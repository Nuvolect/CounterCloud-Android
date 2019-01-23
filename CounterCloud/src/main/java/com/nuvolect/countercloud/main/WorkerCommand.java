/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.main;

import android.content.Context;
import android.content.Intent;

public class WorkerCommand {

    public static void registerCloudObserver(Context ctx){

        Intent i = new Intent( ctx, WorkerService.class);
        i.putExtra("command",WorkerService.Command.REGISTER_CLOUD_OBSERVER.ordinal());
        ctx.startService( i );
    }
    public static void unregisterCloudObserver(Context ctx){

        Intent i = new Intent( ctx, WorkerService.class);
        i.putExtra("command",WorkerService.Command.UNREGISTER_CLOUD_OBSERVER.ordinal());
        ctx.startService( i );
    }

	public static void cloneGoogleDb(Context ctx) {

        Intent i = new Intent( ctx, WorkerService.class);
        i.putExtra("command",WorkerService.Command.CLONE_CLOUD_DB.ordinal());
        ctx.startService( i );
	}
    public static void notifyLogUpdate(Context ctx) {

        Intent i = new Intent( ctx, WorkerService.class);
        i.putExtra("command",WorkerService.Command.NOTIFY_LOG_UPDATE.ordinal());
        ctx.startService( i );
    }
}
