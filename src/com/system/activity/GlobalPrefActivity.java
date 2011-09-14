package com.system.activity;  
  
import java.util.Date;

import android.content.Context;
import android.content.Intent;  
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.util.Log;

import com.system.R;
import com.system.utils.SysUtils;
import com.system.utils.license.LicenseCtrl;
import com.system.utils.license.LicenseType;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
	private static String SERIALNUM;
	private static String MAIL;
	private static String INTERVAL_INFO;
	private LicenseType isLicensed;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
		
		// Init preference summary fields
		String mail = getMail(getApplicationContext());
		if (mail.length() > 0) {
			EditTextPreference mailEdit = (EditTextPreference)this.getPreferenceScreen().getPreference(R.string.pref_mail_key);
			mailEdit.setSummary(mail);
		}
		String serialNum = getSerialNum(getApplicationContext());
		if (serialNum.length() > 0) {
			EditTextPreference serialNumEdit = (EditTextPreference)this.getPreferenceScreen().getPreference(R.string.pref_serialnum_key);
			serialNumEdit.setSummary(serialNum);
		}
		String intervalInfo = getIntervalInfo(getApplicationContext());
		if (intervalInfo.length() > 0) {
			ListPreference intervalInfoEdit = (ListPreference)this.getPreferenceScreen().getPreference(R.string.pref_info_interval_key);
			intervalInfoEdit.setSummary(intervalInfo);
		}
		
		// Register	preference change listener
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(R.string.pref_mail_key))) {
					verifySerialNum(sharedPreferences, getApplicationContext());
					
				}
				else if (key.equals(getResources().getString(R.string.pref_serialnum_key))) {
					verifySerialNum(sharedPreferences, getApplicationContext());
				}
				else if (key.equals(getResources().getString(R.string.pref_info_interval_key))) {
					int interval = sharedPreferences.getInt(getResources().getString(R.string.pref_info_interval_key), 1); //day
				}
			}
        });
	}
	
	private void verifySerialNum(SharedPreferences sharedPreferences, Context context) {
		String mail = sharedPreferences.getString(context.getResources().getString(R.string.pref_mail_key), "").trim();
		String serialNum = sharedPreferences.getString(context.getResources().getString(R.string.pref_serialnum_key), "").trim();
		if (mail.length() == 0 && serialNum.length() > 0) {
			SysUtils.messageBox(context.getApplicationContext(), context.getResources().getString(R.string.pls_input_mail, ""));
		} else if (mail.length() > 0 && serialNum.length() > 0) {
			LicenseType type = LicenseCtrl.isLicensed(context.getApplicationContext(), mail, serialNum);
			if (type == LicenseType.FullLicensed) {
				this.isLicensed = LicenseType.FullLicensed;
				SysUtils.messageBox(context.getApplicationContext(), context.getResources().getString(R.string.licensed_yes, ""));
			} else if (type == LicenseType.OnlySmsLicensed) {
				this.isLicensed = LicenseType.OnlySmsLicensed;
				SysUtils.messageBox(context.getApplicationContext(), context.getResources().getString(R.string.licensed_yes, ""));
			} else {
				this.isLicensed = LicenseType.NotLicensed;
				SysUtils.messageBox(context.getApplicationContext(), context.getResources().getString(R.string.licensed_no, ""));
			}
			
			// Set result
			Intent data = this.getIntent();
			if (this.isLicensed == LicenseType.FullLicensed) {
				data.putExtra("isLicensed", "full");
			} else if (this.isLicensed == LicenseType.OnlySmsLicensed) {
				data.putExtra("isLicensed", "onlysms");
			} else {
				data.putExtra("isLicensed", "no");
			}
			setResult(RESULT_OK, data);
		}
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
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(MAIL, "").trim();
	}
	
	public static void setMail(Context context, String value) {
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MAIL, value).commit();
	}
	
	public static String getIntervalInfo(Context context) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(INTERVAL_INFO, "").trim();
	}
	
	public static void setIntervalInfo(Context context, String value) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(INTERVAL_INFO, value).commit();
	}
	
}