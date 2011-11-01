package com.particle.inspector.common.util;

public class RegExpUtil 
{
	public final static String MULTIPLE_BLANKSPACES = " {2,}"; // Multiple blank spaces
	public final static String VALID_MAIL_ADDR      = "[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+"; // Valid mail address
	public final static String VALID_PHONE_NUM      = "\\d{7,}"; // 7~N number
	public final static String RECORD_TARGET_NUM    = "\\d{4,}"; // 4~N number
}
