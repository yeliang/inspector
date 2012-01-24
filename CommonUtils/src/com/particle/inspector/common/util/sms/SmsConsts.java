package com.particle.inspector.common.util.sms;

public class SmsConsts 
{
	public final static String HEADER_CHECKIN          = "Checkin";
	public final static String HEADER_CHECKIN_EX       = "Checkin,";
	public final static String HEADER_TRIAL            = "Trial";
	public final static String HEADER_TRIAL_EX         = "Trial,";
	
	public final static String SEPARATOR               = ",";
	public final static String BLANKSPACE              = " ";
	
	public final static String HEADER_CAL_KEY          = "#KEY#";
	
	public final static String HEADER_INDICATION       = "#";
	public final static String INDICATION_SYSTEM_MSG   = "#M#"; // System message indication
	public final static String INDICATION_SYSTEM_STOP  = "#S#"; // System stop indication
	public final static String INDICATION_SYSTEM_RESTORE = "#R#"; // System restore indication : release system STOP
	
	public final static String INDICATION_KEY          = "#0#";
	public final static String INDICATION_SENDER       = "#1#";
	public final static String INDICATION_RECV_MAIL    = "#2#";
	public final static String INDICATION_RECV_PHONENUM= "#3#";
	public final static String INDICATION_INTERVAL     = "#4#";
	public final static String INDICATION_TARGET_NUM   = "#5#";
	public final static String INDICATION_NETWORK_MODE = "#6#";
	public final static String INDICATION_SENS_WORDS   = "#7#";
	public final static String INDICATION_SIM_CHANGE   = "#9#";
	public final static String INDICATION_LOCATION     = "#dw";
	public final static String INDICATION_LOCATION_ALIAS = "#location";
	public final static String INDICATION_RING         = "#xl";
	public final static String INDICATION_RING_ALIAS   = "#bell";
	public final static String INDICATION_ENV          = "#hj";
	public final static String INDICATION_ENV_ALIAS    = "#env";
	
	public final static String ON  = "ON";
	public final static String OFF = "OFF";
	public final static String YES = "YES";
	public final static String NO  = "NO";
	public final static String SUCCESS = "OK";
	public final static String FAILURE = "NG";
	public final static String ALL = "ALL";
	public final static String ACTIVE = "A";
	public final static String SILENT = "S";
	public final static String WIFIACTIVE = "WA";
	public final static String WIFISILENT = "WS";
	public final static String GET = "GET";
}
