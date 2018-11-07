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
	public static void publishAppSurvey(Context ctx) {

        Intent i = new Intent( ctx, WorkerService.class);
        i.putExtra("command",WorkerService.Command.PUBLISH_APP_SURVEY.ordinal());
        ctx.startService( i );
	}
}
