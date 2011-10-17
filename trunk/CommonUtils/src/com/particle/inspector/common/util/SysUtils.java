package com.particle.inspector.common.util;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SysUtils
{
	public static String TAG_ERR = "*** ERROR ***";
	public static String TAG_DEBUG = "*** DEBUG ***";
	
	public static String NEWLINE = "\r\n";
	
	// Pop up a simple message box for seconds
	public static void messageBox(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}	
	
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
	public static boolean is3GConnected(Context context)
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
	
	public static boolean isRooted(Context context) {
		return runRootCommand(context, "pwd");
	}
	
	public static boolean runRootCommand(Context context, String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Log.d(TAG_DEBUG, "Unexpected error: " + e.getMessage());
            SysUtils.messageBox(context, "Unexpected error: " + e.getMessage());
            return false;
        }
        finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            	Log.d(TAG_DEBUG, "Unexpected error: " + e.getMessage());
            }
        }
        return true;
    }
	
	public static void threadSleep(long time, String tag)
	{
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Log.e(tag, "Failed to sleep");
		}
	}
	
	public static String getUniformDatetimeStr(Date date) 
	{
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return f.format(date);
	}
}