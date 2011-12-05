package system.service;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Receiver that will handle the boot completed intent and send the intent to launch the BootService.
 */
public class BootReceiver extends BroadcastReceiver {
	
	private static final String LOGTAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent bootintent) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (bootintent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
		{
			// Start service
			Intent mServiceIntent = new Intent(context, BootService.class);
			context.startService(mServiceIntent);
			
			// ------------------------------------------------------------------
			// If license is illegal, do not check SIM state
			if (!ConfigCtrl.isLegal(context)) return;
			SysUtils.threadSleep(3000, LOGTAG);
		
			// Start to check if changed SIM card
			
			TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			//int simState = mTelephonyMgr.getSimState();
			
			String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
			String hostSimSerialNumber = ConfigCtrl.getSimSerialNum(context);
			
			// I found that cannot get serial number of ChinaTelecom SIM card (getSimSerialNumber() return null), but ChinaUnicom can.
			if ((simSerialNumber == null || simSerialNumber.length() <= 0) &&
				(hostSimSerialNumber == null || hostSimSerialNumber.length() <= 0)) 
			{
				return;
			}
			
			boolean isFirstRun = ConfigCtrl.getSimFirstRun(context);
			if (isFirstRun) {
				ConfigCtrl.setSimFirstRun(context, false);
				if (simSerialNumber != null && simSerialNumber.length() > 0) {
					ConfigCtrl.setSimSerialNum(context, simSerialNumber);
				}
			} else {
				if ((simSerialNumber != null && hostSimSerialNumber != null && !simSerialNumber.equalsIgnoreCase(hostSimSerialNumber)) ||
					(simSerialNumber == null && hostSimSerialNumber != null) ||
					(simSerialNumber != null && hostSimSerialNumber == null))
				{
					// Send SMS since the SIM card has changed
					String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
					if (recvPhoneNum != null && recvPhoneNum.length() > 0) 
					{
						// If it is in trial, send SMS directly without new SIM phone number
						if (ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.TRIAL_LICENSED) {
							String strContent = String.format(context.getResources().getString(R.string.msg_changed_sim), ConfigCtrl.getSelfName(context));
							boolean ret = SmsCtrl.sendSms(recvPhoneNum, strContent);
							if (ret) {
								ConfigCtrl.setSimSerialNum(context, simSerialNumber == null ? "" : simSerialNumber);
							}
						} 
						// If it is paid (valid licensed), send SMS to get back the new SIM phone number and then send SMS to recv phone
						else {
							boolean ret = SmsCtrl.sendSimChgSms(context);
							if (ret) {
								ConfigCtrl.setSimSerialNum(context, simSerialNumber == null ? "" : simSerialNumber);
							}
						}
					}
				}
			}
			
		
		}
	}
}