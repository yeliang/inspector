package com.system.utils.license;

import com.system.utils.AesCryptor;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	
	public static LicenseType isLicensed(Context context, String user, String key)
	{
		// NOTICE: the key is only 6 digital
		try {
			String encryped = AesCryptor.encrypt(AesCryptor.defaultSeed, user);
			String key6 = encryped.substring(0,6);
			if (key6.compareToIgnoreCase(key) == 0) {
				return LicenseType.FullLicensed;
			}
			else {
				String smsKey6 = encryped.substring(6,12);
				if (smsKey6.compareToIgnoreCase(key) == 0) {
					return LicenseType.OnlySmsLicensed;
				}
				return LicenseType.NotLicensed;
			}
		} catch (Exception ex)
		{
			Log.e(LOGTAG, ex.getMessage());
			return LicenseType.NotLicensed;
		}
	}
}
