package com.particle.inspector.common.util.gps;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class GpsUtil 
{
	private static final String LOGTAG = "GpsUtil";

	public static boolean isGpsAvailabe(Context context) 
	{
        return ((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
	
	public static Location getLocation(Context context)
    {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // High accuracy
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Low power
        
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.setTestProviderEnabled("gps", true);
        String provider = locationManager.getBestProvider(criteria, true);
        
        try {
        	return locationManager.getLastKnownLocation(provider);
        } catch (Exception ex) {
        	
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
    }
}
