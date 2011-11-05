package com.particle.inspector.authsrv.sqlite.metadata;

import java.util.Date;

import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.license.LicenseCtrl;

public class TKey 
{
	private int id;
	private String key; // The activation key
	private LICENSE_TYPE keyType;
	private String deviceID;
	private String phoneNum;
	private String phoneModel; // Phone model
	private String androidVer; // The Android version of the phone
	private String consumeDate;
	private String recvMail;
	private String recvPhoneNum;
	
	public TKey(int id, String key, LICENSE_TYPE keyType, String deviceID, String phoneNum, String phoneModel, String androidVer, String consumeDate) 
	{
		if (id >= 0) this.id = id; else this.id = -1;
		if (key != null) this.key = key; else this.key = "";
		if (keyType != null) this.keyType = keyType;  else this.keyType = LICENSE_TYPE.NOT_LICENSED;
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (consumeDate != null) this.consumeDate = consumeDate; else this.consumeDate = "";
		this.recvMail = "";
		this.recvPhoneNum = "";
	}
	
	public TKey(String key, LICENSE_TYPE keyType, String deviceID, String phoneNum, String phoneModel, String androidVer, String consumeDate) 
	{
		if (key != null) this.key = key; else this.key = "";
		if (keyType != null) this.keyType = keyType;  else this.keyType = LICENSE_TYPE.NOT_LICENSED;
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (consumeDate != null) this.consumeDate = consumeDate; else this.consumeDate = "";
		this.recvMail = "";
		this.recvPhoneNum = "";
	}
	
	public TKey(int id, String key, LICENSE_TYPE keyType, String deviceID, String phoneNum, String phoneModel, String androidVer, String consumeDate, String recvMail, String recvPhoneNum) 
	{
		if (id >= 0) this.id = id; else this.id = -1;
		if (key != null) this.key = key; else this.key = "";
		if (keyType != null) this.keyType = keyType;  else this.keyType = LICENSE_TYPE.NOT_LICENSED;
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (consumeDate != null) this.consumeDate = consumeDate; else this.consumeDate = "";
		if (recvMail != null) this.recvMail = recvMail; else this.recvMail = "";
		if (recvPhoneNum != null) this.recvPhoneNum = recvPhoneNum; else this.recvPhoneNum = "";
	}
	
	public TKey(String key, LICENSE_TYPE keyType, String deviceID, String phoneNum, String phoneModel, String androidVer, String consumeDate, String recvMail, String recvPhoneNum) 
	{
		if (key != null) this.key = key; else this.key = "";
		if (keyType != null) this.keyType = keyType;  else this.keyType = LICENSE_TYPE.NOT_LICENSED;
		if (deviceID != null) this.deviceID = deviceID; else this.deviceID = "";
		if (phoneNum != null) this.phoneNum = phoneNum; else this.phoneNum = "";
		if (phoneModel != null) this.phoneModel = phoneModel; else this.phoneModel = "";
		if (androidVer != null) this.androidVer = androidVer; else this.androidVer = "";
		if (consumeDate != null) this.consumeDate = consumeDate; else this.consumeDate = "";
		if (recvMail != null) this.recvMail = recvMail; else this.recvMail = "";
		if (recvPhoneNum != null) this.recvPhoneNum = recvPhoneNum; else this.recvPhoneNum = "";
	}

	// Getters and setters
	public int getId() { return id;	}
	public void setId(int id) {	this.id = id; }
	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }	
	public LICENSE_TYPE getKeyType() { return keyType; }
	public void setKeyType(LICENSE_TYPE keyType) { this.keyType = keyType; }	
	public String getDeviceID() { return deviceID; }
	public void setDeviceID(String deviceID) { this.deviceID = deviceID; }
	public String getPhoneNum() { return phoneNum; }
	public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }
	public String getPhoneModel() { return phoneModel; }
	public void setPhoneModel(String phoneModel) { this.phoneModel = phoneModel; }	
	public String getAndroidVer() { return androidVer; }
	public void setAndroidVer(String androidVer) { this.androidVer = androidVer; }	
	public String getConsumeDate() { return consumeDate; }
	public void setConsumeDate(String consumeDate) { this.consumeDate = consumeDate; }
	public String getRecvMail() { return recvMail; }
	public void setRecvMail(String recvMail) { this.recvMail = recvMail; }
	public String getRecvPhoneNum() { return recvPhoneNum; }
	public void setRecvPhoneNum(String recvPhoneNum) { this.recvPhoneNum = recvPhoneNum; }
	
	@Override
	public String toString() {
		return "Key [id=" + String.valueOf(id) + 
				 ", key=" + key +
			 ", keyType=" + LicenseCtrl.enumToStr(keyType) + 
		    ", deviceid=" + deviceID +
            ", phonenum=" + phoneNum +
            ", phonemodel=" + phoneModel +
            ", androidver=" + androidVer +
			", consumedate=" + consumeDate +
			", receivermailaddress=" + recvMail +
			", receiverphonenum=" + recvPhoneNum +
			 "]";
	}

}
