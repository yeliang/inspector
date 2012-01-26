package com.particle.inspector.authsrv;

import java.util.Date;

import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.sms.AUTH_SMS_RESULT;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import com.particle.inspector.common.util.sms.CheckinSms;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.authsrv.sms.SmsCtrl;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.sqlite.KEY_VALIDATION_RESULT;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for activating setting view.
 */
public class SmsReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ActivationReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		if (!intent.getAction().equals(SMS_RECEIVED)) return;
		
		String incomingPhoneNum = SmsCtrl.getSmsAddress(intent);
		String smsBody = SmsCtrl.getSmsBody(intent).trim();
		
		// Calculate license key according to device ID (MEID/IMEI)
		if (smsBody.toUpperCase().startsWith(SmsConsts.HEADER_CAL_KEY)) {
			String deviceID = smsBody.substring(5).trim();
			if (deviceID.length() != LicenseCtrl.MEID_LENGTH && 
				deviceID.length() != LicenseCtrl.IMEI_LENGTH) 
			{
				String msg = String.format(context.getResources().getString(R.string.msg_invalid_deviceid), deviceID);
				SmsCtrl.sendSms(incomingPhoneNum, msg);
				return;
			}
			
			String key = LicenseCtrl.generateFullKey(context, deviceID.toUpperCase());
			String msg = String.format(context.getResources().getString(R.string.msg_the_calculated_key_is), deviceID) + key;
			SmsCtrl.sendSms(incomingPhoneNum, msg);
		}
		
		// --------------------------------------------------------------------------------
		// If it is CHECKIN SMS
		// * The Checkin SMS (client -> server) format: [Header],[Key],[Lang],[Device ID],[Phone Number],[Phone Model],[Android Version],[inspector ver code]
		// * Lang = CN|EN|JP
		// * e.g. Checkin,8B122A1DD9,CN,13B789A23CE9125,13980065966,HTC Desire,2.3,201201020
		else if (smsBody.startsWith(SmsConsts.HEADER_CHECKIN_EX))
		{
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 8) return;
			
			DbHelper dbHelper = new DbHelper(context);
			boolean ret = dbHelper.createOrOpenDatabase();
			if (!ret) {
				Log.e(LOGTAG, "Failed to create or open database");
				return;
			}
			
			CheckinSms sms = new CheckinSms(smsBody);
			String phoneNum =  sms.getPhoneNum().length() > 0 ? sms.getPhoneNum() : SmsCtrl.getSmsAddress(intent);
			
			// Do not insert record for testing phones
			if (phoneNum.contains("15100760464") || // Zhaowei's test phone
				phoneNum.contains("15319967068"))   // My test phone
			{
				return;
			}
			
			LICENSE_TYPE keyType = LicenseCtrl.calLicenseType(context, sms.getKey());
			TKey key = new TKey(sms.getKey(), keyType, sms.getDeviceID(), phoneNum,
				sms.getPhoneModel(), sms.getAndroidVer(), 
				DatetimeUtil.format.format(new Date()), sms.getVerCode());
			
			// Always insert new records even it is the same phone that 
			// the user re-install and re-register the phone.
			// Because we must keep all register SMS to distinguish the cheating case (rewrite MEID/IMEI)
			dbHelper.insert(key);
		}
        
	} // end of onReceive

}