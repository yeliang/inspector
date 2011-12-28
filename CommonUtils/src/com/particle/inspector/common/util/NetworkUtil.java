package com.particle.inspector.common.util;

import java.lang.reflect.Method;

import com.particle.inspector.common.util.phone.PhoneUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetworkUtil 
{
	private static final String LOGTAG = "NetworkUtil";
	
	// Get network connection state 
	public static boolean isNetworkConnected(Context context) {
		try {
			ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetInfo = manager.getActiveNetworkInfo();
			return ((activeNetInfo != null) && activeNetInfo.isConnected());
		} catch (Exception ex) {
			return false;
		}
	}
	
	// Get mobile 3G Data Network connection state
	public static boolean is3GDataConnected(Context context)
	{
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);        
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        return (mobile == State.CONNECTED);
	}
	
	// Get Wifi connection state
	public static boolean isWifiConnected(Context context)
	{
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);        
		State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	    return (wifi == State.CONNECTED);
	}
	
	public static boolean tryToConnectDataNetwork(Context context) 
	{
		int MAX_TRY_COUNT = 3;
		int tryCount = 0;
		while (!isNetworkConnected(context) && tryCount < MAX_TRY_COUNT) {
			enableWifi(context);
			enable3GDataConnection(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
		
		return isNetworkConnected(context);
	} 
	
	public static boolean tryToDisconnectDataNetwork(Context context) 
	{
		int MAX_TRY_COUNT = 3;
		int tryCount = 0;
		while (isNetworkConnected(context) && tryCount < MAX_TRY_COUNT) {
			disableWifi(context);
			disable3GDataConnection(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
		
		return !isNetworkConnected(context);
	}
	
	public static boolean tryToConnectWifi(Context context) 
	{
		int MAX_TRY_COUNT = 3;
		int tryCount = 0;
		while (!isWifiConnected(context) && tryCount < MAX_TRY_COUNT) {
			enableWifi(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
		
		return isWifiConnected(context);
	} 
	
	public static boolean tryToDisconnectWifi(Context context) 
	{
		int MAX_TRY_COUNT = 3;
		int tryCount = 0;
		while (isWifiConnected(context) && tryCount < MAX_TRY_COUNT) {
			disableWifi(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
	
		return !isWifiConnected(context);
	}
	
	private static boolean setWifiState(Context context, boolean state) {
		try {
			WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			wifiManager.setWifiEnabled(state);
	       	return true;
		}
		catch (Exception ex) {
			return false;
		}
	}
	
	public static boolean enableWifi(Context context) {
		return setWifiState(context, true);
	}
	
	public static boolean disableWifi(Context context) {
		return setWifiState(context, false);
	}
	
	/**
	 * Android SDK is not allowed to access 3G data connection directly. So it is achieved through java reflection.
	 * Refer to http://stackoverflow.com/questions/8354530/how-to-enable-data-connection-on-android-2-3
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public static void enable3GDataConnection(Context context)
	{	
		try {
			if (DeviceProperty.verLargerThan22()) {
				ConnectivityManager mgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
				Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
				dataMtd.setAccessible(true);
				dataMtd.invoke(mgr, true); 
			}
			else {
				TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				// If data connected, return
				if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
					return;
				}
		
				PhoneUtils.getITelephony(telephonyManager).enableDataConnectivity();
			}
		} catch (Exception ex) {}
	}
	
	@SuppressWarnings("rawtypes")
	public static void disable3GDataConnection(Context context)
	{	
		try {
			if (DeviceProperty.verLargerThan22()) {
				ConnectivityManager mgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
				Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
				dataMtd.setAccessible(true);
				dataMtd.invoke(mgr, false); 
			}
			else {
				TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				// If data disconnected, return
				if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
					return;
				}
		
				PhoneUtils.getITelephony(telephonyManager).disableDataConnectivity();
			}
		} catch (Exception ex) {}
	}
	
	public static SIM_TYPE getNetworkType(Context context) 
	{
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = telephonyManager.getNetworkType();
		
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return SIM_TYPE.USIM; // WCDMA USIM
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return SIM_TYPE.SIM;  // GPRS SIM
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return SIM_TYPE.SIM;  // EDGE SIM
			default:
				return SIM_TYPE.UIM;  // CDMA UIM
		}
	}
	
}
