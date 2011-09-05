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
	private static String SERIALNUM;
	private static String MAIL;
	private static String INTERVAL_INFO;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
	}
	
	public static String getSerialNum(Context context) {
		SERIALNUM = context.getResources().getString(R.string.pref_serialnum_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SERIALNUM, "").trim();
	}
	
	public static void setSerialNum(Context context, String value) {
		SERIALNUM = context.getResources().getString(R.string.pref_serialnum_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SERIALNUM, value).commit();
	}
	
	public static String getMail(Context context) {
		MAIL = SERIALNUM = context.getResources().getString(R.string.pref_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(MAIL, "").trim();
	}
	
	public static void setMail(Context context, String value) {
		MAIL = SERIALNUM = context.getResources().getString(R.string.pref_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MAIL, value).commit();
	}
	
	public static String getIntervalInfo(Context context) {
		INTERVAL_INFO = SERIALNUM = context.getResources().getString(R.string.pref_info_interval_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(INTERVAL_INFO, "").trim();
	}
	
	public static void setIntervalInfo(Context context, String value) {
		INTERVAL_INFO = SERIALNUM = context.getResources().getString(R.string.pref_info_interval_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(INTERVAL_INFO, value).commit();
	}
	
}