package com.particle.inspector.common.util.sms;

import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;

/**
 * The super key logging SMS (client -> server) format: [Header],[Key],[Lang],[Device ID],[Phone Number],[Phone Model],[Android Version]
 * Lang = CN|EN|JP
 * e.g. Super,8B122A1DD9,CN,13B789A23CE9125,13980065966,HTC Desire,2.3
 * 

*/
public class SuperLoggingSms 
{
	public final static String SMS_HEADER = "Super";
	public final static String SMS_SEPARATOR = ",";
	
	private String header;
	private String key;
	private String deviceID;
	private String phoneNum;
	private String phoneModel;
	private String androidVer;
	private LANG lang;
	
	// Constructor for client SMS 
	public SuperLoggingSms(String key, String deviceID, String phoneNum, String phoneModel, String androidVer, LANG lang) {
		this.header = SMS_HEADER;
		if (key != null) this.key = key; else this.key = "";
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (lang != null) this.lang = lang; else this.lang = LANG.UNKNOWN;
	}
	
	public SuperLoggingSms(String sms) {
		String[] parts = sms.split(SMS_SEPARATOR);
		
		if (parts.length >= 3) {
			this.header = parts[0].trim();
			this.key = parts[1].trim();
			this.lang = LangUtil.str2enum(parts[2]);
		}
		if (parts.length >= 4) {
			this.deviceID = parts[3].trim();
		}
		if (parts.length >= 5) {
			this.phoneNum = parts[4].trim();
		}
		if (parts.length >= 6) {
			this.phoneModel = parts[5].trim();
		}
		if (parts.length >= 7) {
			this.androidVer = parts[6].trim();
		}
	}
	
	@Override
	public String toString() {
		return SMS_HEADER + SMS_SEPARATOR + this.key + SMS_SEPARATOR + 
				LangUtil.enum2str(this.lang) + SMS_SEPARATOR + 
				this.deviceID + SMS_SEPARATOR + this.phoneNum + SMS_SEPARATOR + 
				this.phoneModel + SMS_SEPARATOR + this.androidVer; 
	}
	
	// Getter and setter
	public String getHeader() { return header; }
	public void setHeader(String header) { this.header = header; }
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getDeviceID() { return deviceID; }
	public void setDeviceID(String deviceID) { this.deviceID = deviceID; }
	public String getPhoneNum() { return phoneNum; }
	public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
	public String getPhoneModel() { return phoneModel; }
	public void setPhoneModel(String phoneModel) { this.phoneModel = phoneModel; }
	public String getAndroidVer() { return androidVer; }
	public void setAndroidVer(String androidVer) { this.androidVer = androidVer; }
	public LANG getLang() { return lang; }
	public void setLang(LANG lang) { this.lang = lang; }
}
