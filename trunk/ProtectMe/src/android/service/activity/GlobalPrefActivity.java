package android.service.activity;  
  
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
import android.service.R;
import android.service.config.ConfigCtrl;
import android.util.Log;


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
		
		// Set state of sender mail&password
		setSenderMailState(sp, this);
				
		// Set state of location indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(6)).getPreference(0).setEnabled(false);
		
		// Set state of ring indication to be uneditable
		((PreferenceCategory)this.getPreferenceScreen().getPreference(7)).getPreference(0).setEnabled(false);
		
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
					if(!StrUtils.validateMailAddress(senderMail) || !senderMail.toLowerCase().endsWith("gmail.com")) {
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
				else if (key.equals("pref_safe_pwd")) {
					checkRecvMailFormat(sharedPreferences, context);
					enableReceiverInfoChgFlag();
				}
				else if (key.equals("pref_safe_mail")) {
					checkRecvMailFormat(sharedPreferences, context);
					enableReceiverInfoChgFlag();
				}
				else if (key.equals("pref_safe_phonenum")) {
					String oriPhoneNum = sharedPreferences.getString("pref_safe_phonenum", "");
					String phoneNum = oriPhoneNum.trim();
					Pattern p = Pattern.compile(RegExpUtil.VALID_PHONE_NUM);
					Matcher matcher = p.matcher(phoneNum);
					if (!matcher.matches()) {
						String title = getResources().getString(R.string.error);
						String msg   = String.format(getResources().getString(R.string.pref_pls_input_valid_recv_phonenum), phoneNum);
						SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
					}
					else enableReceiverInfoChgFlag();
				}
				
				// Update preference summary fields
				for(int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
		            initSummary(getPreferenceScreen().getPreference(i));
		        }
				
				// Show special summary
				setSpecialSummary(false);
			}			
        };
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
		String oriMail = sharedPreferences.getString("pref_safe_mail", "");
		String mail = oriMail.trim();
				
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
				String title = getResources().getString(R.string.error);
				String msg   = context.getResources().getString(R.string.pref_must_use_self_sender);
				SysUtils.errorDlg(GlobalPrefActivity.this, title, msg);
				CheckBoxPreference mCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference("pref_use_self_sender");
				if (mCheckBoxPreference != null) {
					mCheckBoxPreference.setChecked(true);
			    }
			}
		}
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
	
	public static String getSafePwd(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_safe_pwd", "").trim();
	}
	
	public static void setSafePwd(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_safe_pwd", value).commit();
	}
	
	public static String getSafeMail(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_safe_mail", "").trim();
	}
	
	public static void setSafeMail(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_safe_mail", value).commit();
	}
	
	public static String getSafePhoneNum(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_safe_phonenum", "").trim();
	}
	
	public static void setSafePhoneNum(Context context, String value) {
		PreferenceManager.getDefaultSharedPreferences(context).edit().putString("pref_safe_phonenum", value).commit();
	}
	
}