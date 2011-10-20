package com.particle.inspector.common.util.gps;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.particle.inspector.common.util.SysUtils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class GpsUtil 
{
	private static final String LOGTAG = "GpsUtil";
	private static final int DEFAULT_INTERVAL = 1000; // ms
	private static final float DEFAULT_DISTANCE = 100; // meter
	private static final int DEFAULT_TRY_COUNT = 100;
	private static final int SLEEP_TIME = 500; // ms
	
	private Context context;
	private LocationManager locationManager;
	private LinkedList<Location> locationQueue;
	private static final int MAX_QUEUE_LEN = 300;
	
	public static final String REALPOSITION = "real";
	public static final String HISTPOSITION = "hist";
	
	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			if (location != null) {
				addLocation(location);
            }
		}

		@Override
		public void onProviderDisabled(String provider) {
			try {
				locationManager.setTestProviderEnabled(provider, false);
			} catch (Exception ex) {
				Log.v(LOGTAG, "Failed to disable " + provider);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			updateLocation(provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			if (status != LocationProvider.AVAILABLE) return;
			updateLocation(provider);
		};
	};
	
	private void updateLocation(String provider) 
	{
		try {
			locationManager.setTestProviderEnabled(provider, true);
		} catch (Exception ex) {
			Log.v(LOGTAG, "Failed to enable " + provider);
		}
		
		try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
            {
            	Location location = null;
            	int tryCount = 0;
            	while (location == null && tryCount < DEFAULT_TRY_COUNT) {
            		tryCount++;
            		SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
            		location = locationManager.getLastKnownLocation(provider);
            	}
            	
            	if (location != null) {
            		addLocation(location);
            	}
            }
        } catch (Exception e) {
            Log.e(LOGTAG, e.getMessage());
        }
	}
	
	private void addLocation(Location location) {
        if (this.locationQueue == null) return;
        if (this.locationQueue.size() >= MAX_QUEUE_LEN) this.locationQueue.poll();
        this.locationQueue.offer(location);
    }
		
	public GpsUtil(Context context) {
		this.context = context;
		this.locationQueue = new LinkedList<Location>();
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL*300, DEFAULT_DISTANCE, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL*300, DEFAULT_DISTANCE, locationListener);
	}

	public static boolean isGpsAvailabe(Context context) 
	{
        return ((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
	
	// If return is not null and it is realtime position, realOrHistorical = "real"
	// If return is not null and it is historical position, realOrHistorical = "hist"
	public Location getLocation(String realOrHistorical)
    {
		if (locationManager == null) return null;
		
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		//criteria.setPowerRequirement(Criteria.POWER_LOW); // Low power
		
		String provider = "";
		
		// Try to get GPS provider
		boolean setGpsProviderEnabled = false;
		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			try {
				locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
				SysUtils.threadSleep(5000, LOGTAG); // sleep a while waiting for GPS enabled
				setGpsProviderEnabled = true;
				provider = LocationManager.GPS_PROVIDER;
			} catch (Exception ex) {}
		} else { //
			provider = LocationManager.GPS_PROVIDER;
		}
		
		// If cannot get GPS provider, try to get network provider
		boolean setNetworkProviderEnabled = false;
		if (!provider.equals(LocationManager.GPS_PROVIDER)) { 
			if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				provider = LocationManager.NETWORK_PROVIDER;
			} else {
				// Try to enable network
				try {
					locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
					SysUtils.threadSleep(5000, LOGTAG); // sleep a while waiting for network enabled
					setNetworkProviderEnabled = true;
					provider = LocationManager.NETWORK_PROVIDER;
				} catch (Exception ex) {}
			}
		}
		
		// If cannot get any provider, return.
		if (!provider.equals(LocationManager.GPS_PROVIDER) &&
			!provider.equals(LocationManager.NETWORK_PROVIDER)) {
			if (this.locationQueue.size() == 0) {
				return null;
			} else {
				realOrHistorical = HISTPOSITION;
				return this.locationQueue.getLast();
			}
		}
		
        try {
        	Location loc = null;
        	int tryCount = 0;
        	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
        	while (loc == null && tryCount < DEFAULT_TRY_COUNT) {
        		tryCount++;
        		loc = locationManager.getLastKnownLocation(provider);
        		SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
        	}
        	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL*300, DEFAULT_DISTANCE, locationListener);
        	
        	// Try to disable providers
        	try {
    			if (setGpsProviderEnabled) locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
    		} catch (Exception ex) {}
        	try {
    			if (setNetworkProviderEnabled) locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false);
    		} catch (Exception ex) {}
        	
        	// Return location
        	if (loc != null) {
        		realOrHistorical = REALPOSITION;
        		return loc;
        	} else {
        		if (this.locationQueue.size() == 0) {
    				return null;
    			} else {
    				realOrHistorical = HISTPOSITION;
    				return this.locationQueue.getLast();
    			}
        	}	        	
        } catch (Exception ex) {
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
    }
}
