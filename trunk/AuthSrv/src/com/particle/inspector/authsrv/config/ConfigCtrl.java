package com.particle.inspector.authsrv.config;

import java.util.Date;

import com.particle.inspector.common.util.DatetimeUtil;

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
	private static final String LAST_CLEANSMS_DATETIME = "LastCleanSmsDatetime";
	
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
	
	public static String getLastCleanSmsDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LAST_CLEANSMS_DATETIME, "");
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setLastCleanSmsDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString(LAST_CLEANSMS_DATETIME, datetime == null ? "" : DatetimeUtil.format.format(datetime));
		return editor.commit();
	}
	
}
