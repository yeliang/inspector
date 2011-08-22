package com.system.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Implementation of configurations I/O by SharedPreferences.
 * in Android, these configurations will be saved in /data/data/PACKAGE_NAME/shared_prefs directory with XML format.
*/
public class ConfigCtrl 
{
	private static final String PREFS_NAME = "com.system";
	private static final String IS_LICENSED = "IsLicensed";
	private static final String INTERVAL = "Interval";
	
	public static void set(Activity activity, String key, String value)
	{	
		Editor editor = activity.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(key, value);     
		editor.commit();
	}
	
	public static String get(Activity activity, String key)
	{
		SharedPreferences config = activity.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getString(key, "false");
	}
	
	public static boolean getIsLicensed(Activity activity)
	{
		SharedPreferences config = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return config.getBoolean(IS_LICENSED, false);
	}
	
	public static void setIsLicensed(Activity activity, boolean value)
	{
		Editor editor = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putBoolean(IS_LICENSED, value);     
		editor.commit();
	}
	
	public static int getInterval(Activity activity)
	{
		SharedPreferences config = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return config.getInt(INTERVAL, 60000);
	}
	
	public static void setInterval(Activity activity, int interval)
	{
		Editor editor = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putInt(INTERVAL, interval);     
		editor.commit();
	}
	
}
