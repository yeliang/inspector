package com.particle.inspector.common.util;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

public class PowerUtil 
{
	private static String LOGTAG = "PowerUtil";
	
	public static boolean isScreenOn(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	public static void setScreenOff(Context context) {
		try {
			Intent intent = new Intent().setClass(context, DummyActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startActivity(intent);
		} catch (Exception ex) {
			//Log.e(LOGTAG, ex.getMessage());
		}
	}
	
}
