package com.particle.inspector.common.util.license;

import com.particle.inspector.common.util.AesCryptor;
import com.particle.inspector.common.util.DeviceProperty;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	private final static String STR_TRIAL_LICENSED = "trial";
	private final static String STR_FULL_LICENSED = "full";
	private final static String STR_NOT_LICENSED = "none";
	public static final int ACTIVATION_KEY_LENGTH = 12;
	
	public static String TRIAL_KEY = "###";
	
	private static AesCryptor cryptor = new AesCryptor();
	
	public static LICENSE_TYPE calLicenseType(Context context, String key)
	{
		if (key == null) return LICENSE_TYPE.NOT_LICENSED;
		else if (key.equals(TRIAL_KEY)) return LICENSE_TYPE.TRIAL_LICENSED;
		
		key = key.trim().toUpperCase();		
		String clearText = DeviceProperty.getDeviceId(context);
		
		try {
			String encryped = cryptor.encrypt(clearText);
			String fullKey = encryped.substring(0, ACTIVATION_KEY_LENGTH);
			if (fullKey.equalsIgnoreCase(key)) {
				return LICENSE_TYPE.FULL_LICENSED;
			}
			else {
				return LICENSE_TYPE.NOT_LICENSED;
			}
		} catch (Exception ex)
		{
			//Log.e(LOGTAG, ex.getMessage());
			return LICENSE_TYPE.NOT_LICENSED;
		}
	}
	
	public static LICENSE_TYPE strToEnum(String typeStr)
	{
		if (typeStr.equals(STR_TRIAL_LICENSED)) return LICENSE_TYPE.TRIAL_LICENSED;
		else if (typeStr.equals(STR_FULL_LICENSED)) return LICENSE_TYPE.FULL_LICENSED;
		else return LICENSE_TYPE.NOT_LICENSED;
	}
	
	public static String enumToStr(LICENSE_TYPE type)
	{
		if (type == LICENSE_TYPE.TRIAL_LICENSED) return STR_TRIAL_LICENSED;
		else if (type == LICENSE_TYPE.FULL_LICENSED) return STR_FULL_LICENSED;
		else return STR_NOT_LICENSED;
	}
}
