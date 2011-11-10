package com.particle.inspector.common.util.license;

import com.particle.inspector.common.util.AesCryptor;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	private final static String STR_TRIAL_LICENSED = "trial";
	private final static String STR_FULL_LICENSED = "full";
	private final static String STR_PART_LICENSED = "part";
	private final static String STR_SUPER_LICENSED = "super";
	private final static String STR_NOT_LICENSED = "none";
	public static final int ACTIVATION_KEY_LENGTH = 12;
	
	public static String TRIAL_KEY = "###";
	
	public static LICENSE_TYPE calLicenseType(Context context, String key)
	{
		key = key.trim().toUpperCase();
		if (key.equals(TRIAL_KEY)) return LICENSE_TYPE.TRIAL_LICENSED;
		
		int keyLen = key.length();
		String clearText = key.substring(0, keyLen/2);
		String crypText  = key.substring(keyLen/2, keyLen);
		
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
				else {
					String superKey = encryped.substring(ACTIVATION_KEY_LENGTH, (int)(ACTIVATION_KEY_LENGTH*1.5));
					if (superKey.compareToIgnoreCase(crypText) == 0) {
						return LICENSE_TYPE.SUPER_LICENSED;
					}
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
		if (typeStr.equals(STR_TRIAL_LICENSED)) return LICENSE_TYPE.TRIAL_LICENSED;
		else if (typeStr.equals(STR_FULL_LICENSED)) return LICENSE_TYPE.FULL_LICENSED;
		else if (typeStr.equals(STR_PART_LICENSED)) return LICENSE_TYPE.PART_LICENSED;
		else if (typeStr.equals(STR_SUPER_LICENSED)) return LICENSE_TYPE.SUPER_LICENSED;
		else return LICENSE_TYPE.NOT_LICENSED;
	}
	
	public static String enumToStr(LICENSE_TYPE type)
	{
		if (type == LICENSE_TYPE.TRIAL_LICENSED) return STR_TRIAL_LICENSED;
		else if (type == LICENSE_TYPE.FULL_LICENSED) return STR_FULL_LICENSED;
		else if (type == LICENSE_TYPE.PART_LICENSED) return STR_PART_LICENSED;
		else if (type == LICENSE_TYPE.SUPER_LICENSED) return STR_SUPER_LICENSED;
		else return STR_NOT_LICENSED;
	}
}
