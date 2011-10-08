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
	
	public static LICENSE_TYPE isLicensed(Context context, String key)
	{
		int keyLen = key.length();
		String clearText = key.substring(0, keyLen/2).toUpperCase();
		String crypText  = key.substring(keyLen/2, keyLen).toUpperCase();
		
		try {
			String encryped = AesCryptor.encrypt(AesCryptor.defaultSeed, clearText);
			String fullKey = encryped.substring(0, ACTIVATION_KEY_LENGTH/2);
			if (fullKey.compareToIgnoreCase(crypText) == 0) {
				return LICENSE_TYPE.FULL_LICENSED;
			}
			else {
				String partKey = encryped.substring(ACTIVATION_KEY_LENGTH/2, ACTIVATION_KEY_LENGTH);
				if (partKey.compareToIgnoreCase(crypText) == 0) {
					return LICENSE_TYPE.PART_LICENSED;
				}
				return LICENSE_TYPE.NOT_LICENSED;
			}
		} catch (Exception ex)
		{
			Log.e(LOGTAG, ex.getMessage());
			return LICENSE_TYPE.NOT_LICENSED;
		}
	}
	
	public static LICENSE_TYPE strToEnum(String typeStr)
	{
		if (typeStr == STR_FULL_LICENSED) return LICENSE_TYPE.FULL_LICENSED;
		else if (typeStr == STR_PART_LICENSED) return LICENSE_TYPE.PART_LICENSED;
		else return LICENSE_TYPE.NOT_LICENSED;
	}
	
	public static String enumToStr(LICENSE_TYPE type)
	{
		if (type == LICENSE_TYPE.FULL_LICENSED) return STR_FULL_LICENSED;
		else if (type == LICENSE_TYPE.PART_LICENSED) return STR_PART_LICENSED;
		else return STR_NOT_LICENSED;
	}
}
