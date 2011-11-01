package com.particle.inspector.keygen;  
  
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

import com.particle.inspector.keygen.R;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
	public static int DEFAULT_KEYS_NUMBER = 100;
	public static final String SETTINGS = "settings";
	
	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener(){
		@SuppressWarnings("unused")
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals("pref_key_count_key")) {
				int keyCount = DEFAULT_KEYS_NUMBER;
				try {
					keyCount = Integer.parseInt(sharedPreferences.getString("pref_key_count_key", String.valueOf(DEFAULT_KEYS_NUMBER)));
				} catch (NumberFormatException e) {
					SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_wrong_count_format));
				}
				if (keyCount > 999) {
					SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_key_over_count));
					setKeyCount(getApplicationContext(), DEFAULT_KEYS_NUMBER);
				}
			}
			else if (key.equals("pref_sender_mailaddr_key")) {
				checkMailFormat("pref_sender_mailaddr_key", sharedPreferences, getApplicationContext());
			}
			else if (key.equals("pref_sender_pwd_key")) {
				String pwd = sharedPreferences.getString("pref_sender_pwd_key", "").trim();
				if (pwd.length() == 0) {
					SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_pwd));
				}
			}
			else if (key.equals("pref_receiver_mailaddr_key")) {
				checkMailFormat("pref_receiver_mailaddr_key", sharedPreferences, getApplicationContext());
			}
			
			setResult();
			
			// Update preference summary fields
			for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
	            initSummary(getPreferenceScreen().getPreference(i));
	        }
			((PreferenceCategory)getPreferenceScreen().getPreference(1)).getPreference(1).setSummary(""); // Mock password
		}
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
		
		// Init preference summary fields
		int prefCount = this.getPreferenceScreen().getPreferenceCount();
		for(int i = 0; i < prefCount; i++){
            initSummary(this.getPreferenceScreen().getPreference(i));
        }
		((PreferenceCategory)getPreferenceScreen().getPreference(1)).getPreference(1).setSummary(""); // Mock password
		
		// Register	preference change listener
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(listener);
	}
	
	private void setResult() 
	{
		Intent data = this.getIntent();
		data.putExtra(SETTINGS, true);
		setResult(RESULT_OK, data);
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
	
	private boolean checkMailFormat(String key, SharedPreferences sharedPreferences, Context context) {
		String mail = sharedPreferences.getString(key, "").trim();
		Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
	    Matcher matcher = p.matcher(mail);
		if (!matcher.matches()) {
			String msg = String.format(context.getResources().getString(R.string.pref_not_valid_mail), mail);
			SysUtils.messageBox(context, msg);
			return false;
		}
		return true;
	}
	
	public static int getKeyCount(Context context) {
		int keyCount = DEFAULT_KEYS_NUMBER;
		try {
			keyCount = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_key_count_key", String.valueOf(DEFAULT_KEYS_NUMBER)));
		} catch (NumberFormatException e) {
			SysUtils.messageBox(context, context.getResources().getString(R.string.pref_wrong_count_format));
		}
		return keyCount;
	}
	
	public static void setKeyCount(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_key_count_key", String.valueOf(value)).commit();
	}
	
	public static String getSenderMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sender_mailaddr_key", "").trim();
	}
	
	public static void setSenderMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sender_mailaddr_key", value.trim()).commit();
	}
	
	public static String getSenderPwd(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sender_pwd_key", "").trim();
	}
	
	public static void setSenderPwd(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sender_pwd_key", value.trim()).commit();
	}
	
	public static String getReceiverMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_receiver_mailaddr_key", "").trim();
	}
	
	public static void setReceiverMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_receiver_mailaddr_key", value.trim()).commit();
	}
	
}