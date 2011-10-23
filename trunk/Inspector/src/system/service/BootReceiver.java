package system.service;

import com.particle.inspector.common.util.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver that will handle the boot completed intent and send the intent to launch the BootService.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent bootintent) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		Intent mServiceIntent = new Intent(context, BootService.class);
		context.startService(mServiceIntent);
	}
}