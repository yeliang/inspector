package system.service.feature.location;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.SysUtils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class LocationUtil 
{
	private static final String LOGTAG = "GpsUtil";
	private static final int DEFAULT_INTERVAL = 60000; // ms
	private static final float DEFAULT_DISTANCE = 0; // meter
	private static final int DEFAULT_TRY_COUNT = 10;
	private static final int SLEEP_TIME = 1000; // ms
	
	private Context context;
	private LocationManager locationManager;
	private LinkedList<Location> locationQueue;
	private static final int MAX_QUEUE_LEN = 100;
	
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
	
	public LocationUtil(Context context) {
		this.context = context;
		locationQueue = new LinkedList<Location>();
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
		}
	}
	
	public void destroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
		}
	}
	
	private void updateLocation(String provider) 
	{
		try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            //criteria.setPowerRequirement(Criteria.POWER_LOW);

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
	
	// If return is not null and it is realtime position, realOrHistorical = "real"
	// If return is not null and it is historical position, realOrHistorical = "hist"
	public Location getLocation(String realOrHistorical)
    {
		if (locationManager == null) return null;
		
		// If GPS is not enabled, try to enable it
		int tryCount = 0;
		boolean tryToEnableGPS = false;
		while (!GpsUtil.isGpsEnabled(context) && tryCount < DEFAULT_TRY_COUNT) {
			GpsUtil.enableGPS(context);
			tryToEnableGPS = true;
			SysUtils.threadSleep(3000, LOGTAG);
		}
		if (tryToEnableGPS) {
			SysUtils.threadSleep(10000, LOGTAG);
		}
		
		// Start to get location
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		
		String provider = "";
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			provider = LocationManager.NETWORK_PROVIDER;
		}
		
		// If cannot get any provider, return a historical location.
		if (provider == "") {
			if (this.locationQueue.size() == 0) {
				return null;
			} else {
				realOrHistorical = HISTPOSITION;
				return this.locationQueue.getLast();
			}
		}
		
		// If provider is available
        try {
        	Location loc = null;
        	tryCount = 0;
        	while (loc == null && tryCount < DEFAULT_TRY_COUNT) {
        		tryCount++;
        		loc = locationManager.getLastKnownLocation(provider);
        		SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
        	}
        	
        	// Return location
        	if (loc != null) {
        		realOrHistorical = REALPOSITION;
        		// If GPS previously forced to be enabled, try to disable it
        		if (tryToEnableGPS) {
        			GpsUtil.disableGPS(context);
        		}
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
