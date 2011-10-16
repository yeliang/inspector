package com.particle.inspector.common.util.gps;

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
	private static final int DEFAULT_INTERVAL = 180000; // 180000ms = 3min
	private static final float DEFAULT_DISTANCE = 100; // meter
	
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
	}

	public static boolean isGpsAvailabe(Context context) 
	{
        return ((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
	
	public Location getLocation()
    {
		if (locationManager == null) return null;
		
        //Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.ACCURACY_FINE); // High accuracy
        //criteria.setAltitudeRequired(false);
        //criteria.setBearingRequired(false);
        //criteria.setCostAllowed(true);
        //criteria.setPowerRequirement(Criteria.POWER_LOW); // Low power
		//locationManager.setTestProviderEnabled("gps", true);
        //String provider = locationManager.getBestProvider(criteria, true);
        
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, false);
		
        try {
        	//return this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        	return locationManager.getLastKnownLocation(bestProvider);
        	//if (loc == null)
        	//	return locationManager.getLastKnownLocation(provider);
        	
        } catch (Exception ex) {
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
    }
}
