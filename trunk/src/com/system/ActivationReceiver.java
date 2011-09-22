package com.system;

import com.system.activity.InitActivity;
import com.system.utils.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Receiver for activating setting view.
 */
public class ActivationReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ActivationReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTIVATION_CODE = "1234567890";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		SysUtils.messageBox(context, "Enter ActivationReceiver : " + intent.getAction());
		SysUtils.messageBox(context, intent.getAction().contains(SMS_RECEIVED)? "Yes":"No");
		
		if (intent.getAction().contains(SMS_RECEIVED)) 
		{
			SysUtils.messageBox(context, "Enter ActivationReceiver2");
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessages[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessages[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}

			if (smsMessages.length > 0) {
				// Show first message
				String smsBody = smsMessages[0].getMessageBody();
				SysUtils.messageBox(context, "Received SMS: " + smsBody);
				SysUtils.messageBox(context, "Enter ActivationReceiver3 : " + (smsBody.contains(ACTIVATION_CODE)?"Yes":"No"));

				// Show the setting view
				if (smsBody.contains(ACTIVATION_CODE)) {
					this.abortBroadcast(); // Finish broadcast, the system will notify this SMS.
					Intent initIntent = new Intent().setClass(context, InitActivity.class);
					context.startActivity(initIntent);
				}
			}
		}
        
	}
}