package com.particle.inspector.common.util.license;

import com.particle.inspector.common.util.AesCryptor;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.sms.SmsConsts;

import android.content.Context;
import android.util.Log;

public class LicenseCtrl 
{
	private final static String LOGTAG = "LicenseCtrl";
	private final static String STR_TRIAL_LICENSED = "TRIAL";
	private final static String STR_FULL_LICENSED = "FULL";
	private final static String STR_NOT_LICENSED = "NONE";
	private final static String STR_ERROR = "error happened, cannot generate key";
	
	public static final int MEID_LENGTH = 14;
	public static final int IMEI_LENGTH = 15;
	public static final int ACTIVATION_KEY_LENGTH = 12;
	
	private static AesCryptor cryptor = new AesCryptor();
	
	// Calculate key according to MEID string
	public static String generateFullKey(Context context, String deviceId)
	{
		if (deviceId == null || deviceId.length() <= 0) return STR_ERROR;
		
		try {
			String encryped = cryptor.encrypt(deviceId.toUpperCase());
			String fullKey = encryped.substring(0, ACTIVATION_KEY_LENGTH);
			return fullKey.toUpperCase().trim();
		} 
		catch (Exception ex) {
			return STR_ERROR;
		}
	}
	
	public static LICENSE_TYPE calLicenseType(Context context, String key)
	{
		if (key == null || key.trim().length() <= 0) return LICENSE_TYPE.NOT_LICENSED;
		else if (key.equals(SmsConsts.TRIAL_KEY)       || 
				 key.equals(SmsConsts.TRIAL_KEY_ALIAS) || 
				 key.equals(SmsConsts.TRIAL_KEY_ALIAS2)) 
			return LICENSE_TYPE.TRIAL_LICENSED;
		
		key = key.trim().toUpperCase();		
		String meid = DeviceProperty.getDeviceId(context);
		
		try {
			String fullKey = generateFullKey(context, meid);
			if (fullKey != null && fullKey.equalsIgnoreCase(key)) {
				return LICENSE_TYPE.FULL_LICENSED;
			}
			else {
				return LICENSE_TYPE.NOT_LICENSED;
			}
		} catch (Exception ex) {
			return LICENSE_TYPE.NOT_LICENSED;
		}
	}
	
	public static LICENSE_TYPE strToEnum(String typeStr)
	{
		if (typeStr.equalsIgnoreCase(STR_TRIAL_LICENSED)) return LICENSE_TYPE.TRIAL_LICENSED;
		else if (typeStr.equalsIgnoreCase(STR_FULL_LICENSED)) return LICENSE_TYPE.FULL_LICENSED;
		else return LICENSE_TYPE.NOT_LICENSED;
	}
	
	public static String enumToStr(LICENSE_TYPE type)
	{
		if (type == LICENSE_TYPE.TRIAL_LICENSED) return STR_TRIAL_LICENSED;
		else if (type == LICENSE_TYPE.FULL_LICENSED) return STR_FULL_LICENSED;
		else return STR_NOT_LICENSED;
	}
}
