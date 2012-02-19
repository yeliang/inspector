package com.particle.inspector.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.os.Environment;

public class ExternalMemUtil 
{
	// In Android, a folder with name starts with '.' is hidden.
	public static final String DEFAULT_FOLDER_P1 = "Android";
	public static final String DEFAULT_FOLDER_P2 = "Android/data";
	public static final String DEFAULT_FOLDER = "Android/data/.tmp";
	
	public static boolean isReady() {
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); 
	}
	
	public static String getExternalMemRootPath() {
		File sdCardRoot = Environment.getExternalStorageDirectory();
		return sdCardRoot.getPath();  
	}
	
	public static String getDefaultDirStr() {
		return getExternalMemRootPath() + "/" + DEFAULT_FOLDER + "/";
	}	

	public static File getDefaultDir() {
		return new File(getDefaultDirStr());
	}
	
	// The root dir of the default dir  
	public static File getDefaultDirP1() {
		return new File(getExternalMemRootPath() + "/" + DEFAULT_FOLDER_P1 + "/");
	}
	
	// The parent dir of the default dir
	public static File getDefaultDirP2() {
		return new File(getExternalMemRootPath() + "/" + DEFAULT_FOLDER_P2 + "/");
	}
	
	/**
	 * Save file to external memory. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. /sdcard/Android/data/.tmp/contact_2011-01-01.txt
	 */
	public static File Save(String fullname, String content) throws Exception
	{
		File file = null;
		
		if (isReady())
		{
			file = new File(getExternalMemRootPath() + "/" + fullname);
			if (file.exists()) file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
		}
		
		return file;
	}
	
	public static File Save2DefaultDir(String name, String content) throws Exception
	{
		File file = null;
		
		if (isReady())
		{
			file = new File(getExternalMemRootPath() + "/" + DEFAULT_FOLDER + "/" + name);
			if (file.exists()) file.delete();
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
		}
		
		return file;
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
	
	public static void copy2DefaultDir(File file) throws Exception
	{
		if (file == null) return;
		
		if (isReady())
		{
			File dstFile = new File(getExternalMemRootPath() + "/" + DEFAULT_FOLDER + "/" + file.getName());
			copyFile(file, dstFile);
		}
	}
	
	public static File creatDir(String dirName)
	{  
        File dir = new File(getExternalMemRootPath() + "/" + dirName);  
        dir.mkdir();  
        return dir;  
    }
	
	public static File createDefaultDir()
	{  
		File dirP1 = getDefaultDirP1();
		File dirP2 = getDefaultDirP2();
        File dir = getDefaultDir();
        try {
        	if (!dirP1.exists()) dirP1.mkdir();
        	if (!dirP2.exists()) dirP2.mkdir();
        	if (!dir.exists()) dir.mkdir();
        } catch (Exception ex) {}
        return dir;  
    }
	
	public static boolean isDirExist(String dirName) {  
        File dir = new File(getExternalMemRootPath() + "/" + dirName);
        return dir.exists();
    }
	
	public static boolean defaultDirExist()	{  
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
	
}
