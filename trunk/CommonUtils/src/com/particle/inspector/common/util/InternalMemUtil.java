package com.particle.inspector.common.util;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

public class InternalMemUtil 
{
	// 1MB = 1024*1024 = 1048576 bytes
	public static long BYTES_OF_1MB = 1048576;
	
	// Get free space on internal memory returning bytes like 79859712 (=76.16MB)
	public static long getFreeSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		int blockSize = stat.getBlockSize();
		int availableBlocks = stat.getAvailableBlocks();
		return ((long) availableBlocks * blockSize);
	}
	
	// Get free space on internal memory returning string like "76.16MB" 
	public static String getFreeSize(Context context) {
		return Formatter.formatFileSize(context, getFreeSize());
	}
	
	// Get the string of files folder of current app in internal storage
	public static String getFilesDirStr(Context context) {
		String path = context.getApplicationContext().getFilesDir().getAbsolutePath() + "/";
		return path;
	}

	public static File getFilesDir(Context context) {
		return new File(getFilesDirStr(context));
	}
	
	/**
	 * Save file to internal storage. If the file exists, overwrite it. 
	 * @param fullname the fullname of the file, e.g. .info/contact_2011-01-01.txt, .recording/Recording_2011-01-01.wav
	 */
	public static File Save(Context context, String fullname, String content) throws Exception
	{
		File file = null;
		
		file = new File(getFilesDirStr(context) + "/" + fullname);
		if (file.exists()) file.delete();
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(content.getBytes());
		fos.close();
		
		return file;
	}
	
}
