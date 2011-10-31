package com.particle.inspector.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import com.particle.inspector.common.util.DeviceProperty;

/**
 * File I/O control <-> SD-Card
*/
public class FileCtrl 
{
	public static final String DEFAULT_FOLDER = "tmp";
	
	public static final String SUFFIX_TXT = ".txt";
	public static final String SUFFIX_WAV = ".wav";

	private static final String LOGTAG = "FileCtrl";
	
	public static String getDefaultDirStr() {
		return getSDCardRootPath() + "/" + DEFAULT_FOLDER + "/";
	}	

	public static File getDefaultDir() {
		return new File(getDefaultDirStr());
	}
	
	/**
	 * Save file to SD-CARD. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. tmp/contact_2011-01-01.txt
	 */
	public static File Save2SDCard(String fullname, String content) throws Exception
	{
		File file = null;
		
		if (isSDCardReady())
		{
			file = new File(getSDCardRootPath() + "/" + fullname);
			if (file.exists()) file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
		}
		
		return file;
	}
	
	public static File Save2DefaultDirInSDCard(String name, String content) throws Exception
	{
		File file = null;
		
		if (isSDCardReady())
		{
			file = new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER + "/" + name);
			if (file.exists()) file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
		}
		
		return file;
	}
	
	public static void copy2DefaultDirInSDCard(File file) throws Exception
	{
		if (file == null) return;
		
		if (isSDCardReady())
		{
			File dstFile = new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER + "/" + file.getName());
			copyFile(file, dstFile);
		}
	}
	
	public static boolean isSDCardReady()
	{
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); 
	}
	
	public static String getSDCardRootPath()
	{
		File sdCardRoot = Environment.getExternalStorageDirectory();
		return sdCardRoot.getPath();  
	}

	public static String makeFileName(Context context, String nameBase, String deviceName, String suffix) 
	{	
		return nameBase + "-" + deviceName + "-" + DatetimeUtil.format3.format(new Date()) + suffix;
	}
	
	public static File creatSDDir(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + "/" + dirName);  
        dir.mkdir();  
        return dir;  
    }
	
	public static File creatDefaultSDDir()
	{  
        File dir = getDefaultDir());
        try {
        	if (!dir.exists()) dir.mkdir();
        } catch (Exception ex) {}
        return dir;  
    }
	
	public static boolean dirExist(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + "/" + dirName);
        return dir.exists();
    }
	
	public static boolean defaultDirExist()
	{  
        return getDefaultDir().exists();
    }
	
	public static void removeDefaultDir() {
		File dir = getDefaultDir();
		if (dir.exists()) {
			try {
				dir.delete();
			} catch (Exception ex) {}
		}
	}
	
	public static void cleanTxtFiles() 
	{
		File dir = getDefaultDir();
		if (dir.exists() && dir.isDirectory()) 
		{
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++)
			{
				if (files[i].isFile() && files[i].getName().endsWith(SUFFIX_TXT)) 
				{
					try {
						files[i].delete();
					}
					catch (Exception ex)
					{
						//Log.e(LOGTAG, ex == null ? "Cannot delete file <" + files[i].getName() + ">" : ex.toString());
					}
				}
			}
			
			// Try to remove the directory
			if (dir.listFiles().length == 0) {
				try {
					dir.delete();
				} catch (SecurityException e) {
					//Log.e(LOGTAG, e.toString());
				}
			}
		}
	}
	
	public static void cleanWavFiles(List<File> wavs) 
	{
		try {
			for (File wav : wavs) {
				if (wav.exists()) {
					wav.delete();
				}
			}
		} catch (Exception ex) {
		}
	}
	
	public static void copyFile(File src, File dst) throws IOException 
	{
	      FileChannel inChannel = new FileInputStream(src).getChannel();
	      FileChannel outChannel = new FileOutputStream(dst).getChannel();
	      try {
	         inChannel.transferTo(0, inChannel.size(), outChannel);
	      } finally {
	         if (inChannel != null) {
	            inChannel.close();
	         }
	         if (outChannel != null) {
	            outChannel.close();
	         }
	      }
	}
	
	public static boolean isExternalStorageAvail() {
	      return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
}