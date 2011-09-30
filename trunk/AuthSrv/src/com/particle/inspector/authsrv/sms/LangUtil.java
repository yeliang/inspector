package com.particle.inspector.authsrv.sms;

public class LangUtil 
{
	public final static String UNKNOWN = "UNKNOWN";
	public final static String CN = "CN";
	public final static String EN = "EN";
	public final static String JP = "JP";
	
	static LANG str2enum(String str) {
		if (str.compareToIgnoreCase(CN) == 0) return LANG.CN;
		else if (str.compareToIgnoreCase(EN) == 0) return LANG.EN;
		else if (str.compareToIgnoreCase(JP) == 0) return LANG.JP;
		else return LANG.UNKNOWN;
	}
	
	static String enum2str(LANG lang) {
		if (lang == LANG.CN) return CN;
		else if (lang == LANG.EN) return EN;
		else if (lang == LANG.JP) return JP;
		else return UNKNOWN;
	}
}