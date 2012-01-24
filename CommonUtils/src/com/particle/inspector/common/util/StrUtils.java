package com.particle.inspector.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils 
{
	public static final String COMMA = ": ";
	public static final String SEPARATELINE = "\r\n----------------------------------------\r\n";
	
	public static String toCommaString(List<String> list)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++)
			sb.append(list.get(i).trim() + ",");
		
		String str = sb.toString();
		if (str.length() > 1) str = str.substring(0, str.length() - 1);
		return str;
	}
	
	public static boolean validateMailAddress(String mailAddr) 
	{
		Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
		Matcher matcher = p.matcher(mailAddr);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean containSensitiveWords(String txt, String[] sensWords) 
	{
		for (String word : sensWords) {
			if (txt.contains(word))
				return true;
		}
		return false;
	}
	
	// Filter and return valid mail address
	public static String[] filterMails(String[] mails)
	{
		List<String> list = new ArrayList<String>();
		Pattern p = Pattern.compile(RegExpUtil.VALID_MAIL_ADDR);
	    for (int i = 0; i < mails.length; i++)
	    {
	    	 Matcher matcher = p.matcher(mails[i].trim());
	    	 if (matcher.matches()) {
	    		 list.add(mails[i].trim());
	    	 }
	    }
	    
	    String [] nums = new String[list.size()];
	    return list.toArray(nums);
	}
}
