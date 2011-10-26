package com.particle.inspector.common.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class NetworkUtil 
{
	private static final String LOGTAG = "NetworkUtil";
	
	// Get networks connection state
	public static boolean isNetworkConnected(Context context)
	{	
		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (mgr.getActiveNetworkInfo() != null)  {
			return mgr.getActiveNetworkInfo().isAvailable();
		} else return false;
        
		//return (isWifiConnected(context) || is3GConnected(context));
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
	
	// It may take 30s at most
	public static boolean tryToConnectDataNetwork(Context context) 
	{
		int MAX_TRY_COUNT = 6;
		int tryCount = 0;
		while (!isNetworkConnected(context) && tryCount < MAX_TRY_COUNT) {
			enableWifi(context, true);
			enable3GDataConnection(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
		
		return isNetworkConnected(context);
	} 
	
	// It may take 30s at most
	public static boolean tryToDisconnectDataNetwork(Context context) 
	{
		int MAX_TRY_COUNT = 6;
		int tryCount = 0;
		while (isNetworkConnected(context) && tryCount < MAX_TRY_COUNT) {
			enableWifi(context, false);
			disable3GDataConnection(context);
			tryCount++;
			SysUtils.threadSleep(5000, LOGTAG);
		}
		
		return !isNetworkConnected(context);
	}
	
	public static boolean enableWifi(Context context, boolean state) {
		try {
	       	WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	       	wifiManager.setWifiEnabled(state);
	       	return true;
	       }
	       catch (Exception ex) {
	       	return false;
	       }
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public static void enable3GDataConnection(Context context)
	{	
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			// If data connected, return
			if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
				return;
			}
		
			Class telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
			Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
		
			Class ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
			Method dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("enableDataConnectivity");
			dataConnSwitchMethod.setAccessible(true);
			boolean ret = (Boolean)dataConnSwitchMethod.invoke(ITelephonyStub);
		} catch (Exception ex) {}
	}
	
	@SuppressWarnings("rawtypes")
	public static void disable3GDataConnection(Context context)
	{	
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			// If data disconnected, return
			if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
				return;
			}
		
			Class telephonyManagerClass = Class.forName(telephonyManager.getClass().getName());
			Method getITelephonyMethod = telephonyManagerClass.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			Object ITelephonyStub = getITelephonyMethod.invoke(telephonyManager);
		
			Class ITelephonyClass = Class.forName(ITelephonyStub.getClass().getName());
			Method dataConnSwitchMethod = ITelephonyClass.getDeclaredMethod("disableDataConnectivity");
			dataConnSwitchMethod.setAccessible(true);
			dataConnSwitchMethod.invoke(ITelephonyStub);
		} catch (Exception ex) {}
	}
}
