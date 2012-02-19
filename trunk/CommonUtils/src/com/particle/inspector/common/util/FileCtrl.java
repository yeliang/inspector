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
	public static final String SUFFIX_TXT = ".txt";
	public static final String SUFFIX_WAV = ".wav";

	private static final String LOGTAG = "FileCtrl";
	
	public static void cleanTxtFiles(Context context) 
	{
		//File dir = getDefaultSDDir();
		File dir = InternalMemUtil.getFilesDir(context);
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
	public static int reduceWavFiles(Context context, int numLimit) 
	{
		List<File> wavs = FileCtrl.getAllWavFiles(context);
		int wavCount = wavs.size();
		if (wavCount <= numLimit) return 0;
		
		// Remove old redundant files by time order
		int removedFileCount = 0;
		wavs = FileCtrl.sortFileByTimeOrder(wavs);
		try {
			for (int i = 0; i < (wavCount - numLimit); i++) {
				wavs.get(i).delete();
				removedFileCount++;
			}
		} catch (Exception ex) {
		}
		
		return removedFileCount;
	}
	
	// Remove all files in inspector internal storage
	public static void removeAllFiles(Context context) 
	{
		try {
			File dir = InternalMemUtil.getFilesDir(context);
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
			File dir = InternalMemUtil.getFilesDir(context);
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
			//Log.e(LOGTAG, e.getMessage());
		}
		return wavs;
	}
	
	public static List<File> getAllWavFilesWithPrefix(Context context, String prefix) {
		List<File> wavs = new ArrayList<File>();
		try {
			File dir = InternalMemUtil.getFilesDir(context);
			if (!dir.exists() || !dir.isDirectory()) return wavs;
			
			File[] files = dir.listFiles();
			String name;
			for (File file : files) {
				name = file.getName();
				if (file.isFile() && name.startsWith(prefix) && name.endsWith(FileCtrl.SUFFIX_WAV) )
				{
					wavs.add(file);
				}
			}
		} catch (Exception e) {
			//Log.e(LOGTAG, e.getMessage());
		}
		return wavs;
	}
	
	public static List<File> filterWavFilesByPrefix(List<File> files, String prefix) {
		List<File> wavs = new ArrayList<File>();
		try {
			for (File file : files) {
				if (file.getName().startsWith(prefix)) {
					wavs.add(file);
				}
			}
		} catch (Exception e) {
			//Log.e(LOGTAG, e.getMessage());
		}
		
		return wavs;
	}
	
	@SuppressWarnings("unchecked")
	public static List<File> sortFileByTimeOrder(List<File> files) 
	{
		 Collections.sort(files, new FileCompareUtil.CompratorByLastModified());
		 return files;
	}

}