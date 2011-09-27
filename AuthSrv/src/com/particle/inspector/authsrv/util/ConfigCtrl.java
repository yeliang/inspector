package com.particle.inspector.authsrv.util;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Implementation of configurations I/O by SharedPreferences.
 * in Android, these configurations will be saved in /data/data/PACKAGE_NAME/shared_prefs directory with XML format.
*/
public class ConfigCtrl 
{
	private static final String PREFS_NAME = "com.particle.inspector.authsrv";
	private static final String CONSUMED_DATETIME = "ConsumedDatetime"; // The 1st activation datetime
	private static final String LAST_ACTIVATED_DATETIME = "LastActivatedDatetime"; // The last activation datetime
	private static final String LAST_GETINFO_DATETIME = "LastGetInfoDatetime";
	
	public static boolean set(Context context, String key, String value)
	{	
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(key, value);     
		return editor.commit();
	}
	
	public static String get(Context context, String key)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getString(key, "false");
	}
	
	public static Date getLastGetInfoTime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LAST_GETINFO_DATETIME, "");
		if (str != "")
			return (new Date(str));
		else
			return null;
	}
	
	public static boolean setLastGetInfoTime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(LAST_GETINFO_DATETIME, datetime.toString());     
		return editor.commit();
	}
	
	public static Date getConsumedDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(CONSUMED_DATETIME, "");
		if (str != "")
			return (new Date(str));
		else
			return null;
	}
	
	public static boolean setConsumedDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(CONSUMED_DATETIME, datetime.toString());     
		return editor.commit();
	}
	
	public static Date getLastActivatedDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LAST_ACTIVATED_DATETIME, "");
		if (str != "")
			return (new Date(str));
		else
			return null;
	}
	
	public static boolean setLastActivatedDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(LAST_ACTIVATED_DATETIME, datetime.toString());     
		return editor.commit();
	}
}
