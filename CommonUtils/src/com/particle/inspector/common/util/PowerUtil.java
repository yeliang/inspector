package com.particle.inspector.common.util;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class PowerUtil 
{
	public static boolean isScreenOn(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	public static void setScreenOff(Context context) {

	}
	
}
