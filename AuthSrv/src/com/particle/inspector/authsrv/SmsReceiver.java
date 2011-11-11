package com.particle.inspector.authsrv;

import java.util.Date;

import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.AUTH_SMS_RESULT;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.SuperLoggingSms;
import com.particle.inspector.common.util.sms.TrialInfoSms;
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
		
		String smsBody = SmsCtrl.getSmsBody(intent).trim();
		
		// --------------------------------------------------------------------------------
		// If it is receiver info SMS (so the key type should be full or part)
		if (smsBody.startsWith(SmsConsts.HEADER_TRIAL_EX)) {
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 4) return; 
			
			TrialInfoSms sms = new TrialInfoSms(smsBody);
			
			DbHelper dbHelper = new DbHelper(context);
			boolean ret = dbHelper.createOrOpenDatabase();
			if (!ret) {
				Log.e(LOGTAG, "Failed to create or open database");
				return;
			}
			
			// Insert to database
			String phoneNum = sms.getPhoneNum().length() > 0 ? sms.getPhoneNum() : SmsCtrl.getSmsAddress(intent);
			TKey key = new TKey(sms.getKey(), LICENSE_TYPE.TRIAL_LICENSED, sms.getDeviceID(), phoneNum,
				sms.getPhoneModel(), sms.getAndroidVer(), 
				DatetimeUtil.format.format(new Date()));
			dbHelper.insertTrialInfo(key);
		}
		
		// --------------------------------------------------------------------------------
		// If it is the key validation request SMS (so the key type should be full or part)
		else if (smsBody.startsWith(SmsConsts.HEADER_AUTH_EX))
		{
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 4) return; 
			
			String smsAddress = SmsCtrl.getSmsAddress(intent);
			AuthSms sms = new AuthSms(smsBody, AUTH_SMS_TYPE.CLIENT);
				
			DbHelper dbHelper = new DbHelper(context);
			boolean ret = dbHelper.createOrOpenDatabase();
			if (!ret) {
				Log.e(LOGTAG, "Failed to create or open database");
				return;
			}
				
			KEY_VALIDATION_RESULT valid = dbHelper.isValidLicenseKey(sms.getKey(), sms.getDeviceID());
			String clientPhoneNum = SmsCtrl.getSmsAddress(intent);
			if (valid != KEY_VALIDATION_RESULT.INVALID) {
				// Send back success SMS
				AuthSms replySms = new AuthSms(sms.getKey(), clientPhoneNum, AUTH_SMS_RESULT.OK, null);
				String reply = replySms.serverSms2Str();
				boolean sentRet = SmsCtrl.sendSms(smsAddress, reply);
				if (sentRet) {
					LICENSE_TYPE keyType = LicenseCtrl.calLicenseType(context, sms.getKey());
					if (valid == KEY_VALIDATION_RESULT.VALID_AND_NOT_EXIST) {
						// Insert to database
						String phoneNum = sms.getPhoneNum().length() > 0 ? sms.getPhoneNum() : smsAddress;
						TKey key = new TKey(sms.getKey(), keyType, sms.getDeviceID(), phoneNum,
							sms.getPhoneModel(), sms.getAndroidVer(), 
							DatetimeUtil.format.format(new Date()));
						dbHelper.insert(key);
					} else if (valid == KEY_VALIDATION_RESULT.VALID_BUT_EXIST) {
						// Insert to database
						TKey key = new TKey(sms.getKey(), keyType, sms.getDeviceID(), sms.getPhoneNum(),
							sms.getPhoneModel(), sms.getAndroidVer(), 
							null);
						dbHelper.updateByKey(key);
					}
				}
			} else {
				// Send back failure SMS
				String msg = dbHelper.getDefaultValidateFailMsg(sms.getKey(), sms.getLang());
				AuthSms replySms = new AuthSms(sms.getKey(), clientPhoneNum, AUTH_SMS_RESULT.NG, msg);
				String reply = replySms.serverSms2Str();
				SmsCtrl.sendSms(smsAddress, reply);
			}
		}
		
		// --------------------------------------------------------------------------------
		// If it is receiver info SMS (so the key type should be full or part)
		else if (smsBody.startsWith(SmsConsts.HEADER_INFO_EX)) {
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			// The sms format: <header>,<device ID>,<receiver mail>,<receiver phone num>
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 4) {
				Log.e(LOGTAG, "Invalid info SMS: " + smsBody);
				SysUtils.messageBox(context, "Invalid info SMS: " + smsBody);
				return;
			}
			
			DbHelper db = new DbHelper(context);
			boolean ret = db.createOrOpenDatabase();
			if (ret) {
				String phoneNum = SmsCtrl.getSmsAddress(intent);
				ret = db.updateReceiverInfoByDeviceId(parts[1], parts[2], parts[3], phoneNum);
				if (!ret) {
					Log.e(LOGTAG, "Failed to update receiver info: " + smsBody);
					SysUtils.messageBox(context, "Failed to update receiver info: " + smsBody);
				}
			}
		}
		
		// --------------------------------------------------------------------------------
		// If it is unregister SMS (so the key type should be full or part)
		else if (smsBody.startsWith(SmsConsts.HEADER_UNREGISTER_EX)) {
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			// The sms format: <header>,<license key>,<device ID>
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 3) {
				Log.e(LOGTAG, "Invalid unregister SMS: " + smsBody);
				//SysUtils.messageBox(context, "Invalid unregister SMS: " + smsBody);
				return;
			}
			
			DbHelper db = new DbHelper(context);
			boolean ret = db.createOrOpenDatabase();
			if (ret) {
				String incomingPhoneNum = SmsCtrl.getSmsAddress(intent);// The client (controlled phone) number
				ret = db.unregister(parts[1], parts[2]);
				if (!ret) {
					Log.e(LOGTAG, "Failed to unregister: " + smsBody);
					//SysUtils.messageBox(context, "Failed to unregister: " + smsBody);
				}
				
				// Send response SMS to client
				boolean smsRet = SmsCtrl.sendUnregisterResponseSms(incomingPhoneNum, parts[1], ret);
			}
		}		
		
		// --------------------------------------------------------------------------------
		// If it is super key info logging SMS
		else if (smsBody.startsWith(SmsConsts.HEADER_SUPER_LOGGING_EX))
		{
			//abortBroadcast(); // Finish broadcast, the system will notify this SMS
			
			String parts[] = smsBody.split(SmsConsts.SEPARATOR);
			if (parts.length < 3) return;
			
			DbHelper dbHelper = new DbHelper(context);
			boolean ret = dbHelper.createOrOpenDatabase();
			if (!ret) {
				Log.e(LOGTAG, "Failed to create or open database");
				return;
			}
			
			SuperLoggingSms sms = new SuperLoggingSms(smsBody);
			String phoneNum =  sms.getPhoneNum().length() > 0 ? sms.getPhoneNum() : SmsCtrl.getSmsAddress(intent);
			LICENSE_TYPE keyType = LicenseCtrl.calLicenseType(context, sms.getKey());
			TKey key = new TKey(sms.getKey(), keyType, sms.getDeviceID(), phoneNum,
				sms.getPhoneModel(), sms.getAndroidVer(), 
				DatetimeUtil.format.format(new Date()));
			if (dbHelper.findDevice(sms.getDeviceID()) == null) {
				dbHelper.insert(key);
			} else {
				dbHelper.updateByDevice(key); // Update info for the same mobile phone
			}
			
		}
        
	} // end of onReceive

}