package com.particle.inspector.common.util.location;

public class BaseStationLocation {
	public int stationId;
	public double longitude;
	public double latitude;
	public int cid;
	public int lac;
	public int mcc;
	public int mnc;
	
	public BaseStationLocation(int stationId, double longi, double lati, int cid, int lac, int mcc, int mnc) {
		this.stationId = stationId;
		this.longitude = longi;
		this.latitude = lati;
		this.cid = cid;
		this.lac = lac;
		this.mcc = mcc;
		this.mnc = mnc;
	}
}
