package com.particle.inspector.authsrv;

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
	
	AuthSms(String key, String deviceID, String phoneNum) {
		this.header = SMS_HEADER;
		this.key = key;
		this.deviceID = deviceID;
		this.phoneNum = phoneNum;
	}
	
	AuthSms(String sms) {
		String[] parts = sms.split(SMS_SEPARATOR);
		if (parts.length >= 2) {
			this.header = parts[0];
			this.key = parts[1];
		}
		if (parts.length >= 3) {
			this.deviceID = parts[2];
		}
		if (parts.length >= 4) {
			this.phoneNum = parts[3];
		} 
	}
	
	public static String createSuccessReplySms(String key) {
		return SMS_HEADER + SMS_SEPARATOR + key + SMS_SEPARATOR + SMS_SUCCESS;
	}
	
	public static String createFailureReplySms(String key, String errMsg) {
		return SMS_HEADER + SMS_SEPARATOR + key + SMS_SEPARATOR + SMS_FAILURE + SMS_SEPARATOR + errMsg;
	}
	
	// Getter and setter
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getDeviceID() { return deviceID; }
	public void setDeviceID(String deviceID) { this.deviceID = deviceID; }
	public String getPhoneNum() { return phoneNum; }
	public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
	
	@Override
	public String toString() {
		return header == null ? "" : header   + SMS_SEPARATOR + 
		          key == null ? "" : key      + SMS_SEPARATOR + 
		     deviceID == null ? "" : deviceID + SMS_SEPARATOR + 
		     phoneNum == null ? "" : phoneNum;
	}
	
}
