package system.service;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Receiver that will handle the boot completed intent and send the intent to launch the BootService.
 */
public class BootReceiver extends BroadcastReceiver {
	
	private TelephonyManager mTelephonyMgr;
	
	@Override
	public void onReceive(Context context, Intent bootintent) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (bootintent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
		{
			mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			
			// Start service
			Intent mServiceIntent = new Intent(context, BootService.class);
			context.startService(mServiceIntent);
		}
		
		mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTelephonyMgr != null && mTelephonyMgr.getSimState() == TelephonyManager.SIM_STATE_READY) 
        {
            String simSerialNumber = mTelephonyMgr.getSimSerialNumber();
            
            boolean isFirstRun = ConfigCtrl.getSimFirstRun(context);
            if (isFirstRun) {
            	ConfigCtrl.setSimFirstRun(context, false);
                ConfigCtrl.setSimSerialNum(context, simSerialNumber);
            } else {
                String hostSimSerialNumber = ConfigCtrl.getSimSerialNum(context);
                if (!simSerialNumber.equalsIgnoreCase(hostSimSerialNumber)) 
                {
                    // Send SMS since the SIM card has changed
                	String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
                	if (recvPhoneNum != null && recvPhoneNum.length() > 0) 
                	{
                		String strContent = String.format(context.getResources().getString(R.string.msg_changed_sim), ConfigCtrl.getSelfName(context));
                		SmsCtrl.sendSms(recvPhoneNum, strContent);
                	}
                }
            }
        }
		
	}
}