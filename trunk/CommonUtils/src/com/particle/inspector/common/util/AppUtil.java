package com.particle.inspector.common.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtil 
{
	public static String getAppVerName(Context context) {
		String ver = "";
		try {
			ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			ver = ""; 
		}
		return ver;
	}
	
	public static int getAppVerCode(Context context) {
		int ver = 0;
		try {
			ver = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			ver = 0; 
		}
		return ver;
	}
	
}
