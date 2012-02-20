package com.particle.inspector.common.util;

import java.io.File;
import java.util.Comparator;

public class FileCompareUtil 
{
	static public class CompratorByLastModified implements Comparator 
	{
		public int compare(Object o1, Object o2) 
		{
			File file1 = (File)o1;
			File file2 = (File)o2;
			long diff = file1.lastModified() - file2.lastModified();
			if (diff > 0)
				return 1;
			else if (diff == 0)
				return 0;
			else
				return -1;
		}
	}
	
	static public class CompratorBySize implements Comparator 
	{
		public int compare(Object o1, Object o2) 
		{
			File file1 = (File)o1;
			File file2 = (File)o2;
			long diff = file1.length() - file2.length();
			if (diff > 0)
				return 1;
			else if (diff == 0)
				return 0;
			else
				return -1;
		}
	}
}