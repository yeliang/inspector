package com.particle.inspector.authsrv;

import java.util.Date;

import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.AUTH_SMS_RESULT;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import com.particle.inspector.authsrv.config.ConfigCtrl;
import com.particle.inspector.authsrv.sms.SmsCtrl;
import com.particle.inspector.authsrv.sqlite.DbHelper;
import com.particle.inspector.authsrv.sqlite.KEY_VALIDATION_RESULT;
import com.particle.inspector.authsrv.sqlite.metadata.TKey;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.SysUtils;

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
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		android.os.Debug.waitForDebugger();//TODO should be removed in the release
		if (!intent.getAction().equals(SMS_RECEIVED)) return;
		
		String smsBody = SmsCtrl.getSmsBody(intent).trim();
		SysUtils.messageBox(context, "Received SMS: " + smsBody);
		
		// If it is the key validation request SMS
		if (smsBody.startsWith(AuthSms.SMS_HEADER + AuthSms.SMS_SEPARATOR))
		{
			String parts[] = smsBody.split(AuthSms.SMS_SEPARATOR);
			if (parts.length < 3) return; 
			
			String smsAddress = SmsCtrl.getSmsAddress(intent);
			SysUtils.messageBox(context, "Phone number: " + smsAddress);
			AuthSms sms = new AuthSms(smsBody, AUTH_SMS_TYPE.CLIENT);
				
			DbHelper dbHelper = new DbHelper(context);
			boolean ret = dbHelper.createOrOpenDatabase();
			if (!ret) {
				Log.e(LOGTAG, "Failed to create or open database");
				return;
			}
				
			KEY_VALIDATION_RESULT valid = dbHelper.isValidLicenseKey(sms.getKey(), sms.getDeviceID());
			if (valid != KEY_VALIDATION_RESULT.INVALID) {
				SysUtils.messageBox(context, sms.getKey() + " is a valid key: " + valid);
				// Send back success SMS
				AuthSms replySms = new AuthSms(sms.getKey(), AUTH_SMS_RESULT.OK, null);
				String reply = replySms.serverSms2Str();
				boolean sentRet = SmsCtrl.sendSms(smsAddress, reply);
				if (sentRet) {
					if (valid == KEY_VALIDATION_RESULT.VALID_AND_NOT_EXIST) {
						// Insert to database
						String phoneNum =  sms.getPhoneNum().length() > 0 ? sms.getPhoneNum() : SmsCtrl.getSmsAddress(intent);
						TKey key = new TKey(sms.getKey(), sms.getDeviceID(), phoneNum,
							sms.getPhoneModel(), sms.getAndroidVer(), 
							DatetimeUtil.format.format(new Date()), 
							DatetimeUtil.format.format(new Date()));
						dbHelper.insert(key);
					} else if (valid == KEY_VALIDATION_RESULT.VALID_BUT_EXIST) {
						// Insert to database
						TKey key = new TKey(sms.getKey(), sms.getDeviceID(), sms.getPhoneNum(),
							sms.getPhoneModel(), sms.getAndroidVer(), 
							null, DatetimeUtil.format.format(new Date()));
						dbHelper.updateByKey(key);
					}
				}
			} else {
				SysUtils.messageBox(context, sms.getKey() + " is not valid key");
				// Send back failure SMS
				String msg = dbHelper.getDefaultValidateFailMsg(sms.getKey(), sms.getLang());
				AuthSms replySms = new AuthSms(sms.getKey(), AUTH_SMS_RESULT.NG, msg);
				String reply = replySms.serverSms2Str();
				SmsCtrl.sendSms(smsAddress, reply);
			}
		}
		
		// If it is receiver info SMS
		else if (smsBody.startsWith("Info|")) {
			// The sms format: <header>|<license key>|<receiver mail>|<receiver phone num>|<receiver sensitive words>|<gps activation word>
			String parts[] = smsBody.split("|");
			if (parts.length < 6) return;
			
			DbHelper db = new DbHelper(context);
			db.updateByKeyToWriteReceiverInfo(parts[1], parts[2], parts[3], parts[4], parts[5]);
		}
        
	}

}