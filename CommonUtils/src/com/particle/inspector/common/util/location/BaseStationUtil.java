package com.particle.inspector.common.util.location;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.particle.inspector.common.util.SysUtils;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

public class BaseStationUtil 
{
	// Get CDMA base station location
	public static BaseStationLocation getCdmaBaseStationLocation(Context context) 
	{
		try {
			TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (mTManager == null) return null;
			CdmaCellLocation gcl = (CdmaCellLocation) mTManager.getCellLocation();
			if (gcl == null) return null;
			int stationId = gcl.getBaseStationId();
			double longi = gcl.getBaseStationLongitude()/14400.0;
			double lati = gcl.getBaseStationLatitude()/14400.0;
			int mcc = Integer.valueOf(mTManager.getNetworkOperator().substring(0, 3));
			int mnc = Integer.valueOf(mTManager.getNetworkOperator().substring(3, 5));
			return (new BaseStationLocation(stationId, longi, lati, -1, -1, mcc, mnc));
		} catch (Exception ex) {
			return null;
		}
	}
	
	// Get GSM base station location
	public static BaseStationLocation getGsmBaseStationLocation(Context context) 
	{
		try {
			TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (mTManager == null) return null;
			GsmCellLocation gcl = (GsmCellLocation) mTManager.getCellLocation();
			if (gcl == null) return null;
			int cid = gcl.getCid();
			int lac = gcl.getLac();
			int mcc = Integer.valueOf(mTManager.getNetworkOperator().substring(0, 3));
			int mnc = Integer.valueOf(mTManager.getNetworkOperator().substring(3, 5));
			return (new BaseStationLocation(-1, -1, -1, cid, lac, mcc, mnc));
		} catch (Exception ex) {
			return null;
		}
	}

	// When network available, send json to google maps to get geo location
	public static String getGeoLocByGsmBaseStationLoc(BaseStationLocation bsLoc) 
	{
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
					//sb.append(getNumber);
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
}
