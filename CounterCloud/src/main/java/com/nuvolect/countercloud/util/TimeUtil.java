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
	static public String friendlyTimeString(long t){

		simpleFormat.setTimeZone(TimeZone.getDefault());
		return simpleFormat.format( t);
	}

	/**
	 * Return time in the ISO 8601 time standard as a string
	 * @return string
	 */
	static public String isoTimeString(long t){

		sdfIso.setTimeZone(TimeZone.getDefault());
		return sdfIso.format( t);
	}
}
