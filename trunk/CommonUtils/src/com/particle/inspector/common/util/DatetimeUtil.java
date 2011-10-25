package com.particle.inspector.common.util;

import java.text.SimpleDateFormat;

public class DatetimeUtil 
{
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_FORMAT2 = "yyyy-MM-dd_HH-mm-ss";
	public static final String DATETIME_FORMAT3 = "yyyyMMdd";
	public static final SimpleDateFormat format = new SimpleDateFormat(DATETIME_FORMAT);
	public static final SimpleDateFormat format2 = new SimpleDateFormat(DATETIME_FORMAT2);
	public static final SimpleDateFormat format3 = new SimpleDateFormat(DATETIME_FORMAT3);
}
