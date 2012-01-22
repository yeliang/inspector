package com.particle.inspector.authsrv.activity;  
  
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

import com.particle.inspector.authsrv.R;
import com.particle.inspector.authsrv.R.string;
import com.particle.inspector.authsrv.R.xml;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
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
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals("pref_sms_clean_interval")) {
					String interval = sharedPreferences.getString("pref_sms_clean_interval", "48"); //Hours
					int intervalHours = Integer.parseInt(interval);
				}
				else if (key.equals("pref_key_warning_interval")) {
					String interval = sharedPreferences.getString("pref_key_warning_interval", "24"); //Hours
					int intervalHours = Integer.parseInt(interval);
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
	
	public static int getSmsInterval(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sms_clean_interval", "48")); // Hour
	}
	
	public static void setSmsInterval(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("pref_sms_clean_interval", value).commit();
	}
	
	public static int getWarningInterval(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_key_warning_interval", "48")); // Hour
	}
	
	public static void setWarningInterval(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("pref_key_warning_interval", value).commit();
	}
	
}