package com.particle.inspector.common.util.sms;

public class SmsConsts 
{
	public final static String HEADER_AUTH             = "Auth";
	public final static String HEADER_AUTH_EX          = "Auth,";
	public final static String HEADER_INFO             = "Info";
	public final static String HEADER_INFO_EX          = "Info,";
	public final static String HEADER_SUPER_LOGGING    = "Super";
	public final static String HEADER_SUPER_LOGGING_EX = "Super,";
	public final static String HEADER_UNREGISTER       = "Unregister";
	public final static String HEADER_UNREGISTER_EX    = "Unregister,";
	
	public final static String SEPARATOR               = ",";
	public final static String BLANKSPACE              = " ";
	
	public final static String HEADER_INDICATION       = "#";
	public final static String INDICATION_KEY          = "#0#";
	public final static String INDICATION_SENDER       = "#1#";
	public final static String INDICATION_RECV_MAIL    = "#2#";
	public final static String INDICATION_RECV_PHONENUM= "#3#";
	public final static String INDICATION_INTERVAL     = "#4#";
	public final static String INDICATION_TARGET_NUM   = "#5#";
	public final static String INDICATION_NETWORK_MODE = "#6#";
	public final static String INDICATION_SENS_WORDS   = "#7#";
	public final static String INDICATION_LOCATION     = "#dw";
	
	public final static String ON  = "ON";
	public final static String OFF = "OFF";
	public final static String YES = "YES";
	public final static String NO  = "NO";
	public final static String SUCCESS = "OK";
	public final static String FAILURE = "NG";
	public final static String ALL = "ALL";
	public final static String ACTIVE = "A";
	public final static String SILENT = "S";
	public final static String GET = "GET";
}
