package com.particle.inspector.common.util.gps;

import com.particle.inspector.common.util.SysUtils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GpsUtil 
{
	private static final String LOGTAG = "GpsUtil";
	private static final int DEFAULT_INTERVAL = 1000; // ms
	private static final float DEFAULT_DISTANCE = 100; // meter
	private static final int DEFAULT_TRY_COUNT = 1000;
	
	private LocationManager locationManager;
	
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
		    double latitude = location.getLatitude();
		    double longitude = location.getLongitude();
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		};
	};
		
	public GpsUtil(Context context) {
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL*300, DEFAULT_DISTANCE*10, locationListener);
	}

	public static boolean isGpsAvailabe(Context context) 
	{
        return ((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
	
	public Location getLocation()
    {
		if (locationManager == null) return null;
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		//criteria.setPowerRequirement(Criteria.POWER_LOW); // Low power
		boolean getGPS = false;
		try {
			locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			getGPS = true;
		} catch (Exception ex) {}
		String bestProvider = "";
        if (getGPS) 
        	bestProvider = locationManager.getBestProvider(criteria, true);
        else 
        	bestProvider = locationManager.getBestProvider(criteria, false);
		
        try {
        	Location loc = null;
        	int tryCount = 0;
        	while (loc == null && tryCount < DEFAULT_TRY_COUNT) {
        		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
        		SysUtils.threadSleep(100, LOGTAG);
        		loc = locationManager.getLastKnownLocation(bestProvider);
        		tryCount++;
        	}
        	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL*300, DEFAULT_DISTANCE*10, locationListener);
        	try {
    			if (getGPS) locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
    		} catch (Exception ex) {}
        	return loc;        	
        } catch (Exception ex) {
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
    }
}
