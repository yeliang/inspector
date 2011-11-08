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
	public LinkedList<Location> locationGpsQueue; // queue for GPS locations
	public LinkedList<Location> locationWifiQueue;// queue for WIFI locations
	private static final int MAX_QUEUE_LEN = 50;   // MAX length for queue
	
	public LocationManager getLocationManager() {return this.locationManager;}
	
	public LocationUtil(Context context) {
		this.context = context;
		locationGpsQueue = new LinkedList<Location>();
		locationWifiQueue = new LinkedList<Location>();
		try {
			locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locationGpsListener = new MyLocationlistener(LocationInfo.GPS);
			locationWifiListener = new MyLocationlistener(LocationInfo.WIFI);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationGpsListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, DEFAULT_INTERVAL, DEFAULT_DISTANCE, locationWifiListener);
		} catch (Exception ex) {
			
		}
	}
	
	public void destroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(locationGpsListener);
			locationManager.removeUpdates(locationWifiListener);
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
	
	private class MyLocationlistener implements LocationListener 
	{
		private String provider;// GPS or WIFI

		public MyLocationlistener(String provider) {
			this.provider = provider;
		}

		@Override
		public void onLocationChanged(Location location) {
			if (this.provider.equalsIgnoreCase(LocationInfo.GPS))
				addGpsLocation(location);
			else if (this.provider.equalsIgnoreCase(LocationInfo.WIFI))
				addWifiLocation(location);
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
