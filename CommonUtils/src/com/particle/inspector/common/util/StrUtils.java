package com.particle.inspector.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils 
{
	public static final String REGEXP_MAIL = "[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+"; // regexp of mail address
	public static final String COMMA = ": ";
	public static final String SEPARATELINE = "\r\n----------------------------------------\r\n";
	
	public static String toCommaString(List<String> list)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++)
			sb.append(list.get(i).trim() + ",");
		
		String str = sb.toString();
		if (str.length() > 1) str = str.substring(0, str.length() - 2);
		return str;
	}
	
	// Filter and return valid mail address
	public static String[] filterMails(String[] mails)
	{
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(REGEXP_MAIL);
	    for (int i = 0; i < mails.length; i++)
	    {
	    	 Matcher matcher = p.matcher(mails[i]);
	    	 if (matcher.matches()) {
	    		 list.add(mails[i].trim());
	    	 }
	    }
	    
	    return (String[])list.toArray();
	}
}
