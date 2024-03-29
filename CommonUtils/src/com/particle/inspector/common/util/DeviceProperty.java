package com.particle.inspector.common.util;

import java.lang.reflect.Method;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

public class DeviceProperty 
{
	private static final String LOGTAG = "DeviceProperty";

	// Get device model, e.g. "HTC Wildfire", "GT-S5570"
	public synchronized static String getDeviceModel()
	{
		return Build.MODEL;
	}
	
	// Return: CN, JP, EN 
	public synchronized static LANG getPhoneLang() 
	{
		String lang = Locale.getDefault().getLanguage();
		if (lang.contains("zh")) return LANG.CN;
		else if (lang.contains("jp")) return LANG.JP;
		else return LANG.EN;
	}
	
	// Get name of product manufacturer
	public synchronized static String getManufacturer()
	{
		return Build.MANUFACTURER;
	}
	
	// Get phone number
	// Notice: not all SIM contains phone card.
	public synchronized static String getPhoneNumber(Context context)
	{
		try {
			return ((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
		} catch (Exception e) {
			//Log.e(LOGTAG, "Failed to get phone number");
			return "";
		}
	}
	
	// Get screen resolution
	public synchronized static Display getScreenResolution(Activity activity)
	{
		try {
			return ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		} catch (Exception e) {
			//Log.e(LOGTAG, e.getMessage());
			return null;
		}
	}
	
	// Get Android OS version
	public synchronized static String getAndroidVersion()
	{
		 switch (Build.VERSION.SDK_INT)
		 {
		 case Build.VERSION_CODES.BASE:
			 return "1.0";
		 case Build.VERSION_CODES.BASE_1_1:
			 return "1.1";
		 case Build.VERSION_CODES.CUPCAKE:
		 case Build.VERSION_CODES.CUR_DEVELOPMENT://Magic version number for a current development build, which has not yet turned into an official release.
			 return "1.5";
		 case Build.VERSION_CODES.DONUT:
			 return "1.6";
		 case Build.VERSION_CODES.ECLAIR:
			 return "2.0";
		 case Build.VERSION_CODES.ECLAIR_0_1:
			 return "2.0.1";
		 case Build.VERSION_CODES.ECLAIR_MR1:
			 return "2.1.x";
		 case Build.VERSION_CODES.FROYO:
			 return "2.2.x";
		 case Build.VERSION_CODES.GINGERBREAD: 
			 return "2.3.x"; // 2.3, 2.3.1, 2.3.2
		 case Build.VERSION_CODES.GINGERBREAD_MR1: 
			 return "2.3.y"; // 2.3.3, 2.3.4
		 case 11: // Build.VERSION_CODES.HONEYCOMB
			 return "3.0.x";
		 case 12: // Build.VERSION_CODES.HONEYCOMB_MR1
			 return "3.1.x";
		 case 13: // Build.VERSION_CODES.HONEYCOMB_MR2
			 return "3.2";
		 case 14: // Build.VERSION_CODES.ICE_CREAM_SANDWICH
			 return "4.0";
		 case 15: // Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
			 return "4.0.3";
		 case 16: // Build.VERSION_CODES.JELLY_BEAN
			 return "4.1";
		 default:return "4.1+";
		 }
	}
	
	// Whether it is up Android 2.2
	public synchronized static boolean verLargerThan22() {
		return (Build.VERSION.SDK_INT > 8);
	}
	
	// Whether it is up Android 2.3.3
	public synchronized static boolean verLargerThan233() {
		return (Build.VERSION.SDK_INT > 10);
	}
	
	// Whether it is equal or up to Android 4.0
	public synchronized static boolean verEqualOrLargerThan40()	{
		return (Build.VERSION.SDK_INT >= 14);
	}
	
	// protected static final String PREFS_FILE = "device_id.xml";
	// protected static final String PREFS_DEVICE_ID = "device_id";

	/**
     * Returns a unique UUID for the current android device. As with all UUIDs, this unique ID is "very highly likely"
     * to be unique across all Android devices. Much more so than ANDROID_ID is.
     *
     * The UUID is generated by using ANDROID_ID as the base key if appropriate, falling back on
     * TelephonyManager.getDeviceID() if ANDROID_ID is known to be incorrect, and finally falling back
     * on a random UUID that's persisted to SharedPreferences if getDeviceID() does not return a
     * usable value.
     *
     * In some rare circumstances, this ID may change. In particular, if the device is factory reset a new device ID
     * may be generated. In addition, if a user upgrades their phone from certain buggy implementations of Android 2.2
     * to a newer, non-buggy version of Android, the device ID may change. Or, if a user uninstalls your app on
     * a device that has neither a proper Android ID nor a Device ID, this ID may change on reinstallation.
     *
     * Note that if the code falls back on using TelephonyManager.getDeviceId(), the resulting ID will NOT
     * change after a factory reset. Something to be aware of.
     *
     * Works around a bug in Android 2.2 for many devices when using ANDROID_ID directly.
     *
     * @see http://code.google.com/p/android/issues/detail?id=10603
     *
     * @return a UUID that may be used to uniquely identify your device for most purposes.
     */
	/*
	public synchronized static String getDeviceUuid(Context context) 
	{
		UUID uuid = null;
		SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
		String id = prefs.getString(PREFS_DEVICE_ID, null);

		if (id != null) {
			// Use the ids previously computed and stored in the prefs file
			uuid = UUID.fromString(id);
		} else {
			String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

			// Use the Android ID unless it's broken, in which case fallback on deviceId,
			// unless it's not available, then fallback on a random number which we store to a prefs file
			try {
				if (!"9774d56d682e549c".equals(androidId)) {
					uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
				} else {
					String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
					uuid = (deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID());
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(LOGTAG, "Failed to get device UUID");
			}

			// Write the value out to the prefs file
			prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
		}

		return (uuid == null ? "" : uuid.toString());
	}
	*/

	/** Return the MEID or IMEI of the device depending on which radio the phone uses (CDMA or GSM).
	 * It doesn't work on Android devices which aren't phones such as tablets, 
	 * it requires the READ_PHONE_STATE permission and it doesn't work reliably on all phones.
	 * 
	 * @see http://baike.baidu.com/view/2823315.htm (MEID definition)
	 * @see http://baike.baidu.com/view/90099.htm   (IMEI definition)
	*/ 
	public synchronized static String getDeviceId(Context context) 
	{
		String deviceId = "";
		try {
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			deviceId = tManager.getDeviceId();
		} catch (Exception e){
			//Log.e(LOGTAG, e.toString());
		}
		return deviceId.trim().toUpperCase();
	}

	// Return device serial number
	public static String getSerialNum() 
	{
		String serialNumber = "";
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);
			serialNumber = (String) get.invoke(c, "ro.serialno");
		} catch (Exception ignored) {
			//Log.e(LOGTAG, "Failed to get device serial number");
		}

		return serialNumber.trim().toUpperCase();
	}
	
	// 0: TelephonyManager.PHONE_TYPE_NONE
	// 1: TelephonyManager.PHONE_TYPE_GSM
	// 2: TelephonyManager.PHONE_TYPE_CDMA
	public static int getPhoneType(Context context) 
	{
		int phoneType = TelephonyManager.PHONE_TYPE_NONE;
		try {
			TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			phoneType = tManager.getPhoneType();
		} catch (Exception e){
			//Log.e(LOGTAG, e.toString());
		}
		return phoneType;
	}

}
