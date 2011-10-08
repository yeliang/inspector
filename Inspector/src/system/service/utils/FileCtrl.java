package system.service.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

/**
 * File I/O control <-> SD-Card
*/
public class FileCtrl 
{
	public static final String DEFAULT_FOLDER = "tmp";
	
	public static final String SUFFIX_TXT = ".txt";

	private static final String LOGTAG = "FileCtrl";
	

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
	
	public static boolean isSDCardReady()
	{
		return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); 
	}
	
	public static String getSDCardRootPath()
	{
		File sdCardRoot = Environment.getExternalStorageDirectory();
		return sdCardRoot.getPath() + "//";  
	}

	public static String makeFileName(Context context, String nameBase, String suffix) 
	{
		String dateStr = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		String phoneNum = DeviceProperty.getPhoneNumber(context);
		return nameBase + "-" + (phoneNum.length() > 0 ? (phoneNum + "-") : "") + dateStr + suffix;
	}
	
	public static File creatSDDir(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + dirName);  
        dir.mkdir();  
        return dir;  
    }
	
	public static File creatDefaultSDDir()
	{  
        File dir = new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER);  
        dir.mkdir();  
        return dir;  
    }
	
	public static boolean dirExist(String dirName)
	{  
        File dir = new File(getSDCardRootPath() + dirName);
        return dir.exists();
    }
	
	public static boolean defaultDirExist()
	{  
        File dir = new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER);
        return dir.exists();
    }
	
	public static void cleanFolder() 
	{
		File dir = new File(getSDCardRootPath() + "/" + DEFAULT_FOLDER);
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
						Log.e(LOGTAG, ex == null ? "Cannot delete file <" + files[i].getName() + ">" : ex.toString());
					}
				}
			}
			
			// Try to remove the directory
			if (dir.listFiles().length == 0) {
				try {
					dir.delete();
				} catch (SecurityException e) {
					Log.e(LOGTAG, e.toString());
				}
			}
		}
		
	}
}