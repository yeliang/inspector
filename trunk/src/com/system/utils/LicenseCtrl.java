package com.system.utils;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	
	public static boolean isLicensed(Context context, String user, String key)
	{
		// NOTICE: the key is only 4length
		try {
			String key4 = AesCryptor.encrypt(AesCryptor.defaultSeed, user).substring(0,4);
			if (key4.compareToIgnoreCase(key) == 0) {
				return true;
			}
			else {
				return false;
			}
		} catch (Exception ex)
		{
			Log.e(LOGTAG, ex.getMessage());
			return false;
		}
	}
}
