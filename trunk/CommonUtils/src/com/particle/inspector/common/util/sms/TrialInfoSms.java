package com.particle.inspector.common.util.sms;

import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.license.LicenseCtrl;

/**
 * The trial info SMS (client -> server) format: [Header],[Trial Key],[Lang],[Device ID],[Phone Number],[Phone Model],[Android Version]
 * Lang = CN|EN|JP
 * e.g. Trial,###,CN,13B789A23CE9125,13980065966,HTC Desire,2.3
*/
public class TrialInfoSms 
{
	private String header;
	private String key;
	private String deviceID;
	private String phoneNum;
	private String phoneModel;
	private String androidVer;
	private LANG lang;
	
	// Constructor for client SMS 
	public TrialInfoSms(String deviceID, String phoneNum, String phoneModel, String androidVer, LANG lang) {
		this.header = SmsConsts.HEADER_TRIAL;
		this.key = LicenseCtrl.TRIAL_KEY;
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (lang != null) this.lang = lang; else this.lang = LANG.UNKNOWN;
	}
	
	public TrialInfoSms(String sms) {
		String[] parts = sms.split(SmsConsts.SEPARATOR);
		if (parts.length >= 3) {
			this.header = parts[0].trim();
			this.key = parts[1].toUpperCase().trim();
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
		return SmsConsts.HEADER_TRIAL_EX + this.key + SmsConsts.SEPARATOR + 
				LangUtil.enum2str(this.lang) + SmsConsts.SEPARATOR + 
				this.deviceID + SmsConsts.SEPARATOR + this.phoneNum + SmsConsts.SEPARATOR + 
				this.phoneModel + SmsConsts.SEPARATOR + this.androidVer; 
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
