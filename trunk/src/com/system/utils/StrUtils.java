package com.system.utils;

import java.util.List;

public class StrUtils 
{
	public static String toCommaString(List<String> list)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size() - 1; i++)
			sb.append(list.get(i).trim() + ",");
		
		String str = sb.toString();
		if (str.length() > 1) str = str.substring(0, str.length() - 2);
		return str;
	}
}
