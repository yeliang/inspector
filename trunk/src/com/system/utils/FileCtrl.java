package com.system.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

/**
 * File I/O control <-> SD-Card
*/
public class FileCtrl 
{
	/**
	 * Save file to SD-CARD. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. /tmp/contact_2011-01-01.txt
	 */
	public static File Save2SDCard(String fullname, String content) throws Exception
	{
		File file = null;
		
		if (isSDCardReady())
		{
			file = new File(getSDCardRootPath() + fullname);
			if (file.exists()) file.delete();
			file.createNewFile();
			//file.setWritable(true);
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.close();
		}
		
		return file;
	}
	
	public static boolean isSDCardReady()
	{
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); 
	}
	
	public static String getSDCardRootPath()
	{
		File sdCardRoot = Environment.getExternalStorageDirectory();
		return sdCardRoot.getPath() + "//";  
	}
}