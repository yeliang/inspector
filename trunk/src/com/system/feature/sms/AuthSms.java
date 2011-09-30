package com.system.feature.sms;

/**
 * The received SMS (client -> server) format: InspectorAuth,[Key],[Lang],[Device ID],[Phone Number]
 * Lang = CN|EN|JP
 * e.g. InspectorAuth,8B122A1DD9,CN,13B789A23CE9125,13980065966
 * 
 * The send SMS (server -> client) format: InspectorAuth,[Key],[Result],[Error Message]
 * e.g. InspectorAuth,8B122A1DD9,OK
 * 		InspectorAuth,8B122A1DD9,NG,This key has already been used.
*/
public class AuthSms 
{
	public final static String SMS_HEADER = "InspectorAuth";
	public final static String SMS_SEPARATOR = ",";
	public final static String SMS_SUCCESS = "OK";
	public final static String SMS_FAILURE = "NG";
	
	private String header;
	private String key;
	private String deviceID;
	private String phoneNum;
	private SMS_RESULT result;
	private String errMsg;
	private LANG lang;
	
	// Constructor for received SMS 
	public AuthSms(String key, String deviceID, String phoneNum, LANG lang) {
		this.header = SMS_HEADER;
		this.key = key;
		this.deviceID = deviceID;
		this.phoneNum = phoneNum;
		this.lang = lang;
	}
	
	// Constructor for send SMS 
	public AuthSms(String key, SMS_RESULT result, String errMsg) {
		this.header = SMS_HEADER;
		this.key = key;
		this.result = result;
		this.errMsg = errMsg;
	}
	
	public AuthSms(String sms, SMS_TYPE type) {
		String[] parts = sms.split(SMS_SEPARATOR);
		if (type == SMS_TYPE.CLIENT) {
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
		} else if (type == SMS_TYPE.SERVER) {
			if (parts.length >= 3) {
				this.header = parts[0].trim();
				this.key = parts[1].trim();
				this.lang = LangUtil.str2enum(parts[2]);
			}
			if (parts.length >= 4) {
				this.errMsg = parts[3].trim();
			}
		}
	}
	
	public String clientSms2Str() {
		return SMS_HEADER + SMS_SEPARATOR + this.key + SMS_SEPARATOR + 
				LangUtil.enum2str(this.lang) + SMS_SEPARATOR + 
				this.deviceID + SMS_SEPARATOR + this.phoneNum; 
	}
	
	public String serverSms2Str() {
		String ret = SMS_HEADER + SMS_SEPARATOR + this.key + SMS_SEPARATOR + 
				(this.result == SMS_RESULT.OK ? SMS_SUCCESS : SMS_FAILURE);
		if (this.result == SMS_RESULT.NG) {
			ret += SMS_SEPARATOR + this.errMsg;
		}
		return ret;
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
	public SMS_RESULT getResult() { return result; }
	public void setResult(SMS_RESULT result) { this.result = result; }
	public String getErrMsg() { return errMsg; }
	public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
	public LANG getLang() { return lang; }
	public void setLang(LANG lang) { this.lang = lang; }
	
}
