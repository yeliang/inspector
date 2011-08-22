package com.system.utils;

import android.app.Activity;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	
	public static boolean isLicensed(Activity activity, String user, String key)
	{
		// NOTICE: the key is only 4length
		try {
			// Firstly read from SharedPreferences
			if (ConfigCtrl.getIsLicensed(activity)) return true;
			
			String key4 = AesCryptor.encrypt(AesCryptor.defaultSeed, user).substring(0,4);
			if (key4 == key) {
				// Write the license info to haredPreferences
				ConfigCtrl.setIsLicensed(activity, true);
				return true;
			}
			else {
				ConfigCtrl.setIsLicensed(activity, false);
				return false;
			}
		} catch (Exception ex)
		{
			Log.e(LOGTAG, ex.getMessage());
			ConfigCtrl.setIsLicensed(activity, false);
			return false;
		}
	}
}

