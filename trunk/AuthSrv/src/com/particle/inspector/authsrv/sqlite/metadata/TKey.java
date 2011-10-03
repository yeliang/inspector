package com.particle.inspector.authsrv.sqlite.metadata;

import java.util.Date;

public class TKey 
{
	private int id;
	private String key; // The activation key
	private String deviceID;
	private String phoneNum;
	private String phoneModel; // Phone model
	private String androidVer; // The Android version of the phone
	private String buyDate;
	private String consumeDate;
	private String lastActivateDate;
	
	// Reserve fields	
	private String reserve_string_1;
	private String reserve_string_2;
	private long reserve_long_1;
	private Date reserve_datetime_1;

	public TKey(int id, String key, String deviceID, String phoneNum, String phoneModel, String androidVer,
			String buyDate, String consumeDate, String lastActivateDate) {
		if (id >= 0) this.id = id;
		if (key != null) this.key = key;
		if (deviceID != null) this.deviceID = deviceID;
		if (phoneNum != null) this.phoneNum = phoneNum;
		if (phoneModel != null) this.phoneModel = phoneModel;
		if (androidVer != null) this.androidVer = androidVer;
		if (buyDate != null) this.buyDate = buyDate;
		if (consumeDate != null) this.consumeDate = consumeDate;
		if (lastActivateDate != null) this.lastActivateDate = lastActivateDate;
	}
	
	public TKey(String key, String deviceID, String phoneNum, String phoneModel, String androidVer,
			String buyDate, String consumeDate, String lastActivateDate) {
		if (key != null) this.key = key;
		if (deviceID != null) this.deviceID = deviceID;
		if (phoneNum != null) this.phoneNum = phoneNum;
		if (phoneModel != null) this.phoneModel = phoneModel;
		if (androidVer != null) this.androidVer = androidVer;
		if (buyDate != null) this.buyDate = buyDate;
		if (consumeDate != null) this.consumeDate = consumeDate;
		if (lastActivateDate != null) this.lastActivateDate = lastActivateDate;
	}

	// Getters and setters
	public int getId() { return id;	}
	public void setId(int id) {	this.id = id; }
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
	public String getBuyDate() { return buyDate; }
	public void setBuyDate(String buyDate) { this.buyDate = buyDate; }	
	public String getConsumeDate() { return consumeDate; }
	public void setConsumeDate(String consumeDate) { this.consumeDate = consumeDate; }
	public String getLastActivateDate() { return lastActivateDate; }
	public void setLastActivateDate(String lastActivateDate) { this.lastActivateDate = lastActivateDate; }
	
	@Override
	public String toString() {
		return "Key [id=" + String.valueOf(id) + 
				 ", key=" + key + 
		    ", deviceid=" + deviceID +
            ", phonenum=" + phoneNum +
            ", phonemodel=" + phoneModel +
            ", androidver=" + androidVer +
			 ", buydate=" + buyDate +
         ", consumedate=" + consumeDate +
    ", lastactivatedate=" + lastActivateDate +
			 "]";
	}

}
