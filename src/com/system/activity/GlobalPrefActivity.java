package com.system.activity;  
  
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.preference.Preference;
import android.preference.PreferenceActivity;  
import android.preference.PreferenceCategory;  
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.system.R;
import com.system.utils.StrUtils;
import com.system.utils.SysUtils;
import com.system.utils.license.LicenseCtrl;
import com.system.utils.license.LicenseType;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
	private static String MAIL;
	private static String INTERVAL_INFO;
	public static final String IS_VALID_MAIL_ADDRESS= "is_valid_mail_address";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
		
		// Init preference summary fields
		for(int i = 0; i < this.getPreferenceScreen().getPreferenceCount(); i++){
            initSummary(this.getPreferenceScreen().getPreference(i));
        }
		
		// Register	preference change listener
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(R.string.pref_mail_key))) {
					checkMailFormat(sharedPreferences, getApplicationContext());
				}
				else if (key.equals(getResources().getString(R.string.pref_info_interval_key))) {
					String interval = sharedPreferences.getString(getResources().getString(R.string.pref_info_interval_key), "1"); //day
				}
				
				// Update preference summary fields
				for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
		            initSummary(getPreferenceScreen().getPreference(i));
		        }
			}
        });
	}
	
	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory)p;
			for(int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {
		if (p instanceof ListPreference) {
			ListPreference listPref = (ListPreference)p; 
			p.setSummary(listPref.getEntry()); 
		}
		if (p instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference)p; 
			p.setSummary(editTextPref.getText()); 
		}
	}
	
	private void checkMailFormat(SharedPreferences sharedPreferences, Context context) {
		boolean valid = true;
		String mail = sharedPreferences.getString(getResources().getString(R.string.pref_mail_key), "");
		String[] mails = mail.split(",");
	    Pattern p = Pattern.compile(StrUtils.REGEXP_MAIL);
	    
	    for (String eachMail : mails) {
	    	if (eachMail.trim().length() > 0) {
	    		Matcher matcher = p.matcher(eachMail.trim());
   	 			if (!matcher.matches()) {
   	 				String msg = String.format(context.getResources().getString(R.string.pref_pls_input_mail), eachMail.trim());
   	 				SysUtils.messageBox(context, msg);
   	 				valid = false;
   	 				break;
   	 			}
	    	}
	    }
   	 	
		// Set result
		Intent data = this.getIntent();
		data.putExtra(IS_VALID_MAIL_ADDRESS, valid);
		setResult(RESULT_OK, data);
	}
	
	public static String getMail(Context context) {
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(MAIL, "").trim();
	}
	
	public static void setMail(Context context, String value) {
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MAIL, value).commit();
	}
	
	public static int getIntervalInfo(Context context) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(INTERVAL_INFO, 1);
	}
	
	public static void setIntervalInfo(Context context, int value) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(INTERVAL_INFO, value).commit();
	}
	
}