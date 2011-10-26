package com.particle.inspector.common.util;

import java.io.DataOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class SysUtils
{
	private static final String LOGTAG = "SysUtils";
	public static final String NEWLINE = "\r\n";
	
	// Pop up a simple message box for seconds
	public static void messageBox(Context context, String msg)
	{
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
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
            Log.d(LOGTAG, "Unexpected error: " + e.getMessage());
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
            	Log.d(LOGTAG, "Unexpected error: " + e.getMessage());
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
	
	
}