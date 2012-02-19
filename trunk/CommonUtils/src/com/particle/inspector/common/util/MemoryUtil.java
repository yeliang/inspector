package com.particle.inspector.common.util;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

public class MemoryUtil 
{
	// 1MB = 1024*1024 = 1048576 bytes
	public static long BYTES_OF_1MB = 1048576;
	
	// Get free space on internal memory returning bytes like 79859712 (=76.16MB)
	public static long getFreeSizeOfInternalMemory() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return (availableBlocks * blockSize);
	}
	
	// Get free space on internal memory returning string like "76.16MB" 
	public static String getFreeSizeOfInternalMemory(Context context) {
		return Formatter.formatFileSize(context, getFreeSizeOfInternalMemory());
	}
}
