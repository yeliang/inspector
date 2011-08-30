package com.system.feature.pref;  
  
import java.util.Date;

import android.content.Context;
import android.content.Intent;  
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.TypedArray;  
import android.net.Uri;  
import android.os.Bundle;  
import android.preference.CheckBoxPreference;  
import android.preference.EditTextPreference;  
import android.preference.ListPreference;  
import android.preference.PreferenceActivity;  
import android.preference.PreferenceCategory;  
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import com.system.R;
  
public class GlobalPref extends PreferenceActivity 
{
	private static String USERNAME = Resources.getSystem().getString(R.string.pref_username_key);
	private static String SERIALNUM = Resources.getSystem().getString(R.string.pref_serialnum_key);
	private static String MAIL = Resources.getSystem().getString(R.string.pref_mail_key);
	private static String INTERVAL_INFO = Resources.getSystem().getString(R.string.pref_info_interval_key);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
	}
	
	public static String getUsername(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(USERNAME, "");
	}
	
	public static void setUsername(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(USERNAME, value).commit();
	}
	
	public static String getSerialNum(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SERIALNUM, "");
	}
	
	public static void setSerialNum(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SERIALNUM, value).commit();
	}
	
	public static String getMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(MAIL, "");
	}
	
	public static void setMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MAIL, value).commit();
	}
	
	public static String getIntervalInfo(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(INTERVAL_INFO, "");
	}
	
	public static void setIntervalInfo(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(INTERVAL_INFO, value).commit();
	}
	
}