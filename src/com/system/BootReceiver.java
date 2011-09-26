package com.system;

import com.system.utils.SysUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver that will handle the boot completed intent and send the intent to launch the BootService.
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent bootintent) {
		SysUtils.messageBox(context, "Enter BootReceiver");
		Intent mServiceIntent = new Intent(context, BootService.class);
		context.startService(mServiceIntent);
	}
}