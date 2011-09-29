package com.particle.inspector.authsrv.sqlite.metadata;

import java.util.Date;

public class TKey 
{
	private int id;
	private String key; // The activation key
	private String deviceID;
	private String phoneNum;
	private Date buyDate;
	private Date consumeDate;
	private Date lastActivateDate;
	
	// Reserve fields	
	private String reserve_string_1;
	private String reserve_string_2;
	private long reserve_long_1;
	private Date reserve_datetime_1;

	public TKey(int id, String key, String deviceID, String phoneNum, Date buyDate, Date consumeDate, Date lastActivateDate) {
		this.id = id;
		this.key = key;
		this.deviceID = deviceID;
		this.phoneNum = phoneNum;
		this.buyDate = buyDate;
		this.consumeDate = consumeDate;
		this.lastActivateDate = lastActivateDate;
	}
	
	public TKey(String key, String deviceID, String phoneNum, Date buyDate, Date consumeDate, Date lastActivateDate) {
		this.key = key;
		this.deviceID = deviceID;
		this.phoneNum = phoneNum;
		this.buyDate = buyDate;
		this.consumeDate = consumeDate;
		this.lastActivateDate = lastActivateDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	
	public Date getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(Date buyDate) {
		this.buyDate = buyDate;
	}
	
	public Date getConsumeDate() {
		return consumeDate;
	}

	public void setConsumeDate(Date consumeDate) {
		this.consumeDate = consumeDate;
	}

	public Date getLastActivateDate() {
		return lastActivateDate;
	}

	public void setLastActivateDate(Date lastActivateDate) {
		this.lastActivateDate = lastActivateDate;
	}
	
	@Override
	public String toString() {
		return "KEY [id=" + id + 
				 ", key=" + key + 
		    ", deviceid=" + deviceID +
            ", phonenum=" + phoneNum +
			 ", buydate=" + buyDate +
         ", consumedate=" + consumeDate +
    ", lastactivatedate=" + lastActivateDate +
			 "]";
	}

}
