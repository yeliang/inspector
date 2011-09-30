package com.particle.inspector.authsrv;

import java.util.Date;

import com.particle.inspector.authsrv.sms.AuthSms;
import com.particle.inspector.authsrv.sms.SMS_RESULT;
import com.particle.inspector.authsrv.sms.SMS_TYPE;
import com.particle.inspector.authsrv.sms.SmsCtrl;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.util.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Receiver for activating setting view.
 */
public class ActivationReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ActivationReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final int ACTIVATION_KEY_LENGTH = 12;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		SysUtils.messageBox(context, "Enter ActivationReceiver : " + intent.getAction());
		
		if (intent.getAction().equals(SMS_RECEIVED)) 
		{	
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessages[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessages[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}

			if (smsMessages.length > 0) {
				String smsBody = smsMessages[0].getMessageBody().trim();
				SysUtils.messageBox(context, "Received SMS: " + smsBody);
				
				String parts[] = smsBody.split(AuthSms.SMS_SEPARATOR);
				if (parts.length >= 3 && parts[0].equalsIgnoreCase(AuthSms.SMS_HEADER)) 
				{
					String strMobile = smsMessages[0].getOriginatingAddress();
					SysUtils.messageBox(context, "Phone number: " + strMobile);
					AuthSms sms = new AuthSms(smsBody, SMS_TYPE.CLIENT);
					DbHelper dbHelper = new DbHelper(context); 
					if (dbHelper.isValidLicenseKey(sms.getKey())) {
						SysUtils.messageBox(context, sms.getKey() + " is valid key");
						// Send back success SMS
						AuthSms replySms = new AuthSms(sms.getKey(), SMS_RESULT.OK, null);
						String reply = replySms.serverSms2Str();
						SmsCtrl.sendSms(strMobile, reply);
					} else {
						SysUtils.messageBox(context, sms.getKey() + " is not valid key");
						// Send back failure SMS
						String msg = dbHelper.getDefaultValidateFailMsg(sms.getKey());
						AuthSms replySms = new AuthSms(sms.getKey(), SMS_RESULT.NG, msg);
						String reply = replySms.serverSms2Str();
						SmsCtrl.sendSms(strMobile, reply);
					}
				}
			}
		}
        
	}

}