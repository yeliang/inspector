package com.particle.inspector.common.util.license;

import com.particle.inspector.common.util.AesCryptor;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	private final static String STR_FULL_LICENSED = "full_licensed";
	private final static String STR_PART_LICENSED = "part_licensed";
	private final static String STR_NOT_LICENSED = "not_licensed";
	public static final int ACTIVATION_KEY_LENGTH = 12;
	
	public static LicenseType isLicensed(Context context, String key)
	{
		int keyLen = key.length();
		String clearText = key.substring(0, keyLen/2).toUpperCase();
		String crypText  = key.substring(keyLen/2, keyLen).toUpperCase();
		
		try {
			String encryped = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
			String fullKey = encryped.substring(0, ACTIVATION_KEY_LENGTH/2);
			if (fullKey.compareToIgnoreCase(crypText) == 0) {
				return LicenseType.FullLicensed;
			}
			else {
				String partKey = encryped.substring(ACTIVATION_KEY_LENGTH/2, ACTIVATION_KEY_LENGTH);
				if (partKey.compareToIgnoreCase(crypText) == 0) {
					return LicenseType.PartLicensed;
				}
				return LicenseType.NotLicensed;
			}
		} catch (Exception ex)
		{
			Log.e(LOGTAG, ex.getMessage());
			return LicenseType.NotLicensed;
		}
	}
	
	public static LicenseType strToEnum(String typeStr)
	{
		if (typeStr == STR_FULL_LICENSED) return LicenseType.FullLicensed;
		else if (typeStr == STR_PART_LICENSED) return LicenseType.PartLicensed;
		else return LicenseType.NotLicensed;
	}
	
	public static String enumToStr(LicenseType type)
	{
		if (type == LicenseType.FullLicensed) return STR_FULL_LICENSED;
		else if (type == LicenseType.PartLicensed) return STR_PART_LICENSED;
		else return STR_NOT_LICENSED;
	}
}
