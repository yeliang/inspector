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
	private static final int DEFAULT_TRY_COUNT = 20;
	private static final int SLEEP_TIME = 5000; // ms
	
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
	
	// Get base station location
	private BaseStationLocation getBaseStationLocation(Context context) {
		TelephonyManager mTManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if (mTManager == null) return null; 
		GsmCellLocation gcl = (GsmCellLocation)mTManager.getCellLocation();
		if (gcl == null) return null;
		int cid = gcl.getCid();
		int lac = gcl.getLac();
		int mcc = Integer.valueOf(mTManager.getNetworkOperator().substring(0, 3));
		int mnc = Integer.valueOf(mTManager.getNetworkOperator().substring(3, 5));
		return (new BaseStationLocation(cid, lac, mcc, mnc));
	}
	
	// When network available, send json to google maps to get geo location
	private String getGeoLocByBaseStationLoc(BaseStationLocation bsLoc) {
		try {
			// Construct json object
			JSONObject jObject = new JSONObject();
			jObject.put("version", "1.1.0");
			jObject.put("host", "maps.google.com");
			jObject.put("request_address", true);
			if (bsLoc.mcc == 460) {
				jObject.put("address_language", "zh_CN");
			} else {
				jObject.put("address_language", "en_US");
			}
			JSONArray jArray = new JSONArray();
			JSONObject jData = new JSONObject();
			jData.put("cell_id", bsLoc.cid);
			jData.put("location_area_code", bsLoc.lac);
			jData.put("mobile_country_code", bsLoc.mcc);
			jData.put("mobile_network_code", bsLoc.mnc);
			jArray.put(jData);
			jObject.put("cell_towers", jArray);
			
			// Send to google maps
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://www.google.com/loc/json");
			StringEntity se = new StringEntity(jObject.toString());
			post.setEntity(se);
			HttpResponse resp = client.execute(post);
			BufferedReader br = null;
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
				
				// Get response
				String getNumber = ("cid: " + bsLoc.cid + SysUtils.NEWLINE)
								 + ("lac: " + bsLoc.lac + SysUtils.NEWLINE)
								 + ("mcc: " + bsLoc.mcc + SysUtils.NEWLINE)
								 + ("mnc: " + bsLoc.mnc + SysUtils.NEWLINE);
				StringBuffer sb = new StringBuffer();
				String result = br.readLine();
				while (result != null) {
					sb.append(getNumber);
					sb.append(result);
					result = br.readLine();
				}
				return sb.toString();
			} else {
				return null;
			}
		} catch (Exception ex) {
			return null;
		}
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
			//if (tryToEnableGPS) SysUtils.threadSleep(10000, LOGTAG);
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
		/*
		if (provider == "") {
			if (this.locationQueue.size() == 0) {
				return null;
			} else {
				realOrHistorical = HISTPOSITION;
				return this.locationQueue.getLast();
			}
		}
		*/
		
		// If provider is available
        try {
        	Location loc = null;
        	int tryCount = 0;
        	while (loc == null && tryCount < DEFAULT_TRY_COUNT) {
        		tryCount++;
        		loc = locationManager.getLastKnownLocation(provider);
        		SysUtils.threadSleep(SLEEP_TIME, LOGTAG);
        	}
        	
        	// If GPS previously forced to be enabled, try to disable it
    		if (tryToEnableGPS) {
    			GpsUtil.disableGPS(context);
    		}
        	
        	// Return location
        	return loc;
        	
        } catch (Exception ex) {
        	Log.e(LOGTAG, ex.getMessage());
        	return null;
        }
		
    }
}
