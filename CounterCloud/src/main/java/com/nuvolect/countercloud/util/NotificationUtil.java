/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */
package com.nuvolect.countercloud.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.nuvolect.countercloud.R;
import com.nuvolect.countercloud.main.CConst;
import com.nuvolect.countercloud.main.CloudMainActivity;

import java.util.ArrayList;

public class NotificationUtil {
	private static final int NOTIFICATION_ID = 1;

	private static ArrayList<String> smallTextHistory = new ArrayList<String>();
	private static int runningTotal = 0;

	public static void pushNotification(Context ctx, String title, String smallText) {

		// Make sure duplicates are not added to the list
		if( smallTextHistory.size() > 0 && smallTextHistory.get(0).contentEquals( smallText))
		    return;

		smallTextHistory.add( 0, smallText);
		if( smallTextHistory.size() > 6){       // Only keep last six

		    for(int i = smallTextHistory.size() -1; i > 5; --i)
		        smallTextHistory.remove(i);
		}

		Resources res = ctx.getResources();
		Bitmap largeIcon = BitmapFactory.decodeResource( res, R.drawable.cc_small_icon);

		// When using proper icon sizes this scaling code is not required
		//		int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
		//		int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
		//		largeIcon = Bitmap.createScaledBitmap(largeIcon, width, height, false);

        Class<?> nextActivity = CloudMainActivity.class;

        Intent nextIntent = new Intent(ctx, nextActivity);
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        
        TaskStackBuilder tsb = TaskStackBuilder.create(ctx);
                tsb.addParentStack( nextActivity );
                tsb.addNextIntent( nextIntent );

        PendingIntent pendingIntent = tsb.getPendingIntent(0, 0);

		@SuppressWarnings("deprecation")
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx)
		.setTicker( title)
		.setContentTitle( title)
		.setSmallIcon( CConst.NOTIFICATION_ICON)
		.setLargeIcon( largeIcon)
		.setAutoCancel( false)
		.setNumber( ++runningTotal)
		.setAutoCancel(true)
		.setContentIntent( pendingIntent);

		// Use the previous notification as small text, if there is one
		if( smallTextHistory.size() > 0)
			builder.setContentText( smallTextHistory.get(0));
		
		NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

		inboxStyle.setBigContentTitle( title );
		builder.setStyle(inboxStyle);

		for( String textItem : smallTextHistory)
			inboxStyle.addLine(textItem);

		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify( NOTIFICATION_ID, inboxStyle.build());

		// Save the title as the top of the small text list for next notification
		smallTextHistory.add( 0, title);
	}

	public static void cancelAll(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
	}
}
