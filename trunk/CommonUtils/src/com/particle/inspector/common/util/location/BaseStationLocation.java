package com.particle.inspector.common.util.location;

public class BaseStationLocation 
{
	public static final String GSM = "GSM";
	public static final String G3 = "3G";
	
	public String type;
	public int stationId;
	public double longitude;
	public double latitude;
	public int cid;
	public int lac;
	public int mcc;
	public int mnc;
	
	public BaseStationLocation(String type, int stationId, double longi, double lati, int cid, int lac, int mcc, int mnc) {
		this.type = type;
		this.stationId = stationId;
		this.longitude = longi;
		this.latitude = lati;
		this.cid = cid;
		this.lac = lac;
		this.mcc = mcc;
		this.mnc = mnc;
	}
}
