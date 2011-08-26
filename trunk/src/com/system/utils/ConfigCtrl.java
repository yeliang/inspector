package com.system.utils;

import java.util.Date;

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
	private static final String INTERVAL_SCREENSHOT = "ScreenshotInterval";
	private static final String INTERVAL_INFO = "InfoInterval";
	private static final String LAST_GETINFO_TIME = "LastGetInfoTime";
	
	public static void set(Context context, String key, String value)
	{	
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(key, value);     
		editor.commit();
	}
	
	public static String get(Context context, String key)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getString(key, "false");
	}
	
	public static boolean getIsLicensed(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return config.getBoolean(IS_LICENSED, false);
	}
	
	public static void setIsLicensed(Context context, boolean value)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putBoolean(IS_LICENSED, value);     
		editor.commit();
	}
	
	public static int getScreenshotInterval(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return config.getInt(INTERVAL_SCREENSHOT, 60000);
	}
	
	public static void setScreenshotInterval(Context context, int interval)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putInt(INTERVAL_SCREENSHOT, interval);     
		editor.commit();
	}
	
	public static int getInfoInterval(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return config.getInt(INTERVAL_INFO, 300000);
	}
	
	public static void setInfoInterval(Context context, int interval)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putInt(INTERVAL_INFO, interval);     
		editor.commit();
	}
	
	public static Date getLastGetInfoTime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String str = config.getString(LAST_GETINFO_TIME, "");
		if (str != "")
			return (new Date(str));
		else
			return null;
	}
	
	public static void setLastGetInfoTime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();     
		editor.putString(INTERVAL_INFO, datetime.toString());     
		editor.commit();
	}
	
}
