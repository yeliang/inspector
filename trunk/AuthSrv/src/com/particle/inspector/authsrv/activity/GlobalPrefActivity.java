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
import com.particle.inspector.authsrv.util.StrUtils;
import com.particle.inspector.authsrv.util.SysUtils;
  
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
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(R.string.xxx))) {
					checkMailFormat(sharedPreferences, getApplicationContext());
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
	
	public static String getMail(Context context) {
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(MAIL, "").trim();
	}
	
	public static void setMail(Context context, String value) {
		MAIL = context.getResources().getString(R.string.pref_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(MAIL, value).commit();
	}
	
}