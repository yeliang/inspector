package android.service.feature.location;

import android.location.Location;

public class LocationInfo 
{
	public static final String GPS  = "GPS";
	public static final String WIFI = "WIFI";
	
	public Location location;
	public String type;//GPS or WIFI
	
	public LocationInfo(Location location, String type) {
		this.location = location;
		this.type = type;
	}
}
