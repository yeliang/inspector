package system.service;

import com.particle.inspector.common.util.license.LICENSE_TYPE;

public class GlobalValues 
{
	// Initialize global values 
	public static LICENSE_TYPE licenseType = LICENSE_TYPE.NOT_LICENSED;
	public static String[] recipients = null;
	public static String[] sensitiveWordArray = null;
	
	// The state flag for env listening feature
	public static boolean IS_ENV_LISTENING = false;
	
	// The state flag for phone location feature
	public static boolean IS_GETTING_LOCATION = false;
}
