package com.system.utils.license;

import com.system.utils.AesCryptor;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	
	public static LicenseType isLicensed(Context context, String user, String key)
	{
		// NOTICE: the key is only 4length
		try {
			String key4 = AesCryptor.encrypt(AesCryptor.defaultSeed, user).substring(0,4);
			if (key4.compareToIgnoreCase(key) == 0) {
				return LicenseType.FullLicensed;
			}
			else {
				String smsKey4 = AesCryptor.encrypt(AesCryptor.smsSeed, user).substring(0,4);
				if (smsKey4.compareToIgnoreCase(key) == 0) {
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
