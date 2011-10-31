package system.service.feature.location;

public class BaseStationLocation {
	public int cid;
	public int lac;
	public int mcc;
	public int mnc;
	
	public BaseStationLocation(int cid, int lac, int mcc, int mnc) {
		this.cid = cid;
		this.lac = lac;
		this.mcc = mcc;
		this.mnc = mnc;
	}
}
