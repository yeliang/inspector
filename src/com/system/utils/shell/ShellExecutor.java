package com.system.utils.shell;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class ShellExecutor 
{
	private static final String LOGTAG = "ShellExecutor";
	
	public static boolean checkSuAvailable()
	{
		String suPath = "/system/bin/su";
		if ((new File(suPath)).exists()) {
			return true;
		} else {
			suPath = "/system/xbin/su";
			if((new File(suPath)).exists()) {
				return true;
			} else {
				suPath = "/data/bin/su";
				if((new File(suPath)).exists()) {
					return true;
				} else return false;
			}
		}
	}
	
	public static boolean executeCommandWithRoot(String cmd)
	{
		try {
			Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + " \n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            if (process.exitValue() != 255) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
            return false;
        }
	}
	
}
