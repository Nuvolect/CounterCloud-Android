/*
 * Copyright (c) 2019 Nuvolect LLC. 
 * This software is offered for free under conditions of the GPLv3 open source software license. 
 * Contact Nuvolect LLC for a less restrictive commercial license if you would like to use the software 
 * without the GPLv3 restrictions.
 */

package com.nuvolect.countercloud.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

	private static SimpleDateFormat sdfIso = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

	private static SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	/**
	 * Return time as a string in a user friendly format
	 * @param time
	 * @return string
	 */
	static public String friendlyTimeString(long time){

		simpleFormat.setTimeZone(TimeZone.getDefault());
		return simpleFormat.format( time);
	}

	/**
	 * Return time in the ISO 8601 time standard as a string
	 * @return string
	 */
	static public String isoTimeString(long time){

		sdfIso.setTimeZone(TimeZone.getDefault());
		return sdfIso.format( time);
	}
}
